web3j接口继承了Ethereum和web3jRx两个接口，web3j的实现类（比如JsonRpc2_0Web3j），提供了方法以发送交易的方式对合约进行部署和函数调用。web3中没有提供将solidity合约转换为java类的方法，所以对合约的操作必须依赖合约或者合约函数的二进制码，即手动拼接参数。  

[build](Nervosj?id=nervosj-build-nervosjservice-nervosj)  
[netPeer](Nervosj?id=requestlt-netpeercountgt-netpeer)  
[appMetaData](Nervosj?id=requestlt-appmetadatagt-appmetadatadefaultblockparameter-defaultblockparameter)  
[appBlockNumber](Nervosj?id=requestlt-appblocknumbergt-appblocknumber)  
[appGetBalance](Nervosj?id=requestlt-appgetbalancegt-appgetbalancestring-address-defaultblockparameter-defaultblockparameter)  
[appGetAbi](Nervosj?id=requestlt-appgetabigt-appgetabistring-contractaddress-defaultblockparameter-defaultblockparameter)  
[appGetTransactionCount](Nervosj?id=requestlt-appgettransactioncountgt-appgettransactioncountstring-address-defaultblockparameter-defaultblockparameter)  
[appGetCode](Nervosj?id=requestlt-appgetcodegt-appgetcodestring-address-defaultblockparameter-defaultblockparameter)  
[appSendRawTransaction](Nervosj?id=requestlt-appsendtransactiongt-appsendrawtransactionstring-signedtransactiondata)  
[appCall](Nervosj?id=requestlt-appcallgt-appcallcall-call-defaultblockparameter-defaultblockparameter)  
[appGetBlockByHash](Nervosj?id=requestlt-appblockgt-appgetblockbyhash-string-blockhash-boolean-returnfulltransactionobjects)  
[appGetBlockByNumber](Nervosj?id=requestlt-appblockgt-appgetblockbynumber-defaultblockparameter-defaultblockparameter-boolean-returnfulltransactionobjects)  
[appGetTransactionByHash](Nervosj?id=requestlt-apptransactiongt-appgettransactionbyhashstring-transactionhash)  
[appGetTransactionReceipt](Nervosj?id=requestlt-appgettransactionreceiptgt-appgettransactionreceiptstring-transactionhash)  
[appNewFilter](Nervosj?id=requestlt-appfiltergt-appnewfilterorgnervosjprotocolcoremethodsrequestappfilter-appfilter)  
[appNewBlockFilter](Nervosj?id=requestlt-appfiltergt-appnewblockfilter)  
[appUninstallFilter](Nervosj?id=requestlt-appuninstallfiltergt-appuninstallfilterbiginteger-filterid)  
[appGetFilterChanges](Nervosj?id=requestlt-apploggt-appgetfilterchangesbiginteger-filterid)  
[appGetFilterLogs](Nervosj?id=requestlt-apploggt-appgetfilterlogsbiginteger-filterid)  

#### Nervosj build (NervosjService nervosj)
根据Web3jService类型实例化web3j  
<b>参数</b>  
nervosj - web3jService实例  
<b>返回值</b>  
Web3实例  
<b>例子</b>  
```
Nervosj service = Nervosj.build(new HttpService("127.0.0.1"));
```
#### Request<?, NetPeerCount> netPeer() 
获取当前连接节点数  
<b>参数</b>  
无  
<b>返回值</b>  
Request<?, NetPeerCount>  
<b>例子</b>  
```
Nervosj service = Nervosj.build(new HttpService("127.0.0.1"));
NetPeerCount netPeerCount = service.netPeerCount().send();
BigInteger peerCount = netPeerCount.getQuantity();
```
#### Request<?, AppMetaData> appMetaData(DefaultBlockParameter defaultBlockParameter)
获取指定块高的元数据  
<b>参数</b>  
defaultBlockParamter - 块高度的接口：数字或者关键字  
<b>返回值</b>  
Request<?, AppMetaData>  
<b>例子</b>  
```
Nervosj service = Nervosj.build(new HttpService("127.0.0.1"));
DefaultBlockParameter defaultParam = DefaultBlockParameter.valueOf("latest");
AppMetaDataResult result = service.appMetaData(defaultParam).send();
int chainId = result.chainId;
String chainName = result.chainName;
String genesisTS = result.genesisTimestamp;
```
#### Request<?, AppBlockNumber> appBlockNumber()
获取当前块高度  
<b>参数</b>  
无  
<b>返回值</b>  
Request<?, AppBlockNumber>  
<b>例子</b>  
```
Nervosj service = Nervosj.build(new HttpService("127.0.0.1"));
AppBlockNumber result = service.appBlockNumber().send();
BigInteger blockNumber = result.getBlockNumber();
```
#### Request<?, AppGetBalance> appGetBalance(String address, DefaultBlockParameter defaultBlockParameter))
获取地址余额  
<b>参数</b>  
address - 所要查询的地址  
defaultBlockParameter - 块高度的接口：数字或者关键字   
<b>返回值</b>  
Request<?, AppGetBalance>  
<b>例子</b>  
```
Nervosj service = Nervosj.build(new HttpService("127.0.0.1"));
string addr = "{hex nervos address starting with 0x}";
DefaultBlockParameter defaultBlockParameter = DefaultBlockParamter.valueOf("latest");
AppGetBalance getBalance = service.appGetBalance(addr, defaultBlockParamter).send();
BigInteger balance = getBalance.getBalance();
```

