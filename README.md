# nervosj

## Introduction
`Nervos Web3j`, adaptated from `Ethereum Web3j`,  is a Java and Android library for working with Smart Contract and integrating with clients on Nervos network.  
## Features
- Complete implementation of Nervos JSON-RPC API over HTTP.    
- Auto-generation of Java smart contract wrappers to create, deploy, transact with and call smart contracts from native Java code (Solidity and Truffle definition formats supported).
- Comprehensive integration tests demonstrating a number of the above scenarios
- Android compatible

## Getting Started

### Prerequisites
java 1.8  
gradle 4.3

### Install
`git clone https://github.com/cryptape/nervosj.git`

### Test net
Use Nervos test net (recommended):  
http://121.196.200.225:1337 is provided as a testnet for test.

Or build your own test net:  
please find more information in [how to set up client in your local](https://fake_url/add_later).

### Nervos transactions
#### Deploy smart contract with transaction
Similar as Ethereum, smart contracts are deployed in Nervos network by sending transactions. Nervos transaction is defined in [Transaction.java](https://github.com/cryptape/nervosj/blob/master/core/src/main/java/org/nervosj/protocol/core/methods/request/Transaction.java).
In Nervos transaction, there are 3 special parameters:  
- nonce: can be generated randomly and depend on specific logic.
- quota: execution fee for operation, like gas in Ethereum.
- valid_util_block: timeout mechanism which should be set in (currentHeight, currentHeight + 100].  

Please see the example below for smart contract deployment.  
Sample contract in solidity:
```
pragma solidity ^0.4.14;

contract SimpleStorage {
    uint storedData;

    function set(uint x) {
        storedData = x;
    }

    function get() constant returns (uint) {
        return storedData;
    }
}
```
Generate binary file for the contract:
```
$solc test_example.sol --bin
```
Construct a transaction with generated binary code and other 3 parameters.
```
long currentHeight = currentBlockNumber();
long validUntilBlock = currentHeight + 80;
BigInteger nonce = BigInteger.valueOf(Math.abs(random.nextLong()));
long quota = 1000000;
Transaction tx = Transaction.createContractTransaction(nonce, quota, validUntilBlock, contractCode);
```
Sign the transaction with sender's private key and send it to test net. 
```
String privateKey = "352416e1c910e413768c51390dfd791b414212b7b4fe6b1a18f58007fa894214";
String rawTx = tx.sign(privateKey);
Web3j service = Web3j.build(new HttpService("http://127.0.0.1:1337"));
EthSendTransaction result = service.ethSendRawTransaction(rawTx).send();
```
Please be attention that all transactions need to be signed since Nervos only supports method `sendRawTransaction` rather than `sendTransaction`.  

Please check [SendTransactionDemo.java](https://github.com/cryptape/nervosj/blob/master/examples/src/main/java/org/nervosj/examples/SendTransactionDemo.java) for complete codes.
#### Call smart contract with transaction
In Nervos smart contract call, like contract deployment, a transaction needs to be created with 2 more parameters:
- contract address: address of the deployed contract.
- functionCallData: ABI of function and parameter.  

After contract deployed, contract address can be fetched from [Receipt](https://github.com/ethereum/wiki/wiki/JSON-RPC#eth_gettransactionreceipt). `functionCallData` is encoded from functionName and parameters by [contract ABI](https://github.com/ethereum/wiki/wiki/Ethereum-Contract-ABI). For example, ABI of function `set()` with parameter 1 is `60fe47b10000000000000000000000000000000000000000000000000000000000000001`.
```
String functionCallData = "60fe47b10000000000000000000000000000000000000000000000000000000000000001";
String privateKey = "352416e1c910e413768c51390dfd791b414212b7b4fe6b1a18f58007fa894214";

//get receipt and address from transaction
String txHash = result.getSendTransactionResult().getHash();
TransactionReceipt txReceipt = service.ethGetTransactionReceipt(txHash).send().getTransactionReceipt().get();
String contractAddress = txReceipt.getContractAddress();

// validUntilBlock should between currentHeight and currentHeight+100
long currentHeight = currentBlockNumber();
long validUntilBlock = currentHeight + 80;

//set nonce 
BigInteger nonce = BigInteger.valueOf(Math.abs(random.nextLong()));
long quota = 1000000;

//sign and send the transaction
Transaction tx = Transaction.createFunctionCallTransaction(contractAddress, nonce, quota, validUntilBlock, functionCallData);
String rawTx = tx.sign(privateKey);
String txHash =  service.ethSendRawTransaction(rawTx).send().getSendTransactionResult().getHash();
```
Please check [SendTransactionDemo.java](https://github.com/cryptape/nervosj/blob/master/examples/src/main/java/org/nervosj/examples/SendTransactionDemo.java) to see complete codes.

### Working with Nervos smart contract with nervosj wrapper
Nervos nervosj provides a tool to help to convert solidity contract to a java class from which smart contracts can be deployed and called. Run `gradle shadowJar` to generate jars so that the tool can be found under `console/build/libs`. Name of the tool is `console-version-all.jar` (current version is 3.2.0).
Usage of console-version-all is shown below:  
```
$ java -jar console-3.2.0-all.jar solidity generate [--javaTypes|--solidityTypes] /path/to/<smart-contract>.bin /path/to/<smart-contract>.abi -o /path/to/src/main/java -p com.your.organisation.name
```  
Example for `Token.sol`, `Token.bin` and `Token.abi` under `/home/qingyangkong/Environment/cryptape/nervosj/benchmark/src/main/resources`:
```
java -jar console/build/libs/console-3.2.0-all.jar solidity generate benchmark/src/main/resources/Token.bin benchmark/src/main/resources/Token.abi -o benchmark/src/main/java/ -p org.nervosj.benchmark
```  
`Token.java` will be created from commands above and class `Token` can be used with CitaTransactionManager to deploy and call smart contracts. Parameters `quota`, `nonce` and `invalidUtilBlock`, as talked before, must be provided when create new transactions. Please be attention that [CitaTransactionManager](https://github.com/cryptape/nervosj/blob/master/core/src/main/java/org/nervosj/tx/CitaTransactionManager.java) is supposed to be used as TransactionManager for transaction creation in Nervos network.
Please check [TokenTest.java](https://github.com/cryptape/nervosj/blob/master/benchmark/src/main/java/org/nervosj/benchmark/TokenTest.java) for complete codes.
### Use general nervosj methods to directly compile, deploy and call smart conrtact
nervosj provides interface [Account](https://github.com/cryptape/nervosj/blob/master/core/src/main/java/org/nervosj/protocol/account/Account.java) for smart contract manipulations. With parameters of smart contract's name, address, method and method's arguments, smart contracts can be deployed and called through the interface without generating extra java, bin or abi file.
Method of smart contract deployment:  
```
// Deploy contract in sync and async way.
public EthSendTransaction deploy(File contractFile, BigInteger nonce, BigInteger quota)

public CompletableFuture<EthSendTransaction> deployAsync(File contractFile, BigInteger nonce, BigInteger quota)
```
Method of smart contract method call:  
```
public Object callContract(String contractAddress, String funcName, BigInteger nonce, BigInteger quota, Object... args)

//function is a encapsulation of method including name, argument datatypes, return type and other info.
public Object callContract(String contractAddress, AbiDefinition functionAbi, BigInteger nonce, BigInteger quota, Object... args)
```
While contract file is required when first deploy the contract, nervosj can get the abi file according to address when call methods in deployed contract.
Please find complete code in [TokenTest](https://github.com/cryptape/nervosj/blob/master/tests/src/main/java/org/nervosj/tests/TokenTest.java).
