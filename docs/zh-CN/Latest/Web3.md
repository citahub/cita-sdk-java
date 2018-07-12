web3j接口继承了Ethereum和web3jRx两个接口，web3j的实现类（比如JsonRpc2_0Web3j），提供了方法以发送交易的方式对合约进行部署和函数调用。web3中没有提供将solidity合约转换为java类的方法，所以对合约的操作必须依赖合约或者合约函数的二进制码，即手动拼接参数。  

[build](zh-CN/latest/web3?id=web3j-build-web3jservice-web3j)  
[netPeer](zh-CN/latest/web3?id=requestlt-netpeercountgt-netpeer)  
[ethMetaData](zh-CN/latest/web3?id=requestlt-ethmetadatagt-ethmetadatadefaultblockparameter-defaultblockparameter)  
[ethBlockNumber](zh-CN/latest/web3?id=requestlt-ethblocknumbergt-ethblocknumber)   
[ethGetBalance](zh-CN/latest/web3?id=requestlt-ethgetbalancegt-ethgetbalancestring-address-defaultblockparameter-defaultblockparameter)  
[ethGetAbi](zh-CN/latest/web3?id=requestlt-ethgetabigt-ethgetabistring-contractaddress-defaultblockparameter-defaultblockparameter)  
[ethGetTransactionCount](zh-CN/latest/web3?id=requestlt-ethgettransactioncountgt-ethgettransactioncountstring-address-defaultblockparameter-defaultblockparameter)  
[ethGetCode](zh-CN/latest/web3?id=requestlt-ethgetcodegt-ethgetcodestring-address-defaultblockparameter-defaultblockparameter)  
[ethSendRawTransaction](zh-CN/latest/web3?id=requestlt-ethsendtransactiongt-ethsendrawtransactionstring-signedtransactiondata)  
[ethCall](zh-CN/latest/web3?id=requestlt-ethcallgt-ethcallcall-call-defaultblockparameter-defaultblockparameter)  
[ethGetBlockByHash](zh-CN/latest/web3?id=requestlt-ethblockgt-ethgetblockbyhash-string-blockhash-boolean-returnfulltransactionobjects)  
[ethGetBlockByNumber](zh-CN/latest/web3?id=requestlt-ethblockgt-ethgetblockbynumber-defaultblockparameter-defaultblockparameter-boolean-returnfulltransactionobjects)  
[ethGetTransactionByHash](zh-CN/latest/web3?id=requestlt-ethtransactiongt-ethgettransactionbyhashstring-transactionhash)  
[ethGetTransactionReceipt](zh-CN/latest/web3?id=requestlt-ethgettransactionreceiptgt-ethgettransactionreceiptstring-transactionhash)  
[ethNewFilter](zh-CN/latest/web3?id=requestlt-ethfiltergt-ethnewfilterorgweb3jprotocolcoremethodsrequestethfilter-ethfilter)  
[ethNewBlockFilter](zh-CN/latest/web3?id=requestlt-ethfiltergt-ethnewblockfilter)  
[ethUninstallFilter](zh-CN/latest/web3?id=requestlt-ethuninstallfiltergt-ethuninstallfilterbiginteger-filterid)  
[ethGetFilterChanges](zh-CN/latest/web3?id=requestlt-ethloggt-ethgetfilterchangesbiginteger-filterid)  
[ethGetFilterLogs](zh-CN/latest/web3?id=requestlt-ethloggt-ethgetfilterlogsbiginteger-filterid)  

#### Web3j build (Web3jService web3j)
根据Web3jService类型实例化web3j  
<b>参数</b>  
web3j - web3jService实例  
<b>返回值</b>  
Web3实例  
<b>例子</b>  
```
Web3j service = Web3j.build(new HttpService("127.0.0.1"));
```
#### Request<?, NetPeerCount> netPeer() 
获取当前连接节点数  
<b>参数</b>  
无  
<b>返回值</b>  
Request<?, NetPeerCount>  
<b>例子</b>  
```
Web3j service = Web3j.build(new HttpService("127.0.0.1"));
NetPeerCount netPeerCount = service.netPeerCount().send();
BigInteger peerCount = netPeerCount.getQuantity();
```
#### Request<?, EthMetaData> ethMetaData(DefaultBlockParameter defaultBlockParameter)
获取指定块高的元数据  
<b>参数</b>  
defaultBlockParamter - 块高度的接口：数字或者关键字  
<b>返回值</b>  
Request<?, EthMetaData>  
<b>例子</b>  
```
Web3j service = Web3j.build(new HttpService("127.0.0.1"));
DefaultBlockParameter defaultParam = DefaultBlockParameter.valueOf("latest");
EthMetaDataResult result = service.ethMetaData(defaultParam).send();
int chainId = result.chainId;
String chainName = result.chainName;
String genesisTS = result.genesisTimestamp;
```
#### Request<?, EthBlockNumber> ethBlockNumber()
获取当前块高度  
<b>参数</b>  
无
<b>返回值</b>  
Request<?, EthBlockNumber>  
<b>例子</b>  
```
Web3j service = Web3j.build(new HttpService("127.0.0.1"));
EthBlockNumber result = service.ethBlockNumber().send();
BigInteger blockNumber = result.getBlockNumber();
```
#### Request<?, EthGetBalance> ethGetBalance(String address, DefaultBlockParameter defaultBlockParameter))
获取地址余额  
<b>参数</b>  
address - 所要查询的地址  
defaultBlockParameter - 块高度的接口：数字或者关键字   
<b>返回值</b>  
Request<?, EthGetBalance>  
<b>例子</b>  
```
Web3j service = Web3j.build(new HttpService("127.0.0.1"));
string addr = "{hex nervos address starting with 0x}";
DefaultBlockParameter defaultBlockParameter = DefaultBlockParamter.valueOf("latest");
EthGetBalance getBalance = service.ethGetBalance(addr, defaultBlockParamter).send();
BigInteger balance = getBalance.getBalance();
```