#### Request<?, AppGetAbi> appGetAbi(String contractAddress, DefaultBlockParameter defaultBlockParameter)
获取合约的Abi  
<b>参数</b>  
address - 所要查询Abi的合约地址  
defaultBlockParameter - 块高度的接口：数字或者关键字   
<b>返回值</b>  
Request<?, AppGetAbi>  
<b>例子</b>  
```
Nervosj service = Nervosj.build(new HttpService("127.0.0.1"));
string addr = "{hex nervos address starting with 0x}";
DefaultBlockParameter defaultBlockParameter = DefaultBlockParamter.valueOf("latest");
AppGetAbi getAbi = service.appGetAbi(addr, defaultBlockParamter).send();
String abi = getAbi.getAbi();
```

#### Request<?, AppGetTransactionCount> appGetTransactionCount(String address, DefaultBlockParameter defaultBlockParameter)
获取账户发送合约数量  
<b>参数</b>  
address - 所要查询的地址  
defaultBlockParameter - 块高度的接口：数字或者关键字   
<b>返回值</b>  
Request<?, AppGetTransactionCount>  
<b>例子</b>  
```
Nervosj service = Nervosj.build(new HttpService("127.0.0.1"));
string addr = "{hex nervos address starting with 0x}";
DefaultBlockParameter defaultBlockParameter = DefaultBlockParamter.valueOf("latest");
AppGetTransactionCount getTransactionCount = service.appGetTransactionCount(addr, defaultBlockParamter).send();
BigInteger txCount = getTransactionCount.getTransactionCount();
```

#### Request<?, AppGetCode> appGetCode(String address, DefaultBlockParameter defaultBlockParameter)
获取合约代码  
<b>参数</b>  
address - 所要查询的地址  
defaultBlockParameter - 块高度的接口：数字或者关键字   
<b>返回值</b>  
Request<?, AppGetCode>  
<b>例子</b>  
```
Nervosj service = Nervosj.build(new HttpService("127.0.0.1"));
string addr = "{hex nervos address starting with 0x}";
DefaultBlockParameter defaultBlockParameter = DefaultBlockParamter.valueOf("latest");
AppGetCode getCode = service.appGetCode(addr, defaultBlockParamter).send();
Sring code = getCode.getCode();
```

#### Request<?, AppSendTransaction> appSendRawTransaction(String signedTransactionData)
向区块链节点发送序列化交易  
<b>参数</b>  
signedTransaction - 经过签名的交易数据  
<b>返回值</b>  
Request<?, AppSendTransaction>  
<b>例子</b>  
```
//create a signed transaction
Transaction tx = Transaction.createContractTransaction(BigInteger.valueOf(nonce), this.config.getQuota(), this.currentHeight + 88, 0, chainId, value, this.config.getCode());
tx.sign(this.config.getPrivateKey(), false, false);

//instantiate a Nervosj and send the transaction
Nervosj service = Nervosj.build(new HttpService("127.0.0.1"));
AppSendTransaction sendTransaction = service.appSendRawTransaction(tx).send();

//get hash of the transaction
String hash = sendTransaction.getHash();
```

#### Request<?, AppCall> appCall(Call call, DefaultBlockParameter defaultBlockParameter)
调用合约接口  
<b>参数</b>  
call - 合约方法的call的封装  
defaultBlockParameter - 块高度的接口：数字或者关键字  
<b>返回值</b>  
Request<?, AppCall>  
<b>例子</b>  
```
Nervosj service = Nervosj.build(new HttpService("127.0.0.1"));
Call call = new Call(from, to, data);
AppCall appCall = service.appCall(call, DefaultBlockParameter.valueOf("latest")).send();
String result = call.getValue();
```

