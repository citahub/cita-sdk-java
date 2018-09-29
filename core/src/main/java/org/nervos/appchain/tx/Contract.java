package org.nervos.appchain.tx;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.nervos.appchain.abi.EventEncoder;
import org.nervos.appchain.abi.EventValues;
import org.nervos.appchain.abi.FunctionEncoder;
import org.nervos.appchain.abi.FunctionReturnDecoder;
import org.nervos.appchain.abi.TypeReference;
import org.nervos.appchain.abi.datatypes.Address;
import org.nervos.appchain.abi.datatypes.Event;
import org.nervos.appchain.abi.datatypes.Function;
import org.nervos.appchain.abi.datatypes.Type;
import org.nervos.appchain.crypto.Credentials;
import org.nervos.appchain.protocol.Nervosj;
import org.nervos.appchain.protocol.core.DefaultBlockParameterName;
import org.nervos.appchain.protocol.core.RemoteCall;
import org.nervos.appchain.protocol.core.methods.request.Call;
import org.nervos.appchain.protocol.core.methods.response.AppCall;
import org.nervos.appchain.protocol.core.methods.response.AppGetCode;
import org.nervos.appchain.protocol.core.methods.response.Log;
import org.nervos.appchain.protocol.core.methods.response.TransactionReceipt;
import org.nervos.appchain.protocol.exceptions.TransactionException;
import org.nervos.appchain.tx.exceptions.ContractCallException;
import org.nervos.appchain.utils.Numeric;


/**
 * Solidity contract type abstraction for interacting with smart contracts via native Java types.
 */
@SuppressWarnings("WeakerAccess")
public abstract class Contract extends ManagedTransaction {

    // https://www.reddit.com/r/ethereum/comments/5g8ia6/attention_miners_we_recommend_raising_gas_limit/
    public static final BigInteger GAS_LIMIT = BigInteger.valueOf(4_300_000);

    protected final String contractBinary;
    protected String contractAddress;
    protected BigInteger gasPrice;
    protected BigInteger gasLimit;
    protected TransactionReceipt transactionReceipt;
    protected Map<String, String> deployedAddresses;

    protected Contract(String contractBinary, String contractAddress,
                       Nervosj nervosj, TransactionManager transactionManager,
                       BigInteger gasPrice, BigInteger gasLimit) {
        super(nervosj, transactionManager);

        //this.contractAddress = ensResolver.resolve(contractAddress);

        this.contractAddress = contractAddress;
        this.contractBinary = contractBinary;
        this.gasPrice = gasPrice;
        this.gasLimit = gasLimit;
    }

    protected Contract(String contractBinary, String contractAddress,
                       Nervosj nervosj, TransactionManager transactionManager) {
        this(contractBinary, contractAddress, nervosj,
                transactionManager, BigInteger.ZERO, BigInteger.ZERO);
    }

    protected Contract(String contractBinary, String contractAddress,
                       Nervosj nervosj, Credentials credentials,
                       BigInteger gasPrice, BigInteger gasLimit) {
        this(contractBinary, contractAddress, nervosj,
                new RawTransactionManager(nervosj, credentials),
                gasPrice, gasLimit);
    }

    @Deprecated
    protected Contract(String contractAddress,
                       Nervosj nervosj, TransactionManager transactionManager,
                       BigInteger gasPrice, BigInteger gasLimit) {
        this("", contractAddress, nervosj, transactionManager, gasPrice, gasLimit);
    }

    @Deprecated
    protected Contract(String contractAddress,
                       Nervosj nervosj, TransactionManager transactionManager) {
        this("", contractAddress, nervosj,
                transactionManager, BigInteger.ZERO, BigInteger.ZERO);
    }

    @Deprecated
    protected Contract(String contractAddress,
                       Nervosj nervosj, Credentials credentials,
                       BigInteger gasPrice, BigInteger gasLimit) {
        this("", contractAddress, nervosj,
                new RawTransactionManager(nervosj, credentials),
                gasPrice, gasLimit);
    }

    public void setContractAddress(String contractAddress) {
        this.contractAddress = contractAddress;
    }

    public String getContractAddress() {
        return contractAddress;
    }

    public void setTransactionReceipt(TransactionReceipt transactionReceipt) {
        this.transactionReceipt = transactionReceipt;
    }

    public String getContractBinary() {
        return contractBinary;
    }