#### Request<?, EthGetAbi> ethGetAbi(String contractAddress, DefaultBlockParameter defaultBlockParameter)
获取合约的Abi  
<b>参数</b>  
address - 所要查询Abi的合约地址  
defaultBlockParameter - 块高度的接口：数字或者关键字   
<b>返回值</b>  
Request<?, EthGetAbi>  
<b>例子</b>  
```
Web3j service = Web3j.build(new HttpService("127.0.0.1"));
string addr = "{hex nervos address starting with 0x}";
DefaultBlockParameter defaultBlockParameter = DefaultBlockParamter.valueOf("latest");
EthGetAbi getAbi = service.ethGetAbi(addr, defaultBlockParamter).send();
String abi = getAbi.getAbi();
```

#### Request<?, EthGetTransactionCount> ethGetTransactionCount(String address, DefaultBlockParameter defaultBlockParameter)
获取账户发送合约数量  
<b>参数</b>  
address - 所要查询的地址  
defaultBlockParameter - 块高度的接口：数字或者关键字   
<b>返回值</b>  
Request<?, EthGetTransactionCount>  
<b>例子</b>  
```
Web3j service = Web3j.build(new HttpService("127.0.0.1"));
string addr = "{hex nervos address starting with 0x}";
DefaultBlockParameter defaultBlockParameter = DefaultBlockParamter.valueOf("latest");
EthGetTransactionCount getTransactionCount = service.ethGetTransactionCount(addr, defaultBlockParamter).send();
BigInteger txCount = getTransactionCount.getTransactionCount();
```

#### Request<?, EthGetCode> ethGetCode(String address, DefaultBlockParameter defaultBlockParameter)
获取合约代码  
<b>参数</b>  
address - 所要查询的地址  
defaultBlockParameter - 块高度的接口：数字或者关键字   
<b>返回值</b>  
Request<?, EthGetCode>  
<b>例子</b>  
```
Web3j service = Web3j.build(new HttpService("127.0.0.1"));
string addr = "{hex nervos address starting with 0x}";
DefaultBlockParameter defaultBlockParameter = DefaultBlockParamter.valueOf("latest");
EthGetCode getCode = service.ethGetCode(addr, defaultBlockParamter).send();
Sring code = getCode.getCode();
```

#### Request<?, EthSendTransaction> ethSendRawTransaction(String signedTransactionData)
向区块链节点发送序列化交易  
<b>参数</b>  
signedTransaction - 经过签名的交易数据  
<b>返回值</b>  
Request<?, EthSendTransaction>  
<b>例子</b>  
```
//create a signed transaction
Transaction tx = Transaction.createContractTransaction(BigInteger.valueOf(nonce), this.config.getQuota(), this.currentHeight + 88, 0, chainId, value, this.config.getCode());
tx.sign(this.config.getPrivateKey(), false, false);

//instantiate a Web3j and send the transaction
Web3j service = Web3j.build(new HttpService("127.0.0.1"));
EthSendTransaction sendTransaction = service.ethSendRawTransaction(tx).send();

//get hash of the transaction
String hash = sendTransaction.getHash();
```

#### Request<?, EthCall> ethCall(Call call, DefaultBlockParameter defaultBlockParameter)
调用合约接口  
<b>参数</b>  
call - 合约方法的call的封装  
defaultBlockParameter - 块高度的接口：数字或者关键字  
<b>返回值</b>  
Request<?, EthCall>  
<b>例子</b>  
```
Web3j service = Web3j.build(new HttpService("127.0.0.1"));
Call call = new Call(from, to, data);
EthCall ethCall = service.ethCall(call, DefaultBlockParameter.valueOf("latest")).send();
String result = call.getValue();
```

