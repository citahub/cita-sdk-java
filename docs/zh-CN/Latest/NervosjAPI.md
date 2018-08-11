# Java API 文档
## 概述：
Nervosj是对于Nervos进行交互的Java SDK包，Nervosj使用JSON-RPC协议对Nervos网络节点发送包含方法名和参数的请求，请求可以是转账或者合约的部署和调用。
在Nervosj中，一共有三种方式实现和链上节点的交互：第一种方法是通过Nervosj定义的方法手动输入包括合约的Abi和Bin来进行操作。
第二种是通过codeGen工具将Solidity合约生成Java类，该Java类继承自Contract类并包含合约中定义的所有方法。相比于第一种方法的手动拼接参数，这个方式可以不用生成Abi和Bin文件而直接通过Java类中的方法来进行合约的部署和调用。  
第三种方法是通过封装在Account中的方法来构建并发送交易，Account会实例化TransacionManager，TransactionManager提供了异步和同步方式对合约进行部署和调用。
## 目录：
1. [Nervosj](zh-CN/Latest/Nervosj.md)
2. [Transaction](zh-CN/Latest/transaction.md)
3. [Account](zh-CN/Latest/Account.md)
