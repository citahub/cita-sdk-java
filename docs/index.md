# cita-sdk-java API 文档

## 概述：

cita-sdk-java 是对于 CITA 进行交互的 Java SDK 包，cita-sdk-java 使用 JSON-RPC 协议对 CITA 网络节点发送包含方法名和参数的请求，请求可以是转账或者合约的部署和调用。

在 cita-sdk-java 中，一共有三种方式实现和链上节点的交互：

第一种: 通过 cita-sdk-java 定义的方法手动输入包括合约的Abi和Bin来进行操作。

第二种: 通过codeGen工具将Solidity合约生成Java类，该Java类继承自Contract类并包含合约中定义的所有方法。
相比于第一种方法的手动拼接参数，这个方式可以不用生成Abi和Bin文件而直接通过Java类中的方法来进行合约的部署和调用。  

第三种: 通过封装在Account中的方法来构建并发送交易，Account会实例化TransactionManager，TransactionManager 提供了异步和同步方式对合约进行部署和调用。

## 目录：

1. [JSON-RPC](jsonrpc.md)
2. [Transaction](transaction.md)
3. [Account](account.md)