#### Request<?, EthBlock> ethGetBlockByHash (String blockHash, boolean returnFullTransactionObjects)
根据块的哈希值查询块信息  
<b>参数</b>  
blockHash - 块高度的接口：数字或者关键字  
returnFullTransactionObjects - 是否返回交易信息 (True: 返回详细交易列表| False: 只返回交易hash)  
<b>返回值</b>  
Request<?, EthBlock>  
<b>例子</b>  
```
Web3j service = Web3j.build(new HttpService("127.0.0.1"));
String blockHash = "{block hash to search}";
EthBlock ethBlock = service.ethGetBlockByHash(blockHash, false).send();
```

#### Request<?, EthBlock> ethGetBlockByNumber (DefaultBlockParameter defaultBlockParameter, boolean returnFullTransactionObjects)
根据块高度查询块信息  
<b>参数</b>  
defaultBlockParameter - 块高度的接口：数字或者关键字   
returnFullTransactionObjects - 是否返回交易信息 (True: 返回详细交易列表| False: 只返回交易hash)   
<b>返回值</b>  
Request<?, EthBlock>  
<b>例子</b>  
```
Web3j service = Web3j.build(new HttpService("127.0.0.1"));
EthBlock ethBlock = service.ethGetBlockByHash(DefaultBlockParameter.valueOf("latest"), false).send();
```
#### Request<?, EthTransaction> ethGetTransactionByHash(String transactionHash)
根据哈希查询交易信息  
<b>参数</b>  
transactionHash - 交易哈希   
<b>返回值</b>  
Request<?, EthTransaction>  
<b>例子</b>  
```
Web3j service = Web3j.build(new HttpService("127.0.0.1"));
String txHash = "{hash of transactino to be searched}";
EthTransaction responseTx = service.ethGetTransactionByHash(txHash).send();
```
#### Request<?, EthGetTransactionReceipt> ethGetTransactionReceipt(String transactionHash)
根据交易哈希查询交易回执  
<b>参数</b>  
transactionHash - 交易哈希   
<b>返回值</b>  
Request<?, EthGetTransactionReceipt>  
<b>例子</b>  
```
Web3j service = Web3j.build(new HttpService("127.0.0.1"));
String txHash = "{hash of transactino to be searched}";
EthGetTransactionReceipt txReceipt = service.ethGetTransactionReceipt(txHash).send();
```
#### Request<?, EthFilter> ethNewFilter(org.web3j.protocol.core.methods.request.EthFilter ethFilter)
创建一个新的Nervos过滤器  
<b>参数</b>  
ethFilter - 针对于Nervos智能合约event的过滤器（定义在Request中的ethFilter）  
<b>返回值</b>  
Request<?, EthFilter>  
<b>例子</b>  
```
Web3j service = Web3j.build(new HttpService("127.0.0.1"));
org.web3j.protocol.core.methods.request.EthFilter ethFilter = new EthFilter(fromBlock, toBlock, addresses);
EthFilter ethFilter = service.ethNewFilter(txHash).send();
BigInteger filterId = ethFilter.getFilterId();
```

#### Request<?, EthFilter> ethNewBlockFilter()
创建一个新的块过滤器，当有新的块写入时通知  
<b>参数</b>  
无   
<b>返回值</b>  
Request<?, EthFilter>  
<b>例子</b>  
```
Web3j service = Web3j.build(new HttpService("127.0.0.1"));
EthFilter ethFilter = service.ethNewBlockFilter().send();
BigInteger filterId = ethFilter.getFilterId();
```

#### Request<?, EthUninstallFilter> ethUninstallFilter(BigInteger filterId)
移除已部署的过滤器  
<b>参数</b>  
filterId - 过滤器Id   
<b>返回值</b>  
Request<?, EthUninstallFilter>  
<b>例子</b>  
```
Web3j service = Web3j.build(new HttpService("127.0.0.1"));
BigInteger filterId = {your filter Id };
EthUninstallFilter uninstallFilter = service.ethUninstallFilter(filterId).send();
```

#### Request<?, EthLog> ethGetFilterChanges(BigInteger filterId)
根据过滤器Id查询log，返回上一次查询之后的所有log  
<b>参数</b>  
filterId - 过滤器Id   
<b>返回值</b>  
Request<?, EthLog>  
<b>例子</b>  
```
Web3j service = Web3j.build(new HttpService("127.0.0.1"));
BigInteger filterId = {your filter Id };
EthLog logs = service.ethGetFilterChanges(filterId).send();
List<LogResult> results = logs.getLogs();
```
#### Request<?, EthLog> ethGetFilterLogs(BigInteger filterId)
根据过滤器Id查询log，返回符合输入filter Id的所有log
<b>参数</b>  
filterId - 过滤器Id   
<b>返回值</b>  
Request<?, EthLog>  
<b>例子</b>  
```
Web3j service = Web3j.build(new HttpService("127.0.0.1"));
BigInteger filterId = {your filter Id };
EthLog logs = service.ethGetFilterLogs(filterId).send();
List<LogResult> results = logs.getLogs();
```

