Account封装了TransactionManager（在Cita中使用的是CitaTransactionManager），通过web3和账户的私钥进行实例化。Account使用了CompiledContract类，可以直接从读取Solidity合约文件，生成ABI和BIN文件以供TransactionManager使用。  
[Account](Account?id=accountstring-privatekey-nervosj-service)  
[deploy](Account?id=appsendtransaction-deployfile-contractfile-biginteger-nonce-long-quota-int-version-int-chainid-string-value)  
[callContract](Account?id=object-callcontractstring-contractaddress-string-funcname-biginteger-nonce-long-quota-int-version-int-chainid-string-value-object-args)  

#### Account(String privateKey, Nervosj service)
实例化Account对象   
<b>参数</b>  
privateKey - 发送交易地址的私钥  
service - Nervosj实例  
<b>返回值</b>  
Account  
<b>例子</b>  
```
String privateKey = "{private key}";
Nervosj service  = Nervosj.build(new HttpService("127.0.0.1"));
Account account = new Account(privateKey, service);
```
#### AppSendTransaction deploy(File contractFile, BigInteger nonce, long quota, int version, int chainId, String value)
部署合约  
<b>参数</b>  
contractFile - solidity智能合约文件  
nonce - 随机数用于防止重放攻击  
quota - 用户支付矿工的费用  
version - 链的版本信息  
chainId - 链Id  
value - 交易中原生token的数量   
<b>返回值</b>  
AppSendTransaction  
<b>例子</b>  
```
String privateKey = "{private key}";
Nervosj service  = Nervosj.build(new HttpService("127.0.0.1"));
Account account = new Account(privateKey, service);
AppSendTransaction appSendTransaction = account.deploy(new File(path), randomNonce(), quota, version, chainId, value);
```
#### Object callContract(String contractAddress, String funcName, BigInteger nonce, long quota, int version, int chainId, String value, Object... args)
调用合约方法,根据Abi中对方法的定义判断使用sendRawTransaction还是app_call。  
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
Object  
<b>例子</b>  
```
String privateKey = "{private key}";
Nervosj service  = Nervosj.build(new HttpService("127.0.0.1"));
Account account = new Account(privateKey, service);
AppSendTransaction appSendTransaction = (AppSendTransaction) account.callContract(contractAddress, transfer, randomNonce(), quota, version, chainId, value, toAddress, amount);
```