#### Request<?, AppBlock> appGetBlockByHash (String blockHash, boolean returnFullTransactionObjects)
根据块的哈希值查询块信息  
<b>参数</b>  
blockHash - 块高度的接口：数字或者关键字  
returnFullTransactionObjects - 是否返回交易信息 (True: 返回详细交易列表| False: 只返回交易hash)  
<b>返回值</b>  
Request<?, AppBlock>  
<b>例子</b>  
```
Nervosj service = Nervosj.build(new HttpService("127.0.0.1"));
String blockHash = "{block hash to search}";
AppBlock appBlock = service.appGetBlockByHash(blockHash, false).send();
```

#### Request<?, AppBlock> appGetBlockByNumber (DefaultBlockParameter defaultBlockParameter, boolean returnFullTransactionObjects)
根据块高度查询块信息  
<b>参数</b>  
defaultBlockParameter - 块高度的接口：数字或者关键字   
returnFullTransactionObjects - 是否返回交易信息 (True: 返回详细交易列表| False: 只返回交易hash)   
<b>返回值</b>  
Request<?, AppBlock>  
<b>例子</b>  
```
Nervosj service = Nervosj.build(new HttpService("127.0.0.1"));
AppBlock appBlock = service.appGetBlockByHash(DefaultBlockParameter.valueOf("latest"), false).send();
```
#### Request<?, AppTransaction> appGetTransactionByHash(String transactionHash)
根据哈希查询交易信息  
<b>参数</b>  
transactionHash - 交易哈希   
<b>返回值</b>  
Request<?, AppTransaction>  
<b>例子</b>  
```
Nervosj service = Nervosj.build(new HttpService("127.0.0.1"));
String txHash = "{hash of transactino to be searched}";
AppTransaction responseTx = service.appGetTransactionByHash(txHash).send();
```
#### Request<?, AppGetTransactionReceipt> appGetTransactionReceipt(String transactionHash)
根据交易哈希查询交易回执  
<b>参数</b>  
transactionHash - 交易哈希   
<b>返回值</b>  
Request<?, AppGetTransactionReceipt>  
<b>例子</b>  
```
Nervosj service = Nervosj.build(new HttpService("127.0.0.1"));
String txHash = "{hash of transactino to be searched}";
AppGetTransactionReceipt txReceipt = service.appGetTransactionReceipt(txHash).send();
```
#### Request<?, AppFilter> appNewFilter(org.nervosj.protocol.core.methods.request.AppFilter appFilter)
创建一个新的Nervos过滤器  
<b>参数</b>  
appFilter - 针对于Nervos智能合约event的过滤器（定义在Request中的appFilter）  
<b>返回值</b>  
Request<?, AppFilter>  
<b>例子</b>  
```
Nervosj service = Nervosj.build(new HttpService("127.0.0.1"));
org.nervosj.protocol.core.methods.request.AppFilter appFilter = new AppFilter(fromBlock, toBlock, addresses);
AppFilter appFilter = service.appNewFilter(txHash).send();
BigInteger filterId = appFilter.getFilterId();
```

#### Request<?, AppFilter> appNewBlockFilter()
创建一个新的块过滤器，当有新的块写入时通知  
<b>参数</b>  
无   
<b>返回值</b>  
Request<?, AppFilter>  
<b>例子</b>  
```
Nervosj service = Nervosj.build(new HttpService("127.0.0.1"));
AppFilter appFilter = service.appNewBlockFilter().send();
BigInteger filterId = appFilter.getFilterId();
```

#### Request<?, AppUninstallFilter> appUninstallFilter(BigInteger filterId)
移除已部署的过滤器  
<b>参数</b>  
filterId - 过滤器Id   
<b>返回值</b>  
Request<?, AppUninstallFilter>  
<b>例子</b>  
```
Nervosj service = Nervosj.build(new HttpService("127.0.0.1"));
BigInteger filterId = {your filter Id };
AppUninstallFilter uninstallFilter = service.appUninstallFilter(filterId).send();
```

#### Request<?, AppLog> appGetFilterChanges(BigInteger filterId)
根据过滤器Id查询log，返回上一次查询之后的所有log  
<b>参数</b>  
filterId - 过滤器Id   
<b>返回值</b>  
Request<?, AppLog>  
<b>例子</b>  
```
Nervosj service = Nervosj.build(new HttpService("127.0.0.1"));
BigInteger filterId = {your filter Id };
AppLog logs = service.appGetFilterChanges(filterId).send();
List<LogResult> results = logs.getLogs();
```
#### Request<?, AppLog> appGetFilterLogs(BigInteger filterId)
根据过滤器Id查询log，返回符合输入filter Id的所有log  
<b>参数</b>  
filterId - 过滤器Id   
<b>返回值</b>  
Request<?, AppLog>  
<b>例子</b>  
```
Nervosj service = Nervosj.build(new HttpService("127.0.0.1"));
BigInteger filterId = {your filter Id };
AppLog logs = service.appGetFilterLogs(filterId).send();
List<LogResult> results = logs.getLogs();
```

