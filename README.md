# nervosj
[![Build Status](https://travis-ci.org/cryptape/nervosj.svg?branch=android)](https://travis-ci.org/cryptape/nervosj)
## Introduction
nervosj, originally adapted from Ethereum Web3j, is a Java library for working with Smart Contract and integrating with clients on Nervos network.
## Features
- Complete implementation of Nervos JSON-RPC API over HTTP.
- Auto-generation of Java smart contract wrappers to create, deploy, transact with and call smart contracts from native Java code (Solidity and Truffle definition formats supported).
- Comprehensive integration tests demonstrating a number of the above scenarios
- Android compatible

## Getting Started

### Prerequisites
Java 8  
Gradle 4.3  

### Install
`git clone https://github.com/cryptape/nervosj.git`  
`gradle shadowJar` to generate a jar file for nervosj.  
### Test net
Use Nervos AppChain test net (recommended):
http://121.196.200.225:1337 is provided as a testnet for tests.  

Or build your own Nervos AppChain net:  
Please find more information in [how to set up client in your local](https://docs.nervos.org/Nervos-AppChain-Docs/#/quick-start/deploy-appchain).  

### Get started
#### Deploy smart contract
Similar as Ethereum, smart contracts are deployed in Nervos AppChain network by sending transactions. Nervos AppChain transaction is defined in [Transaction.java](https://github.com/cryptape/nervosj/blob/master/core/src/main/java/org/nervosj/protocol/core/methods/request/Transaction.java).  
In Nervos transaction, there are 3 special parameters:  
- nonce: generated randomly or depend on specific logic to avoid replay attack.
- quota: transaction execution fee for operation, like gasPrice * gasLimit in Ethereum.
- valid_until_block: timeout mechanism which should be set in (currentHeight, currentHeight + 100]. Transaction will be discarded beyond `valid_until_block`.

Please see the example below for a smart contract deployment.  

Generate binary file for the contract by below command:  
```shell
$solc example.sol --bin
```

Construct a transaction with generated binary code and other 3 parameters.  
```java
long currentHeight = currentBlockNumber();
long validUntilBlock = currentHeight + 80;
BigInteger nonce = BigInteger.valueOf(Math.abs(random.nextLong()));
long quota = 1000000;
Transaction tx = Transaction.createContractTransaction(nonce, quota, validUntilBlock, contractCode);
```

Sign the transaction with sender's private key and send it to Nervos AppChain net.
```java
String rawTx = tx.sign(privateKey);
Nervosj service = Nervosj.build(new HttpService(ipAddr + ":" + port));
EthSendTransaction result = service.appSendRawTransaction(rawTx).send();
```
Please be attention that all transactions need to be signed since Nervos AppChain only supports method `sendRawTransaction` rather than `sendTransaction`.  

#### Call functions in smart contract
In Nervos AppChain smart contract call, like contract deployment, a transaction needs to be created with 2 more parameters:
- contract address: address of the deployed contract.
- functionCallData: ABI of function and parameter.

After contract deployed, contract address can be fetched from TransactionReceipt. `functionCallData` is encoded from functionName and parameters by contract ABI. For example, `functionCallData` of function `set()` with parameter 1 is `60fe47b10000000000000000000000000000000000000000000000000000000000000001`.  
```java
//get receipt and address from transaction
String txHash = result.getSendTransactionResult().getHash();
TransactionReceipt txReceipt = service.appGetTransactionReceipt(txHash).send().getTransactionReceipt().get();
String contractAddress = txReceipt.getContractAddress();

//sign and send the transaction
Transaction tx = Transaction.createFunctionCallTransaction(contractAddress, nonce, quota, validUntilBlock, functionCallData);
String rawTx = tx.sign(privateKey);
String txHash =  service.ethSendRawTransaction(rawTx).send().getSendTransactionResult().getHash();
```
Please check [TokenTransactionTest.java](https://github.com/cryptape/nervosj/blob/master/examples/src/main/java/org/nervos/appchain/tests/TokenTransactionTest.java) to see a complete example for smart contract deployment and function invocation.  

### Working with smart contract with nervosj wrapper
Besides interacting with smart contracts by sending transactions with binary code, nervosj provides a tool to help to convert solidity contract to a Java class from which smart contracts can be deployed and called.  

Run `gradle shadowJar` to generate jars so that the tool can be found under `console/build/libs`. Name of the tool is `console-version-all.jar`.  

Usage of console-version-all is shown below:  
```shell
$ java -jar console-0.17-all.jar solidity generate [--javaTypes|--solidityTypes] /path/to/{smart-contract}.bin /path/to/{smart-contract}.abi -o /path/to/src/main/java -p {package-path}
```
Example generate Java class from `Token.sol`, `Token.bin` and `Token.abi` under `nervosj/tests/src/main/resources`:  
```shell
java -jar console/build/libs/console-0.17-all.jar solidity generate tests/src/main/resources/Token.bin tests/src/main/resources/Token.abi -o tests/src/main/java/ -p org.nervos.appchain.tests
```
`Token.java` will be created from commands above and class `Token` can be used with CitaTransactionManager to deploy and call smart contract `Token`. Please be attention that [CitaTransactionManager](https://github.com/cryptape/nervoj/blob/master/core/src/main/java/org/nervos/appchain/tx/CitaTransactionManager.java) is supposed to be used as TransactionManager for transaction creation in Nervos AppChain network.  
Please check [TokenCodegenTest.java](https://github.com/cryptape/nervosj/blob/master/benchmark/src/main/java/org/nervos/appchain/tests/TokenCodegenTest.java) for a complete example.  

### Working with smart contract with nervosj Account
nervosj provides interface [Account](https://github.com/cryptape/nervosj/blob/master/core/src/main/java/org/nervos/appchain/protocol/account/Account.java) for smart contract manipulations. With parameters of smart contract's name, address, method and method's arguments, smart contracts can be deployed and called through the interface without exposing extra java, bin or abi file to developers.  

Method of smart contract deployment:  
```java
// Deploy contract in sync and async way.
public EthSendTransaction deploy(File contractFile, BigInteger nonce, BigInteger quota)

public CompletableFuture<EthSendTransaction> deployAsync(File contractFile, BigInteger nonce, BigInteger quota)
```
Method of smart contract method call:
```java
public Object callContract(String contractAddress, String funcName, BigInteger nonce, BigInteger quota, Object... args)

//function is a encapsulation of method including name, argument datatypes, return type and other info.
public Object callContract(String contractAddress, AbiDefinition functionAbi, BigInteger nonce, BigInteger quota, Object... args)
```
While contract file is required when first deploy the contract, nervosj can get the abi file according to address when call methods in deployed contract.  
Please find complete code in [TokenAccountTest](https://github.com/cryptape/nervosj/blob/master/tests/src/main/java/org/nervos/appchain/tests/TokenAccountTest.java).
