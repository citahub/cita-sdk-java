package com.cryptape.cita.tx;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.cryptape.cita.protocol.CITAj;
import com.cryptape.cita.protocol.core.DefaultBlockParameterName;
import com.cryptape.cita.protocol.core.RemoteCall;
import com.cryptape.cita.protocol.core.methods.request.Call;
import com.cryptape.cita.protocol.core.methods.response.AppCall;
import com.cryptape.cita.protocol.core.methods.response.AppGetCode;
import com.cryptape.cita.protocol.core.methods.response.Log;
import com.cryptape.cita.protocol.core.methods.response.TransactionReceipt;
import com.cryptape.cita.tx.exceptions.ContractCallException;
import com.cryptape.cita.abi.EventEncoder;
import com.cryptape.cita.abi.EventValues;
import com.cryptape.cita.abi.FunctionEncoder;
import com.cryptape.cita.abi.FunctionReturnDecoder;
import com.cryptape.cita.abi.TypeReference;
import com.cryptape.cita.abi.datatypes.Address;
import com.cryptape.cita.abi.datatypes.Event;
import com.cryptape.cita.abi.datatypes.Function;
import com.cryptape.cita.abi.datatypes.Type;
import com.cryptape.cita.protocol.exceptions.TransactionException;
import com.cryptape.cita.utils.Numeric;


/**
 * Solidity contract type abstraction for interacting with smart contracts via native Java types.
 */
@SuppressWarnings("WeakerAccess")
public abstract class Contract extends ManagedTransaction {

    // https://www.reddit.com/r/ethereum/comments/5g8ia6/attention_miners_we_recommend_raising_gas_limit/

    private static final String ABI_ADDRESS = "ffffffffffffffffffffffffffffffffff010001";

    protected final String contractBinary;
    protected String contractAddress;
    protected String nonce;
    protected long quota;
    protected TransactionReceipt transactionReceipt;
    protected Map<String, String> deployedAddresses;

    protected Contract(String contractBinary, String contractAddress,
                       CITAj citaj, TransactionManager transactionManager,
                       String nonce, long quota) {
        super(citaj, transactionManager);

        //this.contractAddress = ensResolver.resolve(contractAddress);

        this.contractAddress = contractAddress;
        this.contractBinary = contractBinary;
        this.nonce = nonce;
        this.quota = quota;
    }

    protected Contract(String contractBinary, String contractAddress,
                       CITAj citaj, TransactionManager transactionManager) {
        this(contractBinary, contractAddress, citaj,
                transactionManager, "", 0);
    }