    /**
     * Check that the contract deployed at the address associated with this smart contract wrapper
     * is in fact the contract you believe it is.
     *
     * <p>This method uses the
     * <a href="https://github.com/ethereum/wiki/wiki/JSON-RPC#eth_getcode">eth_getCode</a> method
     * to get the contract byte code and validates it against the byte code stored in this smart
     * contract wrapper.
     *
     * @return true if the contract is valid
     * @throws IOException if unable to connect to nervosj node
     */
    public boolean isValid() throws IOException {
        if (contractAddress.equals("")) {
            throw new UnsupportedOperationException(
                    "Contract binary not present, you will need to regenerate your smart "
                            + "contract wrapper with nervosj v2.2.0+");
        }

        AppGetCode ethGetCode = nervosj
                .appGetCode(contractAddress, DefaultBlockParameterName.LATEST)
                .send();
        if (ethGetCode.hasError()) {
            return false;
        }

        String code = Numeric.cleanHexPrefix(ethGetCode.getCode());
        // There may be multiple contracts in the Solidity bytecode, hence we only check for a
        // match with a subset
        return !code.isEmpty() && contractBinary.contains(code);
    }

    /**
     * If this Contract instance was created at deployment, the TransactionReceipt associated
     * with the initial creation will be provided, e.g. via a <em>deploy</em> method. This will
     * not persist for Contracts instances constructed via a <em>load</em> method.
     *
     * @return the TransactionReceipt generated at contract deployment
     */
    public Optional<TransactionReceipt> getTransactionReceipt() {
        return Optional.ofNullable(transactionReceipt);
    }

    /**
     * Execute constant function call - i.e. a call that does not change state of the contract
     *
     * @param function to call
     * @return {@link List} of values returned by function call
     */
    private List<Type> executeCall(
            Function function) throws IOException {
        String encodedFunction = FunctionEncoder.encode(function);
        AppCall ethCall =
                nervosj.appCall(
                        new Call(transactionManager.getFromAddress(),
                                contractAddress, encodedFunction),
                        DefaultBlockParameterName.LATEST).send();

        String value = ethCall.getValue();
        return FunctionReturnDecoder.decode(value, function.getOutputParameters());
    }

    @SuppressWarnings("unchecked")
    protected <T extends Type> T executeCallSingleValueReturn(
            Function function) throws IOException {
        List<Type> values = executeCall(function);
        if (!values.isEmpty()) {
            return (T) values.get(0);
        } else {
            return null;
        }
    }

    @SuppressWarnings("unchecked")
    protected <T extends Type, R> R executeCallSingleValueReturn(
            Function function, Class<R> returnType) throws IOException {
        T result = executeCallSingleValueReturn(function);
        if (result == null) {
            throw new ContractCallException("Empty value (0x) returned from contract");
        }

        Object value = result.getValue();
        if (returnType.isAssignableFrom(value.getClass())) {
            return (R) value;
        } else if (result.getClass().equals(Address.class) && returnType.equals(String.class)) {
            return (R) result.toString();  // cast isn't necessary
        } else {
            throw new ContractCallException(
                    "Unable to convert response: " + value
                            + " to expected type: " + returnType.getSimpleName());
        }
    }

    protected List<Type> executeCallMultipleValueReturn(
            Function function) throws IOException {
        return executeCall(function);
    }

    protected TransactionReceipt executeTransaction(
            Function function)
            throws IOException, TransactionException {
        return executeTransaction(function, "0");
    }

    private TransactionReceipt executeTransaction(
            Function function, String weiValue)
            throws IOException, TransactionException {
        return executeTransaction(FunctionEncoder.encode(function), weiValue);
    }

    /**
     * Given the duration required to execute a transaction.
     *
     * @param data  to send in transaction
     * @param weiValue in Wei to send in transaction
     * @return {@link Optional} containing our transaction receipt
     * @throws IOException                 if the call to the node fails
     * @throws TransactionException if the transaction was not mined while waiting
     */
    TransactionReceipt executeTransaction(
            String data, String weiValue)
            throws TransactionException, IOException {

        return send(contractAddress, data, weiValue, gasPrice, gasLimit);
    }

    // adapt to cita
    TransactionReceipt executeTransaction(
            String data, long quota, String nonce, long validUntilBlock,
            int version , int chainId, String value)
            throws TransactionException, IOException {
        return sendAdaptToCita(
                contractAddress, data, quota, nonce, validUntilBlock, version, chainId, value);
    }

    protected <T extends Type> RemoteCall<T>
                executeRemoteCallSingleValueReturn(Function function) {
        return new RemoteCall<>(
                () -> executeCallSingleValueReturn(function));
    }

    protected <T> RemoteCall<T> executeRemoteCallSingleValueReturn(
            Function function, Class<T> returnType) {
        return new RemoteCall<>(
                () -> executeCallSingleValueReturn(function, returnType));
    }

