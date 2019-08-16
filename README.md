# cita-sdk-java
[![Build Status](https://travis-ci.org/cryptape/cita-sdk-java.svg?branch=master)](https://travis-ci.org/cryptape/cita-sdk-java) 

[English](https://github.com/cryptape/cita-sdk-java#introduction)  
[中文](https://github.com/cryptape/cita-sdk-java#简介)

## Introduction

cita-sdk-java, originally adapted from Ethereum web3j, is a Java library for working with Smart Contract and integrating with clients on CITA.

For detailed documentation, see [documentation](docs/index.md).

## Features

- Complete implementation of CITA JSON-RPC API over HTTP.
- Auto-generation of Java smart contract wrappers to create, deploy, transact with and call smart contracts from native Java code (Solidity and Truffle definition formats supported).
- Android compatible. 

## Getting Started

### Prerequisites
Java 8  
Gradle 5.0  

### Install
Install from repositories:  
maven  
```
<dependency>
  <groupId>com.cryptape.cita</groupId>
  <artifactId>core</artifactId>
  <version>0.24.1</version>
</dependency>
```
Gradle
```
compile 'com.cryptape.cita:core:0.24.1'
```

Install manually
If you want to generate the jar and import manually.

> Because uploading jar package to maven server and packing jar package locally through `shadowJar` command have conflicts, you 
> should add `apply plugin: 'com.github.johnrengelman.shadow'` of `console/bulid.gradle` when packing locally.(It's annotated by default)

```
git clone https://github.com/cryptape/cita-sdk-java.git
gradle shadowJar
```


### CITA Test net

Use CITA test net (recommended):
https://node.cryptape.com is provided as a test net.

Or build your own CITA net:
Please find more information in [CITA](https://github.com/cryptape/cita).

### Get started

#### Deploy smart contract

Similar as Ethereum, smart contracts are deployed in CITA network by sending transactions. CITA transaction is defined in [Transaction.java](https://github.com/cryptape/cita-sdk-java/blob/master/core/src/main/java/com/cryptape/cita/protocol/core/methods/request/Transaction.java).  
In CITA transaction, there are 3 special parameters:
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
Random random = new Random(System.currentTimeMillis());
String nonce = String.valueOf(Math.abs(random.nextLong()));
long quota = 1000000;
Transaction tx = Transaction.createContractTransaction(nonce, quota, validUntilBlock, contractCode);
```

Sign the transaction with sender's private key and send it to CITA net.
```java
String rawTx = tx.sign(privateKey);
CITAj service = CITAj.build(new HttpService(ipAddr + ":" + port));
AppSendTransaction result = service.appSendRawTransaction(rawTx).send();
```
Please be attention that all transactions need to be signed since CITA only supports method `sendRawTransaction` rather than `sendTransaction`.

#### Call functions in smart contract
In CITA smart contract call, like contract deployment, a transaction needs to be created with 2 more parameters:
- contract address: address of the deployed contract.
- functionCallData: ABI of function and parameter.

After contract deployed, contract address can be fetched from TransactionReceipt. `functionCallData` is encoded from functionName and parameters by contract ABI. For example, `functionCallData` of function `set()` with parameter 1 is `60fe47b10000000000000000000000000000000000000000000000000000000000000001`.
```java
//get receipt and address from transaction
String txHash = result.getSendTransactionResult().getHash();
TransactionReceipt txReceipt = service.appGetTransactionReceipt(txHash).send().getTransactionReceipt();
String contractAddress = txReceipt.getContractAddress();

//sign and send the transaction
Transaction tx = Transaction.createFunctionCallTransaction(contractAddress, nonce, quota, validUntilBlock, functionCallData);
String rawTx = tx.sign(privateKey);
String txHash =  service.appSendRawTransaction(rawTx).send().getSendTransactionResult().getHash();
```
Please check [TokenTransactionExample.java](https://github.com/cryptape/cita-sdk-java/blob/master/tests/src/main/java/com/cryptape/cita/tests/TokenTransactionExample.java) to see a complete example for smart contract deployment and function invocation.

### Working with smart contract with cita-sdk-java wrapper
Besides interacting with smart contracts by sending transactions with binary code, cita-sdk-java provides a tool to help to convert solidity contract to a Java class from which smart contracts can be deployed and called.

Download cita jar file from release page or run `gradle shadowJar` to generate jars so that the tool can be found under `console/build/libs`. Name of the tool is `console-version-all.jar`.

Usage of console-version-all is shown below:
```shell
$ java -jar console-0.17-all.jar solidity generate [--javaTypes|--solidityTypes] /path/to/{smart-contract}.bin /path/to/{smart-contract}.abi -o /path/to/src/main/java -p {package-path}
```
Example generate Java class from `Token.sol`, `Token.bin` and `Token.abi` under `/tests/src/main/resources`:
```shell
java -jar console/build/libs/console-0.17-all.jar solidity generate tests/src/main/resources/Token.bin tests/src/main/resources/Token.abi -o tests/src/main/java/ -p com.cryptape.cita.tests
```
`Token.java` will be created from commands above and class `Token` can be used with TransactionManager to deploy and call smart contract `Token`. Please be attention that [TransactionManager](https://github.com/cryptape/cita-sdk-java/blob/master/core/src/main/java/com/cryptape/cita/tx/TransactionManager.java) is supposed to be used as TransactionManager for transaction creation in CITA network.
Please check [TokenCodegenExample.java](https://github.com/cryptape/cita-sdk-java/blob/master/tests/src/main/java/com/cryptape/cita/tests/TokenCodegenExample.java) for a complete example.

### Working with smart contract with cita-sdk-java Account (Test)
cita-sdk-java provides interface [Account](https://github.com/cryptape/cita-sdk-java/blob/master/core/src/main/java/com/cryptape/cita/protocol/account/Account.java) for smart contract manipulations. With parameters of smart contract's name, address, method and method's arguments, smart contracts can be deployed and called through the interface without exposing extra java, bin or abi file to developers.

Method of smart contract deployment:
```java
// Deploy contract in sync and async way.
public AppSendTransaction deploy(File contractFile, String nonce, BigInteger quota)

public Future<AppSendTransaction> deployAsync(File contractFile, String nonce, BigInteger quota)
```
Method of smart contract method call:
```java
public Object callContract(String contractAddress, String funcName, String nonce, BigInteger quota, Object... args)

//function is a encapsulation of method including name, argument datatypes, return type and other info.
public Object callContract(String contractAddress, AbiDefinition functionAbi, String nonce, BigInteger quota, Object... args)
```
While contract file is required when first deploy the contract, cita-sdk-java can get the abi file according to address when call methods in deployed contract.
Please find complete code in [TokenAccountExample](https://github.com/cryptape/cita-sdk-java/blob/master/tests/src/main/java/com/cryptape/cita/tests/TokenAccountExample.java).


## 简介
cita-sdk-java 是对以太坊 Web3j 进行改写，适配 CITA 的一个 Java 开发包。cita-sdk-java 集成了与 CITA 客户端交互的功能，可以用来对 CITA 发送交易，系统配置，信息查询。

开发请参考[详细文档](docs/index.md)。

## 特性
- 通过 HTTP 协议，实现了 CITA 所定义的所有 JSON-RPC 方法。
- 可以通过 Solidity 智能合约生成该合约的 Java 类。这个智能合约的 Java 类作为 java 对智能合约的包裹层，可以使开发和通过 java 方便地对智能合约进行部署和合约方法的调用（支持Solidity 和 Truffle 的格式）。
- 适配安卓

## 开始

### 预装组件
Java 8  
Gradle 5.0

### 安装
通过远程仓库安装：  
```
<dependency>
  <groupId>com.cryptape.cita</groupId>
  <artifactId>core</artifactId>
  <version>0.24.1</version>
</dependency>
```
Gradle
```
compile 'com.cryptape.cita:core:0.24.1'
```

手动安装  
如果你想使用最新的 CITA，编译 CITA 生成 jar 包，并手动引入。

> 由于上传 jar 包至 maven server 和本地通过 shadow 命令打包存在冲突，所以当你要在本地通过 shadowJar 命令打包时，
> 你需要添加和保留 `console/bulid.gradle` 文件下第一行 `apply plugin: 'com.github.johnrengelman.shadow'`（改行默认是被注释的）。

```
git clone https://github.com/cryptape/cita-sdk-java.git
gradle shadowJar
```


### CITA 测试网络
使用 CITA 测试网络（推荐）：  
https://node.cryptape.com

或者可以部署你自己的 CITA：  
如果需要了解怎么部署 CITA 网络，请查阅[CITA](https://github.com/cryptape/cita)。

### 快速教程
#### 部署智能合约
与以太坊类似，智能合约是通过发送交易来部署的。CITA 交易对象定义在 [Transaction.java](https://github.com/cryptape/cita-sdk-java/blob/master/core/src/main/java/com/cryptape/cita/protocol/core/methods/request/Transaction.java)。
在 CITA 交易中，有三个特殊的参数：
- nonce： 随机数或者通过特定的逻辑生成的随机信息，nonce是为了避免重放攻击。
- quota： 交易执行费用，也就是矿工费，就像以太坊中的 gasPrice * gasLimit。
- valid_until_block： 超时机制，valid_until_block 可以定义的范围是 (currentHeight, currentHeight + 100]。交易在`valid_until_block`之后会作废。

以下是一个智能合约部署的例子。

通过 solc 生成智能合约的二进制文件，命令如下：
```shell
$solc example.sol --bin
```

根据生成的二进制文件和其他3个参数构造一个交易，代码如下：
```java
long currentHeight = currentBlockNumber();
long validUntilBlock = currentHeight + 80;
Random random = new Random(System.currentTimeMillis());
String nonce = String.valueOf(Math.abs(random.nextLong()));
long quota = 1000000;
Transaction tx = Transaction.createContractTransaction(nonce, quota, validUntilBlock, contractCode);
```

用发送者的私钥对交易进行签名然后发送到 CITA 网络，代码如下：
```java
String rawTx = tx.sign(privateKey);
CITAj service = CITAj.build(new HttpService(ipAddr + ":" + port));
AppSendTransaction result = service.appSendRawTransaction(rawTx).send();
```
请注意因为 CITA 只支持 `sendRawTransaction` 方法而不是 `sendTransaction` ，所以所有发送给 CITA 的交易都需要被签名。

#### 调用智能合约的函数
在 CITA 中，正如智能合约的部署，智能合约中函数的调用也是通过发送交易来实现的，调用合约函数的交易是通过两个参数构造的：
- 合约地址： 已部署合约的地址。
- 函数编码数据： 函数以及入参的 ABI 的编码后数据。

智能合约成功部署以后，可以通过交易回执得到合约地址。以下是调用合约函数的例子，在例子中，`functionCallData`  通过对合约 ABI 中的函数名和入参编码得到。入参为 1 的`set()` 函数的编码数据 `functionCallData` 是 `60fe47b10000000000000000000000000000000000000000000000000000000000000001`.
```java
//得到回执和回执中的合约部署地址
String txHash = result.getSendTransactionResult().getHash();
TransactionReceipt txReceipt = service.appGetTransactionReceipt(txHash).send().getTransactionReceipt();
String contractAddress = txReceipt.getContractAddress();

//对交易签名并且发送
Transaction tx = Transaction.createFunctionCallTransaction(contractAddress, nonce, quota, validUntilBlock, functionCallData);
String rawTx = tx.sign(privateKey);
String txHash =  service.appSendRawTransaction(rawTx).send().getSendTransactionResult().getHash();
```
请在 [TokenTransactionExample.java](https://github.com/cryptape/cita-sdk-java/blob/master/tests/src/main/java/com/cryptape/cita/tests/TokenTransactionExample.java) 中查看完整代码。

### 通过 cita-sdk-java 中的 wrapper 与智能合约交互
以上例子展示了直接通过合约二进制码和函数的编码构造交易，并且发送与链上合约进行交互。除此方法以外，cita-sdk-java 提供了 codeGen 工具可以通过 solidity 合约生成 java 类。通过 cita-sdk-java 生成的 java 类，可以方便对合约进行部署和函数调用。

在 release 页面下载 cita-sdk-java 的 jar 包，或者在源项目中运行 `gradle shadowJar` 生成 jar 包，jar包会在 `console/build/libs` 中生成，名字是 `console-version-all.jar`。

solidity 合约转化为 java 类操作如下：
```shell
$ java -jar console-0.17-all.jar solidity generate [--javaTypes|--solidityTypes] /path/to/{smart-contract}.bin /path/to/{smart-contract}.abi -o /path/to/src/main/java -p {package-path}
```
这个例子通过 `Token.sol`, `Token.bin` and `Token.abi` 这三个文件在  `tests/src/main/resources` 生成对应的 java 类，命令如下：
```
java -jar console/build/libs/console-0.17-all.jar solidity generate tests/src/main/resources/Token.bin tests/src/main/resources/Token.abi -o tests/src/main/java/ -p com.cryptape.cita.tests
```
`Token.java` 会通过以上命令生成， `Token` 可以与 `TransactionManager` 一起使用来和 Token 合约交互。请注意在 CITA 中应该使用 [TransactionManager](https://github.com/cryptape/cita-sdk-java/blob/master/core/src/main/java/com/cryptape/cita/tx/TransactionManager.java) 而不是 TransactionManager。
请在 [TokenCodegenExample.java](https://github.com/cryptape/cita-sdk-java/blob/master/tests/src/main/java/com/cryptape/cita/tests/TokenCodegenExample.java) 查看完整代码.

### 通过 CITAj 中的 Account 与智能合约交互（测试阶段）
cita-sdk-java 还提供了接口 [Account](https://github.com/cryptape/cita-sdk-java/blob/master/core/src/main/java/com/cryptape/cita/protocol/account/Account.java) 与智能合约交互。 通过智能合约的名字，地址，函数名和函数入参，Account 可以进行合约的部署和合约函数的调用。通过 Account 这个方式，开发者无需进行合约二进制文件和 abi 细节处理。

合约部署示例代码：
```java
// Deploy contract in sync and async way.
public AppSendTransaction deploy(File contractFile, String nonce, BigInteger quota)

public Future<AppSendTransaction> deployAsync(File contractFile, String nonce, BigInteger quota)
```
合约函数调用示例代码：
```java
public Object callContract(String contractAddress, String funcName, String nonce, BigInteger quota, Object... args)

//function is a encapsulation of method including name, argument datatypes, return type and other info.
public Object callContract(String contractAddress, AbiDefinition functionAbi, String nonce, BigInteger quota, Object... args)
```
虽然在第一次部署合约的时候需要提供合约文件，但是在以后调用合约函数的时候 cita-sdk-java 通过 CITA 提供的 getAbi 接口根据合约地址得到对应的 abi。  
请在 [TokenAccountExample](https://github.com/cryptape/cita-sdk-java/blob/master/tests/src/main/java/com/cryptape/cita/tests/TokenAccountExample.java) 中查看完整代码。
