Transction定义在core.request中，用于将交易数据封装并且签名（如果需要），交易数据或者签名后的交易数据被ethCall()或者ethSendRawTransaction()所使用进行合约的调用或者部署。  
[Transaction](zh-CN/latest/transaction?id=transactionstring-to-biginteger-nonce-long-quota-long-valid_until_block-int-version-int-chainid-string-value-string-data)  
[createContractTransaction](zh-CN/latest/transaction?id=createcontracttransactionbiginteger-nonce-long-quota-long-valid_until_block-int-version-int-chainid-string-value-string-init)  
[createFunctionCallTransaction](zh-CN/latest/transaction?id=createfunctioncalltransactionstring-to-biginteger-nonce-long-quota-long-valid_until_block-int-version-int-chainid-string-value-string-data)  
[CitaTransactionManager](zh-CN/latest/transaction?id=citatransactionmanagerweb3j-nervosj-credentials-credentials)
[sendTransaction](zh-CN/latest/transaction?id=ethsendtransaction-sendtransactionstring-to-string-data-biginteger-quota-biginteger-nonce-biginteger-validuntilblock-biginteger-version-int-chainid-string-value)  
[sendTransactionAsync](zh-CN/latest/transaction?id=completablefuture-sendtransactionasyncstring-to-string-data-biginteger-quota-biginteger-nonce-biginteger-validuntilblock-biginteger-version-int-chainid-string-value)   
#### Transaction(String to, BigInteger nonce, long quota, long valid_until_block, int version, int chainId, String value, String data)
根据参数新建一个交易  
<b>参数</b>  
to - 交易将要的发送地址  
nonce - 随机数用于防止重放攻击  
quota - 用户支付矿工的费用  
valid_until_block - 超时机制，超过设定块高取消交易
version - 链的版本信息  
chainId - 链Id  
value - 交易中原生token的数量   
data - 编码后交易数据（abi）  
<b>返回值</b>  
Transaction实例  
<b>例子</b>  
```
Web3j service = Web3j.build(new HttpService("127.0.0.1"));
String to = "{address to which the tx is sent}";
BigInteger nonce = BigInteger.valueOf(Math.abs(this.random.nextLong()));
long quota = 9999;
long valid_until_block = service.ethBlockNumber().send().getBlockNumber() + 88;
int version = 0;
in chainId = 1;
String value = "100000000";
String init = "{encoded abi}";
Transaction tx = Transction.createContractTransaction(nonce, quota, valid_until_block, version, chainId, value, init);
```
#### createContractTransaction(BigInteger nonce, long quota, long valid_until_block, int version, int chainId, String value, String init)
根据参数新建一个交易  
<b>参数</b>  
nonce - 随机数用于防止重放攻击  
quota - 用户支付矿工的费用  
valid_until_block - 超时机制，超过设定块高取消交易
version - 链的版本信息  
chainId - 链Id  
value - 交易中原生token的数量   
init - 合约编码后数据（abi）  
<b>返回值</b>  
Transaction实例  
<b>例子</b>  
```
//create new nervosj service
Web3j service = Web3j.build(new HttpService("127.0.0.1"));

//settings initiation
BigInteger nonce = BigInteger.valueOf(Math.abs(this.random.nextLong()));
long quota = 9999;
long valid_until_block = service.ethBlockNumber().send().getBlockNumber() + 88;
int version = 0;
in chainId = 1;
String value = "100000000";
String init = "{encoded abi}";

//construct transaction
Transaction txToDeployContract = Transction.createContractTransaction(nonce, quota, valid_until_block, version, chainId, value, init);
String signedTx = txToDeployContract.sign(this.config.getPrivateKey(), false, false);
EthSendTransaction EthSendTx = service.sendRawTransaction(signedTx);
```
#### createFunctionCallTransaction(String to, BigInteger nonce, long quota, long valid_until_block, int version, int chainId, String value, String data)
根据参数新建一个交易  
<b>参数</b>  
to - 交易将要的发送地址  
nonce - 随机数用于防止重放攻击  
quota - 用户支付矿工的费用  
valid_until_block - 超时机制，超过设定块高取消交易
version - 链的版本信息  
chainId - 链Id  
value - 交易中原生token的数量   
data - 编码后交易数据（abi）  
<b>返回值</b>  
Transaction实例  
<b>例子</b>  
```
//create new nervosj service
Web3j service = Web3j.build(new HttpService("127.0.0.1"));

//settings initiation
String to = "{smart contract address}";
BigInteger nonce = BigInteger.valueOf(Math.abs(this.random.nextLong()));
long quota = 9999;
long valid_until_block = service.ethBlockNumber().send().getBlockNumber() + 88;
int version = 0;
in chainId = 1;
String value = "100000000";
String init = "{encoded abi}";

//construct transaction
Transaction txToCallContract = Transction.createFunctionCallTransaction(to, nonce, quota, valid_until_block, version, chainId, value, init);
String signedTx = txToDeployContract.sign(this.config.getPrivateKey(), false, false);
EthSendTransaction EthSendTx = service.sendRawTransaction(signedTx);
```
#### CitaTransactionManager(Web3j nervosj, Credentials credentials)
CitaTransactionManager继承自TransactionManager，进行了Nervos适配。由于在Nervos中，没有支持sendTransaction()方法，所以私钥信息需要在实例化CitaTransactionManager时传入，否则无法对交易签名。  
<b>参数</b>  
nervosj - web3j实例
credentials - 发起交易账户的credential  
<b>返回值</b>  
CitaTransctionManager实例  
<b>例子</b>  
```
Credentials credentials = Credentials.create(privateKey);
Web3j service = Web3j.build(new HttpService("127.0.0.1"));
CitaTransactionManager transactionManager = new CitaTransactionManager(service, credentials);
```
#### EthSendTransaction sendTransaction(String to, String data, BigInteger quota, BigInteger nonce, BigInteger validUntilBlock, BigInteger version, int chainId, String value)
<b>参数</b>  
to - 交易将要的发送地址  
data - 编码后交易数据（abi）  
quota - 用户支付矿工的费用  
nonce - 随机数用于防止重放攻击  
valid_until_block - 超时机制，超过设定块高取消交易
version - 链的版本信息  
chainId - 链Id  
value - 交易中原生token的数量   
<b>返回值</b>  
EthSendTransaction  
<b>例子</b>  
```
CitaTransactionManager transactionManager = new CitaTransactionManager(service, credentials);
String to = "{address to which the contract is sent}";
String contractBin = "{contract bin or function call bin}";
BigInteger quota = 99999;
BigInteger nonce = BigInteger.valueOf(Math.abs(this.random.nextLong()));
long valid_until_block = service.ethBlockNumber().send().getBlockNumber() + 88;
int version = 0;
int chainId = 1;
String value = "0";
EthSendTransaction ethSendTransaction = citaTransactionManager.sendTransaction(to, contractBin, quota, nonce, valid_until_block, BigInteger.valueOf(version), chainId, value);
```
#### CompletableFuture<EthSendTransaction> sendTransactionAsync(String to, String data, BigInteger quota, BigInteger nonce, BigInteger validUntilBlock, BigInteger version, int chainId, String value)
<b>参数</b>  
to - 交易将要的发送地址  
data - 编码后交易数据（abi）  
quota - 用户支付矿工的费用  
nonce - 随机数用于防止重放攻击  
valid_until_block - 超时机制，超过设定块高取消交易
version - 链的版本信息  
chainId - 链Id  
value - 交易中原生token的数量   
<b>返回值</b>  
EthSendTransaction  
<b>例子</b>  
```
CitaTransactionManager transactionManager = new CitaTransactionManager(service, credentials);
String to = "{address to which the contract is sent}";
String contractBin = "{contract bin or function call bin}";
BigInteger quota = 99999;
BigInteger nonce = BigInteger.valueOf(Math.abs(this.random.nextLong()));
long valid_until_block = service.ethBlockNumber().send().getBlockNumber() + 88;
int version = 0;
int chainId = 1;
String value = "0";
CompletableFuture<EthSendTransaction> ethSendTransaction = citaTransactionManager.sendTransaction(to, contractBin, quota, nonce, valid_until_block, BigInteger.valueOf(version), chainId, value);
```