    protected RemoteCall<List<Type>>
                executeRemoteCallMultipleValueReturn(Function function) {
        return new RemoteCall<>(
                () -> executeCallMultipleValueReturn(function));
    }

    protected RemoteCall<TransactionReceipt>
                executeRemoteCallTransaction(Function function) {
        return new RemoteCall<>(() -> executeTransaction(function));
    }

    protected RemoteCall<TransactionReceipt> executeRemoteCallTransaction(
            Function function, String weiValue) {
        return new RemoteCall<>(() -> executeTransaction(function, weiValue));
    }

    protected RemoteCall<TransactionReceipt> executeRemoteCallTransaction(
            Function function, long quota, String nonce,
            long validUntilBlock, int version,
            int chainId, String value) {
        return new RemoteCall<>(
                () -> executeTransaction(
                        FunctionEncoder.encode(function),
                        quota, nonce, validUntilBlock, version, chainId, value));
    }

    private static <T extends Contract> T create(
            T contract, String binary, String encodedConstructor, String value)
            throws IOException, TransactionException {
        TransactionReceipt transactionReceipt =
                contract.executeTransaction(binary + encodedConstructor, value);

        String contractAddress = transactionReceipt.getContractAddress();
        if (contractAddress == null) {
            throw new RuntimeException("Empty contract address returned");
        }
        contract.setContractAddress(contractAddress);
        contract.setTransactionReceipt(transactionReceipt);

        return contract;
    }

    private static <T extends Contract> T create(
            T contract, String binary, String encodedConstructor,
            long quota, String nonce, long validUntilBlock,
            int version, int chainId, String value)
            throws IOException, TransactionException {
        TransactionReceipt transactionReceipt =
                contract.executeTransaction(
                        binary + encodedConstructor, quota, nonce,
                        validUntilBlock, version, chainId, value);

        String contractAddress = transactionReceipt.getContractAddress();
        if (contractAddress == null) {
            throw new RuntimeException("Empty contract address returned");
        }
        contract.setContractAddress(contractAddress);
        contract.setTransactionReceipt(transactionReceipt);

        return contract;
    }

