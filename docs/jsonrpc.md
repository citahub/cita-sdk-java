## JSONRPC

CITAj 接口继承了 CITA 和 CITAjRx 两个接口，CITAj 的实现类（比如JsonRpc2_0CITAj），提供了方法以发送交易的方式对合约进行部署和函数调用。

* [Build CITAj](#build-citaj)
* [netPeer](#netpeer)
* [appMetaData](#appmetadata)
* [appBlockNumber](#appblocknumber)
* [appGetBalance](#appgetbalance)
* [appGetAbi](#appgetabi)
* [appGetTransactionCount](#appgettransactioncount)
* [appGetCode](#appgetcode)
* [appSendRawTransaction](#appsendrawtransaction)
* [appCall](#appcall)
* [appGetBlockByHash](#appgetblockbyhash)
* [appGetBlockByNumber](#appgetblockbynumber)
* [appGetTransactionByHash](#appgettransactionbyhash)
* [appGetTransactionReceipt](#appgettransactionreceipt)
* [appNewBlockFilter](#appnewblockfilter)
* [appBlockHashObservable](#appblockhashobservable)
* [appNewFilter](#appnewfilter)
* [appUninstallFilter](#appuninstallfilter)
* [appGetFilterChanges](#appgetfilterchanges)
* [appGetFilterLogs](#appgetfilterlogs)
* [appLogObservable](#applogobservable)

### Build CITAj

**方法名**
`CITAj build (CITAjService citaj)`
根据 CITAjService 类型实例化 CITAj。

**参数**
citaj - CITAjService 实例

**返回值**
CITAj 实例

**示例**
```
CITAj service = CITAj.build(new HttpService("http://127.0.0.1"));
```

### netPeer

**方法名**
`Request<?, NetPeerCount> netPeer()`
获取当前连接节点数。

**参数**
无

**返回值**
Request<?, NetPeerCount>

**示例**
```
CITAj service = CITAj.build(new HttpService("http://127.0.0.1"));
NetPeerCount netPeerCount = service.netPeerCount().send();
BigInteger peerCount = netPeerCount.getQuantity();
```

### appMetaData

**方法名**
`Request<?, AppMetaData> appMetaData(DefaultBlockParameter defaultBlockParameter)`
获取指定块高的元数据。

**参数**
* defaultBlockParameter - 块高度的接口：数字或者关键字

**返回值**
Request<?, AppMetaData>

**示例**
```
CITAj service = CITAj.build(new HttpService("http://127.0.0.1"));
DefaultBlockParameter defaultParam = DefaultBlockParameter.valueOf("latest");
AppMetaDataResult result = service.appMetaData(defaultParam).send();
int chainId = result.chainId;
String chainName = result.chainName;
String genesisTS = result.genesisTimestamp;
```
### appBlockNumber

**方法名**
`Request<?, AppBlockNumber> appBlockNumber()`
获取当前块高度。

**参数**
无

**返回值**
Request<?, AppBlockNumber>

**示例**
```
CITAj service = CITAj.build(new HttpService("http://127.0.0.1"));
AppBlockNumber result = service.appBlockNumber().send();
BigInteger blockNumber = result.getBlockNumber();
```
### appGetBalance

**方法名**
`Request<?, AppGetBalance> appGetBalance(String address, DefaultBlockParameter defaultBlockParameter))`
获取地址余额。

**参数**
* address - 所要查询的地址
* defaultBlockParameter - 块高度的接口：数字或者关键字

**返回值**
Request<?, AppGetBalance>

**示例**
```
CITAj service = CITAj.build(new HttpService("http://127.0.0.1"));
string addr = "{hex cita address starting with 0x}";
DefaultBlockParameter defaultBlockParameter = DefaultBlockParamter.valueOf("latest");
AppGetBalance getBalance = service.appGetBalance(addr, defaultBlockParamter).send();
BigInteger balance = getBalance.getBalance();
```

### appGetAbi

**方法名**
`Request<?, AppGetAbi> appGetAbi(String contractAddress, DefaultBlockParameter defaultBlockParameter)`
获取合约的Abi。

**参数**
* address - 所要查询Abi的合约地址
* defaultBlockParameter - 块高度的接口：数字或者关键字

**返回值**
Request<?, AppGetAbi>

**示例**
```
CITAj service = CITAj.build(new HttpService("http://127.0.0.1"));
string addr = "{hex cita address starting with 0x}";
DefaultBlockParameter defaultBlockParameter = DefaultBlockParamter.valueOf("latest");
AppGetAbi getAbi = service.appGetAbi(addr, defaultBlockParamter).send();
String abi = getAbi.getAbi();
```

### appGetTransactionCount

**方法名**
`Request<?, AppGetTransactionCount> appGetTransactionCount(String address, DefaultBlockParameter defaultBlockParameter)`
获取账户发送合约数量。

**参数**
* address - 所要查询的地址
* defaultBlockParameter - 块高度的接口：数字或者关键字

**返回值**
Request<?, AppGetTransactionCount>

**示例**
```
CITAj service = CITAj.build(new HttpService("http://127.0.0.1"));
string addr = "{hex cita address starting with 0x}";
DefaultBlockParameter defaultBlockParameter = DefaultBlockParamter.valueOf("latest");
AppGetTransactionCount getTransactionCount = service.appGetTransactionCount(addr, defaultBlockParamter).send();
BigInteger txCount = getTransactionCount.getTransactionCount();
```

### appGetCode

**方法名**
`Request<?, AppGetCode> appGetCode(String address, DefaultBlockParameter defaultBlockParameter)`
获取合约代码。

**参数**
* address - 所要查询的地址
* defaultBlockParameter - 块高度的接口：数字或者关键字

**返回值**
Request<?, AppGetCode>

**示例**
```
CITAj service = CITAj.build(new HttpService("http://127.0.0.1"));
string addr = "{hex cita address starting with 0x}";
DefaultBlockParameter defaultBlockParameter = DefaultBlockParamter.valueOf("latest");
AppGetCode getCode = service.appGetCode(addr, defaultBlockParamter).send();
Sring code = getCode.getCode();
```

### appSendRawTransaction

**方法名**
`Request<?, AppSendTransaction> appSendRawTransaction(String signedTransactionData)`
向区块链节点发送序列化交易。

**参数**
* signedTransaction - 经过签名的交易数据

**返回值**
Request<?, AppSendTransaction>

**示例**
```
//create a signed transaction
Transaction tx = Transaction.createContractTransaction(BigInteger.valueOf(nonce), this.config.getQuota(), this.currentHeight + 88, 0, chainId, value, this.config.getCode());
tx.sign(this.config.getPrivateKey(), false, false);

//instantiate a CITAj and send the transaction
CITAj service = CITAj.build(new HttpService("http://127.0.0.1"));
AppSendTransaction sendTransaction = service.appSendRawTransaction(tx).send();

//get hash of the transaction
String hash = sendTransaction.getHash();
```

### appCall

**方法名**
`Request<?, AppCall> appCall(Call call, DefaultBlockParameter defaultBlockParameter)`
调用合约接口。

**参数**
* call - 合约方法的call的封装
* defaultBlockParameter - 块高度的接口：数字或者关键字

**返回值**
Request<?, AppCall>

**示例**
```
CITAj service = CITAj.build(new HttpService("http://127.0.0.1"));
Call call = new Call(from, to, data);
AppCall appCall = service.appCall(call, DefaultBlockParameter.valueOf("latest")).send();
String result = call.getValue();
```

### appGetBlockByHash

**方法名**
`Request<?, AppBlock> appGetBlockByHash(String blockHash, boolean returnFullTransactionObjects)`
根据块的哈希值查询块信息。

**参数**
* blockHash - 块高度的接口：数字或者关键字
* returnFullTransactionObjects - 是否返回交易信息 (True: 返回详细交易列表| False: 只返回交易hash)

**返回值**
Request<?, AppBlock>

**示例**
```
CITAj service = CITAj.build(new HttpService("http://127.0.0.1"));
String blockHash = "{block hash to search}";
AppBlock appBlock = service.appGetBlockByHash(blockHash, false).send();
```

### appGetBlockByNumber

**方法名**
`Request<?, AppBlock> appGetBlockByNumber(DefaultBlockParameter defaultBlockParameter, boolean returnFullTransactionObjects)`
根据块高度查询块信息。

**参数**
* defaultBlockParameter - 块高度的接口：数字或者关键字
* returnFullTransactionObjects - 是否返回交易信息 (True: 返回详细交易列表| False: 只返回交易hash)

**返回值**
Request<?, AppBlock>

**示例**
```
CITAj service = CITAj.build(new HttpService("http://127.0.0.1"));
AppBlock appBlock = service.appGetBlockByHash(DefaultBlockParameter.valueOf("latest"), false).send();
```


### appGetTransactionByHash

**方法名**
`Request<?, AppTransaction> appGetTransactionByHash(String transactionHash)`
根据哈希查询交易信息。

**参数**
* transactionHash - 交易哈希

**返回值**
Request<?, AppTransaction>

**示例**
```
CITAj service = CITAj.build(new HttpService("http://127.0.0.1"));
String txHash = "{hash of transactino to be searched}";
AppTransaction responseTx = service.appGetTransactionByHash(txHash).send();
```
### appGetTransactionReceipt

**方法名**
`Request<?, AppGetTransactionReceipt> appGetTransactionReceipt(String transactionHash)`
根据交易哈希查询交易回执。

**参数**
* transactionHash - 交易哈希

**返回值**
Request<?, AppGetTransactionReceipt>

**示例**
```
CITAj service = CITAj.build(new HttpService("http://127.0.0.1"));
String txHash = "{hash of transactino to be searched}";
AppGetTransactionReceipt txReceipt = service.appGetTransactionReceipt(txHash).send();
```

### appNewBlockFilter

**方法名**
`Request<?, AppFilter> appNewBlockFilter()`
创建一个新的块过滤器，当有新的块写入时通知。

**参数**
无

**返回值**
Request<?, AppFilter>

**示例**
```
CITAj service = CITAj.build(new HttpService("http://127.0.0.1"));
AppFilter appFilter = service.appNewBlockFilter().send();
BigInteger filterId = appFilter.getFilterId();
```

### appBlockHashObservable

**方法名**
`Observable<String> appBlockHashObservable()`
新建一个Block Filter来监听新增块的哈希，返回一个Observable，可以用交互的形式来监听块高的变化。

**参数**
无

**返回值**
Observable<?, AppLog>

**示例**
```
CITAj service = CITAj.build(new HttpService("http://127.0.0.1"));
Observable blockFitlerObservable = service.appBlockHashObservable();
AppLog logs = service.appGetFilterLogs(filterId).send();
        blockFitlerObservable.subscribe(block -> {
            System.out.println(block.toString());
        });
```

### appNewFilter

**方法名**
`Request<?, AppFilter> appNewFilter(com.cryptape.cita.protocol.core.methods.request.AppFilter appFilter)`
创建一个新的Event过滤器以用来监听合约中的Event。

**参数**
* appFilter - 针对于 CITA 智能合约event的过滤器（定义在Request中的appFilter）

**返回值**
Request<?, AppFilter>

**示例**
```
CITAj service = CITAj.build(new HttpService("http://127.0.0.1"));
com.cryptape.cita.protocol.core.methods.request.AppFilter appFilter = new AppFilter(fromBlock, toBlock, addresses);
AppFilter appFilter = service.appNewFilter(txHash).send();
BigInteger filterId = appFilter.getFilterId();
```

### appUninstallFilter

**方法名**
`Request<?, AppUninstallFilter> appUninstallFilter(BigInteger filterId)`
移除已部署的过滤器。

**参数**
* filterId - 过滤器Id

**返回值**
Request<?, AppUninstallFilter>

**示例**
```
CITAj service = CITAj.build(new HttpService("http://127.0.0.1"));
BigInteger filterId = {your filter Id };
AppUninstallFilter uninstallFilter = service.appUninstallFilter(filterId).send();
```

### appGetFilterChanges

**方法名**
`Request<?, AppLog> appGetFilterChanges(BigInteger filterId)`
根据过滤器Id查询log，返回上一次查询之后的所有log。

**参数**
* filterId - 过滤器Id

**返回值**
Request<?, AppLog>

**示例**
```
CITAj service = CITAj.build(new HttpService("http://127.0.0.1"));
BigInteger filterId = {your filter Id };
AppLog logs = service.appGetFilterChanges(filterId).send();
List<LogResult> results = logs.getLogs();
```

### appGetFilterLogs

**方法名**
`Request<?, AppLog> appGetFilterLogs(BigInteger filterId)`
根据过滤器Id查询log，返回符合输入filter Id的所有log。

**参数**
* filterId - 过滤器Id

**返回值**
Request<?, AppLog>

**示例**
```
CITAj service = CITAj.build(new HttpService("http://127.0.0.1"));
BigInteger filterId = {your filter Id };
AppLog logs = service.appGetFilterLogs(filterId).send();
List<LogResult> results = logs.getLogs();
```

### appLogObservable

**方法名**
`Observable<Log> appLogObservable(AppFilter appFilter)`
根据AppFilter来安装一个新的Filter用以获取历史log和监听新的Log，返回一个Observable以交互的模式监听Log。

**参数**
* AppFilter - 过滤器可以由`appNewFilter`来新建

**返回值**
Observable<Log>

**示例**
```
Observable appLogObservable = service.appLogObservable(filter);
            Observable<String> reponse = appLogObservable.map(
                    (log) -> {
                        EventValues eventValues = staticExtractEventParameters(event, (Log)log);
                        return (String) eventValues.getIndexedValues().get(0).getValue();;
                    }
                    );

            reponse.subscribe(x -> {
                System.out.println(x);
            });
```