    @Deprecated
    protected Contract(String contractAddress,
                       CITAj citaj, TransactionManager transactionManager) {
        this("", contractAddress, citaj,
                transactionManager, "", 0);
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
     * @throws IOException if unable to connect to citaj node
     */
    public boolean isValid() throws IOException {
        if (contractAddress.equals("")) {
            throw new UnsupportedOperationException(
                    "Contract binary not present, you will need to regenerate your smart "
                            + "contract wrapper with citaj v2.2.0+");
        }

        AppGetCode ethGetCode = citaj
                .appGetCode(contractAddress, DefaultBlockParameterName.PENDING)
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
    public TransactionReceipt getTransactionReceipt() {
        return transactionReceipt;
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
                citaj.appCall(
                        new Call(transactionManager.getFromAddress(),
                                contractAddress, encodedFunction),
                        DefaultBlockParameterName.PENDING).send();

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
     * @return  containing our transaction receipt
     * @throws IOException                 if the call to the node fails
     * @throws TransactionException if the transaction was not mined while waiting
     */
    TransactionReceipt executeTransaction(
            String data, String weiValue)
            throws TransactionException, IOException {

        return send(contractAddress, data, quota, nonce, 0, 1, BigInteger.ONE, weiValue);
    }

    TransactionReceipt executeTransaction(
            String data, long quota, String nonce, long validUntilBlock,
            int version , BigInteger chainId, String value)
            throws TransactionException, IOException {
        return send(
                contractAddress, data, quota, nonce, validUntilBlock, version, chainId, value);
    }

    TransactionReceipt uploadAbi(
        String abi, long quota, String nonce, long validUntilBlock,
        int version , BigInteger chainId, String value)
        throws TransactionException, IOException {
        String data = hex_remove_0x(contractAddress) + hex_remove_0x(bytesToHexStr(abi.getBytes()));
        return send(
            ABI_ADDRESS, data, quota, nonce, validUntilBlock,
            version, chainId, value);
    }

    private String hex_remove_0x(String hex) {
        if (hex.contains("0x")) {
            return hex.substring(2);
        }
        return hex;
    }

    private String bytesToHexStr(byte[] byteArr) {
        if (null == byteArr || byteArr.length < 1) {
            return "";
        }
        StringBuilder sb = new StringBuilder();
        for (byte t : byteArr) {
            if ((t & 0xF0) == 0) {
                sb.append("0");
            }
            sb.append(Integer.toHexString(t & 0xFF));
        }
        return sb.toString();
    }

    protected <T extends Type> RemoteCall<T> executeRemoteCallSingleValueReturn(Function function) {
        return new RemoteCall<>(
                () -> executeCallSingleValueReturn(function));
    }

    protected <T> RemoteCall<T> executeRemoteCallSingleValueReturn(
            Function function, Class<T> returnType) {
        return new RemoteCall<>(
                () -> executeCallSingleValueReturn(function, returnType));
    }

    protected RemoteCall<List<Type>> executeRemoteCallMultipleValueReturn(Function function) {
        return new RemoteCall<>(
                () -> executeCallMultipleValueReturn(function));
    }

    protected RemoteCall<TransactionReceipt> executeRemoteCallTransaction(Function function) {
        return new RemoteCall<>(() -> executeTransaction(function));
    }

    protected RemoteCall<TransactionReceipt> executeRemoteCallTransaction(
            Function function, String weiValue) {
        return new RemoteCall<>(() -> executeTransaction(function, weiValue));
    }

    protected RemoteCall<TransactionReceipt> executeRemoteCallTransaction(
            Function function, long quota, String nonce,
            long validUntilBlock, int version,
            BigInteger chainId, String value) {
        return new RemoteCall<>(
                () -> executeTransaction(
                        FunctionEncoder.encode(function),
                        quota, nonce, validUntilBlock, version, chainId, value));
    }

    protected RemoteCall<TransactionReceipt> executeUploadAbi(
        String abi, long quota, String nonce,
        long validUntilBlock, int version,
        BigInteger chainId, String value) {
        return new RemoteCall<>(
            () -> executeTransaction(
                abi, quota, nonce, validUntilBlock, version, chainId, value));
    }


    private static <T extends Contract> T create(
            T contract, String binary, String encodedConstructor,
            long quota, String nonce, long validUntilBlock,
            int version, BigInteger chainId, String value)
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

    private static <T extends Contract> T create(
        T contract, String binary, String encodedConstructor,
        long quota, String nonce, long validUntilBlock,
        int version, BigInteger chainId, String value, String abi)
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

        if(abi != null && abi.length() > 0) {
            contract.uploadAbi(abi, quota, nonce,
                validUntilBlock + 88, version, chainId, value);
        }

        return contract;
    }


    protected static <T extends Contract> T deploy(
        Class<T> type,
        CITAj citaj, TransactionManager transactionManager,
        long quota, String nonce, long validUntilBlock,
        int version, String binary, BigInteger chainId,
        String value, String encodedConstructor)
        throws IOException, TransactionException {

        try {
            Constructor<T> constructor = type.getDeclaredConstructor(
                String.class,
                CITAj.class, TransactionManager.class);
            constructor.setAccessible(true);

            // we want to use null here to ensure that "to" parameter on message is not populated
            // Unfortunately, we need empty string(not null) that represent create contract
            T contract = constructor.newInstance(
                "", citaj, transactionManager);
            return create(contract, binary, encodedConstructor, quota, nonce,
                validUntilBlock, version, chainId, value);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    protected static <T extends Contract> T deploy(
        Class<T> type,
        CITAj citaj, TransactionManager transactionManager,
        long quota, String nonce, long validUntilBlock,
        int version, String binary, BigInteger chainId,
        String value, String encodedConstructor, String abi)
        throws IOException, TransactionException {

        try {
            Constructor<T> constructor = type.getDeclaredConstructor(
                String.class,
                CITAj.class, TransactionManager.class);
            constructor.setAccessible(true);

            // we want to use null here to ensure that "to" parameter on message is not populated
            // Unfortunately, we need empty string(not null) that represent create contract
            T contract = constructor.newInstance(
                "", citaj, transactionManager);
            return create(contract, binary, encodedConstructor, quota, nonce,
                validUntilBlock, version, chainId, value, abi);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    protected static <T extends Contract> RemoteCall<T> deployRemoteCall(
        Class<T> type, CITAj citaj, TransactionManager transactionManager,
        long quota, String nonce, long validUntilBlock,
        int version, BigInteger chainId, String value,
        String binary, String encodedConstructor) {
        return new RemoteCall<>(() -> deploy(
            type, citaj, transactionManager, quota, nonce, validUntilBlock,
            version, binary, chainId, value, encodedConstructor));
    }

    protected static <T extends Contract> RemoteCall<T> deployRemoteCall(
        Class<T> type, CITAj citaj, TransactionManager transactionManager,
        long quota, String nonce, long validUntilBlock,
        int version, BigInteger chainId, String value,
        String binary, String encodedConstructor, String abi) {
        return new RemoteCall<>(() -> deploy(
            type, citaj, transactionManager, quota, nonce, validUntilBlock,
            version, binary, chainId, value, encodedConstructor, abi));
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
    protected static <S extends Type, T> List<T> convertToNative(List<S> arr) {
        List<T> out = new ArrayList<T>();
        for (Iterator<S> it = arr.iterator(); it.hasNext(); ) {
            out.add((T)it.next().getValue());
        }
        return out;
    }

}