    protected static <T extends Contract> T deploy(
            Class<T> type,
            Nervosj nervosj, Credentials credentials,
            BigInteger gasPrice, BigInteger gasLimit,
            String binary, String encodedConstructor, String value) throws
            IOException, TransactionException {

        try {
            Constructor<T> constructor = type.getDeclaredConstructor(
                    String.class,
                    Nervosj.class, Credentials.class,
                    BigInteger.class, BigInteger.class);
            constructor.setAccessible(true);

            // we want to use null here to ensure that "to" parameter on message is not populated
            // We don't need to modify this,
            // because we must specify the CitaTransactionManager which used to send transaction
            T contract = constructor.newInstance(
                    null, nervosj, credentials, gasPrice, gasLimit);

            return create(contract, binary, encodedConstructor, value);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    protected static <T extends Contract> T deploy(
            Class<T> type,
            Nervosj nervosj, TransactionManager transactionManager,
            BigInteger gasPrice, BigInteger gasLimit,
            String binary, String encodedConstructor, String value)
            throws IOException, TransactionException {

        try {
            Constructor<T> constructor = type.getDeclaredConstructor(
                    String.class,
                    Nervosj.class, TransactionManager.class,
                    BigInteger.class, BigInteger.class);
            constructor.setAccessible(true);

            // we want to use null here to ensure that "to" parameter on message is not populated
            // Unfortunately, we need empty string(not null) that represent create contract
            T contract = constructor.newInstance(
                    "", nervosj, transactionManager, gasPrice, gasLimit);
            return create(contract, binary, encodedConstructor, value);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    protected static <T extends Contract> T deploy(
            Class<T> type,
            Nervosj nervosj, TransactionManager transactionManager,
            long quota, String nonce, long validUntilBlock,
            int version, String binary, int chainId,
            String value, String encodedConstructor)
            throws IOException, TransactionException {

        try {
            Constructor<T> constructor = type.getDeclaredConstructor(
                    String.class,
                    Nervosj.class, TransactionManager.class);
            constructor.setAccessible(true);

            // we want to use null here to ensure that "to" parameter on message is not populated
            // Unfortunately, we need empty string(not null) that represent create contract
            T contract = constructor.newInstance(
                    "", nervosj, transactionManager);
            return create(contract, binary, encodedConstructor, quota, nonce,
                    validUntilBlock, version, chainId, value);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    protected static <T extends Contract> RemoteCall<T>
                    deployRemoteCall(
            Class<T> type, Nervosj nervosj, TransactionManager transactionManager,
            long quota, String nonce, long validUntilBlock,
            int version, int chainId, String value,
            String binary, String encodedConstructor) {
        return new RemoteCall<>(() -> deploy(
                type, nervosj, transactionManager, quota, nonce, validUntilBlock,
                version, binary, chainId, value, encodedConstructor));
    }

    protected static <T extends Contract> RemoteCall<T> deployRemoteCall(
            Class<T> type,
            Nervosj nervosj, Credentials credentials,
            BigInteger gasPrice, BigInteger gasLimit,
            String binary, String encodedConstructor,
            String value) {
        return new RemoteCall<>(() -> deploy(
                type, nervosj, credentials, gasPrice, gasLimit, binary,
                encodedConstructor, value));
    }

    protected static <T extends Contract> RemoteCall<T> deployRemoteCall(
            Class<T> type,
            Nervosj nervosj, Credentials credentials,
            BigInteger gasPrice, BigInteger gasLimit,
            String binary, String encodedConstructor) {
        return deployRemoteCall(
                type, nervosj, credentials, gasPrice, gasLimit,
                binary, encodedConstructor, "0");
    }

    protected static <T extends Contract> RemoteCall<T> deployRemoteCall(
            Class<T> type,
            Nervosj nervosj, TransactionManager transactionManager,
            BigInteger gasPrice, BigInteger gasLimit,
            String binary, String encodedConstructor, String value) {
        return new RemoteCall<>(() -> deploy(
                type, nervosj, transactionManager, gasPrice, gasLimit, binary,
                encodedConstructor, value));
    }

    protected static <T extends Contract> RemoteCall<T> deployRemoteCall(
            Class<T> type,
            Nervosj nervosj, TransactionManager transactionManager,
            BigInteger gasPrice, BigInteger gasLimit,
            String binary, String encodedConstructor) {
        return deployRemoteCall(
                type, nervosj, transactionManager, gasPrice, gasLimit, binary,
                encodedConstructor, "0");
    }

    public static EventValues staticExtractEventParameters(
            Event event, Log log) {

        List<String> topics = log.getTopics();
        String encodedEventSignature = EventEncoder.encode(event);
        if (!topics.get(0).equals(encodedEventSignature)) {
            return null;
        }

        List<Type> indexedValues = new ArrayList<>();
        List<Type> nonIndexedValues = FunctionReturnDecoder.decode(
                log.getData(), event.getNonIndexedParameters());

        List<TypeReference<Type>> indexedParameters = event.getIndexedParameters();
        for (int i = 0; i < indexedParameters.size(); i++) {
            Type value = FunctionReturnDecoder.decodeIndexedValue(
                    topics.get(i + 1), indexedParameters.get(i));
            indexedValues.add(value);
        }
        return new EventValues(indexedValues, nonIndexedValues);
    }

    protected EventValues extractEventParameters(Event event, Log log) {
        return staticExtractEventParameters(event, log);
    }

    protected List<EventValues> extractEventParameters(
            Event event, TransactionReceipt transactionReceipt) {

        List<Log> logs = transactionReceipt.getLogs();
        List<EventValues> values = new ArrayList<>();
        for (Log log : logs) {
            EventValues eventValues = extractEventParameters(event, log);
            if (eventValues != null) {
                values.add(eventValues);
            }
        }

        return values;
    }

    /**
     * Subclasses should implement this method to return pre-existing addresses for deployed
     * contracts.
     *
     * @param networkId the network id, for example "1" for the main-net, "3" for ropsten, etc.
     * @return the deployed address of the contract, if known, and null otherwise.
     */
    protected String getStaticDeployedAddress(String networkId) {
        return null;
    }

    public final void setDeployedAddress(String networkId, String address) {
        if (deployedAddresses == null) {
            deployedAddresses = new HashMap<>();
        }
        deployedAddresses.put(networkId, address);
    }

    public final String getDeployedAddress(String networkId) {
        String addr = null;
        if (deployedAddresses != null) {
            addr = deployedAddresses.get(networkId);
        }
        return addr == null ? getStaticDeployedAddress(networkId) : addr;
    }

    @SuppressWarnings("unchecked")
    protected static <S extends Type, T>
            List<T> convertToNative(List<S> arr) {
        List<T> out = new ArrayList<T>();
        for (Iterator<S> it = arr.iterator(); it.hasNext(); ) {
            out.add((T)it.next().getValue());
        }
        return out;
    }

}
