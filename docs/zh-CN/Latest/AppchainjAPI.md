# CITAj API 文档
## 概述：
CITAj 是对于 Nervos Appchain 进行交互的Java SDK包，Apchainj 使用 JSON-RP C协议对 Appchain 网络节点发送包含方法名和参数的请求，请求可以是转账或者合约的部署和调用。
在 CITAj 中，一共有三种方式实现和链上节点的交互：

第一种方法是通过 CITAj 定义的方法手动输入包括合约的 Abi 和 Bin 来进行操作。

第二种是通过 codeGen 工具将 Solidity 合约生成对应 Java 类，该 Java 类继承自 Contract 类并包含合约中定义的所有方法。相比于第一种方法的手动拼接参数，这个方式可以不用生成 Abi 和 Bin 文件而直接通过 Java 类中的方法来进行合约的部署和调用。

第三种方法是通过封装在 Account 中的方法来构建并发送交易，Account 会实例化 TransacionManager，TransactionManager 提供了异步和同步方式对合约进行部署和调用。
## 目录：
1. [CITAj](CITAj.md)
2. [Transaction](Transaction.md)
3. [Account](Account.md)
