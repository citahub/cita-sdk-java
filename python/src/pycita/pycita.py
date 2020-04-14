from typing import Iterable, Dict, List, Tuple, Optional, Union, cast
import typing
from dataclasses import dataclass
import subprocess
import json
import time
import ast
from binascii import hexlify, unhexlify
from pathlib import Path
import random

import requests
from eth_abi import encode_single, decode_single
import sha3  # type: ignore

PARAM = Union[str, bytes]  # 用于CitaClient方法的参数类型. 如果是str, 默认都具有'0x'前缀
DEFAULT_QUOTA = 10000000  # 默认调用每个合约方法所消耗的quota上限.


def param_to_str(p: PARAM) -> str:
    """将PARAM统一到str形式."""
    assert (isinstance(p, str) and p.startswith('0x')) or isinstance(p, bytes)
    return p if isinstance(p, str) else f'0x{hexlify(p).decode()}'


def param_to_bytes(p: PARAM) -> bytes:
    """将PARAM统一到bytes形式."""
    assert (isinstance(p, str) and p.startswith('0x')) or isinstance(p, bytes)
    return p if isinstance(p, bytes) else unhexlify(p[2:])


@typing.no_type_check
def join_param(*param_list: List[PARAM]) -> str:
    """
    用于拼接多个PARAM类型.

    :param param_list: 参数列表. 如果某个元素是str, 则开头必须是'0x'
    :return: 拼接后的字符串
    """
    if not param_list:
        return ''

    # 参数中有str, 返回str类型
    ret = ['0x']
    for i in param_list:
        if isinstance(i, bytes):
            ret.append(hexlify(i).decode())
        else:
            assert i[:2] == '0x'
            ret.append(i[2:])
    return ''.join(ret)


def equal_param(lhs: PARAM, rhs: PARAM) -> bool:
    """判断两个参数的内容是否相同."""
    if type(lhs) is type(rhs):
        return lhs == rhs
    return param_to_bytes(lhs) == param_to_bytes(rhs)


def encode_param(types: str, values) -> bytes:
    r"""
    返回参数编码后的字符串.

    :param types, values: 参考eth_abi.encode_single的文档.
    :return: 如b'\x0b\xad\xf0\x0d'...
    """
    if not types:
        return b''

    if types[0] != '(':
        types = f'({types})'

    if isinstance(values, tuple):
        return encode_single(types, values)
    else:
        return encode_single(types, (values,))


def decode_param(types: str, bin: bytes):
    """
    返回解码后的python类型的数据.

    :param types, values: 参考eth_abi.decode_single的文档
    :return: python类型的数据. 如果包含2个以上的值, 返回Tuple. 如果只有一个值, 则解开Tuple
    """
    if (not types) or types[0] != '(':
        types = f'({types})'
    ret = decode_single(types, bin)
    if len(ret) == 0:
        return ()
    return ret if len(ret) >= 2 else ret[0]


class CitaClient:
    """
    cita-cli的封装.

    注意成员函数的参数, 如果是Union[str, bytes] 和返回值的编码都使用bytes, 以避免是否要加0x的困惑
    """

    def __init__(self, cli_path: str, url: str, timeout: int = 10):
        """
        指定cita环境.

        :param cli_path: cita-cli可执行文件所在的路径
        :param url: cita后端服务的url
        :param timeout: JSON RPC或cita-cli的调用超时时间, 单位秒
        """
        assert Path(cli_path).exists(), '未找到cita-cli'
        self.cli_path: List[str] = [cli_path]
        self.url = url
        self.timeout = timeout

    def _raw_cmd(self, cmd: List[str]):
        """
        调用cita-cli.

        :param cmd: 命令行参数. 如果其中有bytes类型, 会转化为0x开头的hex字符串
        :return: 返回cita-cli的输出
        """
        full_cmd = self.cli_path + cmd

        r = subprocess.check_output(full_cmd, encoding='utf8', text=True, timeout=self.timeout)
        return r

    def _jsonrpc(self, method: str, params: List) -> Union[None, str, Dict, List]:
        """
        执行jsonrpc调用.

        :param method: JSON RPC的方法名
        :param params: 被调方法的实参列表
        :return: JSON
        """
        req_id = random.randint(1, 10000)
        req = {
            "jsonrpc": "2.0",
            "id": req_id,
            "method": method,
            "params": params
        }
        resp = requests.post(self.url, json=req, timeout=self.timeout)
        try:
            rj = resp.json()
            assert rj['id'] == req_id
            return rj['result']
        except Exception:
            raise RuntimeError(f'`{method}` jsonrpc failed. code={resp.status_code} reason={resp.text} original_req={req}')

    def create_key(self) -> Dict[str, str]:
        """
        创建账户. 只有用私钥签名发起交易后, 才会改变链上状态.

        :return: 如 ``{'address': '0x11...', 'public': '0x22...', 'private': '0x33...'}``
        """
        r = json.loads(self._raw_cmd(['key', 'create']))
        assert r['address'][:2] == '0x'
        assert r['private'][:2] == '0x'
        assert r['public'][:2] == '0x'
        return r

    def get_peer_count(self) -> int:
        """兄弟节点个数."""
        r = self._jsonrpc('peerCount', [])
        r = cast(str, r)
        assert r.startswith('0x')
        return ast.literal_eval(r)

    def get_peers(self) -> Dict[str, str]:
        """
        获取兄弟节点信息.

        :return: 各个节点的信息, {节点名: 节点ip, ...}
        """
        r = self._jsonrpc('peersInfo', [])
        r = cast(Dict, r)
        return r.get('peers', {})

    def get_latest_block_number(self) -> int:
        """最新区块的高度."""
        r = self._jsonrpc('blockNumber', [])
        r = cast(str, r)
        assert r.startswith('0x')
        return ast.literal_eval(r)

    def get_block_by_hash(self, hash: PARAM, tx_detail: bool = False) -> Dict:
        """
        根据区块hash获取区块详情.

        :param hash: 32字节的hash
        :param tx_detail: True 区块中会包含交易详情, 否则只包含交易hash
        :return: 区块详情
        """
        hash_str = param_to_str(hash)
        assert len(hash_str) == 64 + 2
        r = self._jsonrpc('getBlockByHash', [hash_str, tx_detail])
        return cast(Dict, r)

    def get_block_by_number(self, height: int, tx_detail: bool = False) -> Dict:
        """
        根据区块id获取区块详情.

        :param height: 区块高度, 从0起
        :param tx_detail: True 区块中会包含交易详情, 否则只包含交易hash
        :return: 区块详情
        """
        assert height >= 0
        r = self._jsonrpc('getBlockByNumber', ['0x%02x' % height, tx_detail])
        return cast(Dict, r)

    def deploy_contract(self, private_key: PARAM, code: PARAM, param: PARAM = b'') -> str:
        """
        部署合约.

        :param private_key: 私钥
        :param code: 编译后的合约
        :param params: 合约构造函数的参数经encode_param编码后的bytes. 无参数用 b''
        :return: 交易回执地址
        """
        cmd = ['rpc', 'sendRawTransaction',
               '--code', join_param(code, param),
               '--private-key', private_key,
               '--url', self.url]
        r = json.loads(self._raw_cmd(cmd))
        if r.get('result', {}).get('status') != 'OK':
            raise RuntimeError(r)

        reply = r['result']['hash']
        assert reply[:2] == '0x'
        return reply

    def confirm_transaction(self, hash_addr: PARAM, timeout: int = -1) -> Dict:
        """
        等待交易完成.

        :param hash_addr: 回执地址, 32字节
        :param timeout: 等待回执的时间, 单位秒. -1: 一直等待回执; 0: 无论是否达成共识, 直接返回; 其他值表示超时时间
        :return: 回执结果.
        """
        r: Dict = {}
        t0 = time.time()
        r = self.get_transcation_receipt(hash_addr, 0)

        # 先获得交易回执
        while 'blockNumber' not in r:
            time.sleep(1)
            t1 = time.time()
            if t1 - t0 >= timeout and timeout != -1:
                raise RuntimeError('timeout')
            r = self.get_transcation_receipt(hash_addr, 0)

        this_block = ast.literal_eval(r['blockNumber'])
        while self.get_latest_block_number() - this_block < 1:  # cita是先共识交易顺序, 后执行交易, 下个块公布上次答案, 所以会差1个块.
            time.sleep(1)
            t1 = time.time()
            if t1 - t0 >= timeout and timeout != -1:
                raise RuntimeError('timeout')
        return r

    def get_transcation_receipt(self, hash_addr: PARAM, timeout: int = -1) -> Dict:
        """
        查看回执结果.

        :param hash_addr: 回执地址, 32字节
        :param timeout: 等待回执的时间, 单位秒. -1: 一直等待回执; 0: 无论有无回执, 直接返回; 其他值表示超时时间
        :return: 回执结果. 如果交易还没执行且timeout=0, 则返回{}. 否则表示在pending区块中已经加入此交易, 期待共识
        """
        t0 = time.time()

        while 1:
            r = self._jsonrpc('getTransactionReceipt', [param_to_str(hash_addr)])
            if r is None:
                r = {}
            assert isinstance(r, dict)
            if timeout == 0 or r:
                break

            t1 = time.time()
            if t1 - t0 >= timeout and timeout != -1:
                raise RuntimeError('timeout')
            time.sleep(1)

        error = r.get('errorMessage')
        if error:  # 交易失败
            raise RuntimeError(error)

        return r

    def call_readonly_func(self, contract_addr: PARAM, func_addr: PARAM, param: PARAM = b'', from_addr: PARAM = b'', block_number: str = 'latest') -> bytes:
        """
        调用合约的只读函数.

        :param contract_addr: 合约地址, 20字节
        :param func_addr: 合约内的函数地址, 4字节
        :param param: 合约构造函数的参数经encode_param编码后的bytes. 无参数用 b''
        :param from_addr: 调用者的地址, 默认是 b''
        :param block_number: 区块高度, 可以是一个'0x'开头的高度字符串, 也可以是'latest'或'pending', 默认是'latest'
        :return: 返回值编码的bytes
        """
        # 构造 CallRequest
        to_ = param_to_str(contract_addr)
        assert len(to_) == 40 + 2
        req = {'to': to_}

        if from_addr:
            from_ = param_to_str(from_addr)
            assert len(from_) == 40 + 2
            req['from'] = from_

        data = param_to_str(func_addr)
        assert len(data) == 8 + 2
        if param:
            data += param_to_str(param)[2:]
        req['data'] = data

        r = self._jsonrpc('call', [req, block_number])
        assert isinstance(r, str) and r.startswith('0x')
        return param_to_bytes(r)

    def call_func(self, private_key: PARAM, contract_addr: PARAM, func_addr: PARAM, param: PARAM = b'', quota: int = DEFAULT_QUOTA) -> str:
        """
        调用合约的函数.

        :param private_key: 私钥
        :param contract_addr: 合约地址
        :param func_addr: 合约内的函数地址
        :param param: 编码后的函数参数列表
        """
        cmd: List[str] = ['rpc', 'sendRawTransaction',
                          '--address', param_to_str(contract_addr),
                          '--code', join_param(func_addr, param),
                          '--quota', str(quota),
                          '--private-key', param_to_str(private_key),
                          '--url', self.url]
        r = json.loads(self._raw_cmd(cmd))
        if r.get('result', {}).get('status') != 'OK':
            raise RuntimeError(r)

        return r['result']['hash']

    def batch_call_func(self, private_key: PARAM, tx_code_list: List[PARAM], quota: int = DEFAULT_QUOTA) -> str:
        """
        发起批量交易.

        :param private_key: 私钥.
        :param tx_code_list: 由ContractClass.get_tx_code生成的交易数据.
        :return: 交易hash
        """
        cmd: List[str] = ['scm', 'BatchTx', 'multiTxs',
                          '--private-key', param_to_str(private_key),
                          '--quota', str(quota),
                          '--url', self.url]

        for tx_code in tx_code_list:
            cmd += ['--tx-code', param_to_str(tx_code)]
        r = json.loads(self._raw_cmd(cmd))
        if r.get('result', {}).get('status') != 'OK':
            raise RuntimeError(r)

        return r['result']['hash']

    # def estimate_quota(self, contract_addr: PARAM, func_addr: PARAM, param: PARAM = b'', from_addr: PARAM = b'') -> int:
    #     """
    #     估计合约调用所需的quota. (只在商业版可用)

    #     :param contract_addr: 合约地址.
    #     :param func_addr: 合约内的函数地址.
    #     :param param: 编码后的函数参数列表.
    #     :param from_addr: 合约的调用方地址.
    #     :return: 此调用所需的quota.
    #     """
    #     to_ = param_to_str(contract_addr)
    #     assert len(to_) == 40 + 2
    #     req = {'to': to_}

    #     if from_addr:
    #         from_ = param_to_str(from_addr)
    #         assert len(from_) == 40 + 2
    #         req['from'] = from_

    #     data = param_to_str(func_addr)
    #     assert len(data) == 8 + 2
    #     if param:
    #         data += param_to_str(param)[2:]
    #     req['data'] = data

    #     r = self._jsonrpc('estimateQuota', [req, 'latest'])
    #     assert isinstance(r, str) and r.startswith('0x')
    #     return ast.literal_eval(r)

    def get_code(self, contract_addr: PARAM) -> bytes:
        """
        获取合约代码.

        :param contract_addr: 合约地址, 20字节
        :return: 合约代码bytes
        """
        addr = param_to_str(contract_addr)
        assert len(addr) == 42
        r = self._jsonrpc('getCode', [addr, 'latest'])
        assert isinstance(r, str) and r.startswith('0x')
        if r == '0x':  # 合约不存在
            return b''
        rb = param_to_bytes(r)
        return rb

    def get_abi(self, contract_addr: PARAM) -> List:
        """
        获取合约的ABI.

        :param contract_addr: 合约地址, 20字节
        :return: ABI的json
        """
        addr = param_to_str(contract_addr)
        assert len(addr) == 42
        r = self._jsonrpc('getAbi', [addr, 'latest'])
        assert isinstance(r, str) and r.startswith('0x')
        if r == '0x':  # 合约不存在或未绑定ABI
            return []

        rb = param_to_bytes(r)
        rbs = decode_param('string', rb)
        return json.loads(rbs)

    def store_abi(self, private_key: PARAM, contract_addr: PARAM, abi: str) -> str:
        """
        将ABI追加给指定的合约.

        :param private_key: 私钥
        :param contract_addr: 合约地址
        :param abi: ABI的json的字符串形式
        :return: 交易hash
        """
        abi_data = encode_param('string', abi)

        cmd = ['rpc', 'sendRawTransaction',
               '--address', '0xffffffffffffffffffffffffffffffffff010001',
               '--code', join_param(contract_addr, abi_data),
               '--private-key', private_key,
               '--url', self.url]
        r = json.loads(self._raw_cmd(cmd))
        if r.get('result', {}).get('status') != 'OK':
            raise RuntimeError(r)

        return r['result']['hash']

    def get_transaction(self, tx_hash: PARAM) -> Dict:
        """
        获取交易详情.

        :param tx_hash: 交易hash, 32字节
        :return: JSON结构的交易详情
        """
        h = param_to_str(tx_hash)
        assert len(h) == 64 + 2
        r = self._jsonrpc('getTransaction', [h])
        if not r:
            return {}
        return cast(Dict, r)

    def get_transaction_count(self, addr: PARAM, block_number: str = 'latest') -> int:
        """
        获取指定账户发起的交易数量.

        :param addr: 账户地址, 20字节
        :param block_number: 区块高度, 可以是一个'0x'开头的高度字符串, 也可以是'latest'或'pending', 默认是'latest'
        :return: 交易数量
        """
        addr_ = param_to_str(addr)
        assert len(addr) == 40 + 2
        r = self._jsonrpc('getTransactionCount', [addr_, block_number])
        if not r or r == '0x':
            return 0
        assert isinstance(r, str)
        return ast.literal_eval(r)

    def decode_transaction_content(self, content: PARAM) -> Dict:
        """
        把交易内容解析成结构化的各个字段.

        :param content: 交易内容, 就是 JSON RPC ``getTransaction`` 返回的``content``字段.
        """
        cmd = ['tx', 'decode-unverifiedTransaction',
               '--content', param_to_str(content)]
        return json.loads(self._raw_cmd(cmd))


@dataclass
class ABI:
    func_name: str  # 合约方法名
    func_addr: str  # 合约方法地址 0x12345678
    param_types: str  # 参数
    return_types: str  # 返回值
    mutable: bool  # 是否只读
    quota: int  # 调用配额


class ContractClass:

    def __init__(self, sol_file: Path, client: CitaClient, func_name2quota: Optional[Dict[str, int]] = None):
        """
        生成ABI定义好的合约调用对象.

        :param sol_file: ``.sol`` 合约文件路径. 此文件中应该包含两行注释:

                         - ``BYTECODE=6080bc...`` , **不** 包含0x开头的合约二进制的hex编码串
                         - ``ABI=[{"inputs": ...}]`` , ABI的JSON定义

        :param client: CitaClient.
        :param func_name2quota: 方法名->最大Quota.
        """
        self.client = client
        self.name, self.bytecode, self.abi = self._parse_sol_file(sol_file)
        self.func_mapping: Dict[str, ABI] = self._parse_abi(self.abi, func_name2quota if func_name2quota else {})

    def get_raw_abi(self) -> str:
        """返回remix提供的原始abi."""
        return self.abi

    def get_code(self) -> str:
        """获取合约的bytecode."""
        return self.bytecode

    @staticmethod
    def _parse_sol_file(sol_file: Path) -> Tuple[str, str, str]:
        """解析.sol, 提取主合约名, BYTECODE, ABI."""
        assert sol_file.name.endswith('.sol'), '请输入合约定义文件的路径 *.sol'
        classname = sol_file.stem
        fn = sol_file.with_suffix('.bin')
        with open(fn) as f:
            lines = [i.strip() for i in f]

        bytecode = ''
        for no, line in enumerate(lines):
            # 找出主合约的部分 ======= <stdin>:XXX =======
            if line.find('<stdin>:' + classname) != -1:
                assert lines[no + 1].strip() == 'Binary:'
                assert lines[no + 3].strip() == 'Contract JSON ABI'
                bytecode = '0x' + lines[no + 2]
                abi = lines[no + 4]
                break

        if not bytecode:
            raise RuntimeError(f'找不到合约编译后的内容: {fn}')

        return classname, bytecode, abi

    @staticmethod
    def _parse_abi(abi: str, func_name2quota: Dict[str, int]) -> Dict[str, ABI]:
        """解析ABI到函数签名."""
        result: Dict[str, ABI] = {}
        for func_def in json.loads(abi):
            if func_def['type'] not in ('function', 'constructor'):  # 比如 event
                continue
            func_name = func_def.get('name', '')
            return_types = ','.join(i['type'] for i in func_def.get('outputs', []))
            mutable = func_def['stateMutability'] not in ('view', 'pure', 'constant')
            param_types = ','.join(i['type'] for i in func_def['inputs'])
            sig = f'{func_name}({param_types})'
            func_addr = '0x' + sha3.keccak_256(sig.encode()).hexdigest()[:8]
            t = ABI(func_name, func_addr, param_types, return_types, mutable,
                    func_name2quota.get(func_name, DEFAULT_QUOTA))

            # 重载函数只支持第一个名字的自动映射, 其他都要使用方法地址.
            if func_name not in result:
                result[func_name] = t
            if func_name != '':  # 跳过构造函数
                result[func_addr] = t
        return result

    def instantiate_raw(self, private_key: PARAM, *args) -> str:
        """
        部署合约, 不等待交易回执.

        :param private_key: 用于部署合约的私钥.
        :param args: 合约构造函数的参数.
        :return: 部署交易hash.
        """
        if args == ():
            param = b''
        else:
            param = encode_param(self.func_mapping[''].param_types, args)
        return self.client.deploy_contract(private_key, self.bytecode, param)

    def instantiate(self, private_key: PARAM, *args) -> Tuple['ContractProxy', str, str]:
        """
        部署合约, 等待交易回执.

        :param private_key: 用于部署合约的私钥
        :param args: 合约构造函数的参数.
        :return: (合约实例的封装, 合约地址, 部署交易hash)
        """
        tx_hash = self.instantiate_raw(private_key, *args)
        r = self.client.get_transcation_receipt(tx_hash)
        contract_addr = r['contractAddress']
        # if store_abi:
        #     h = client.store_abi(private_key, contract_addr, json.dumps(self.abi, separators=(',', ':')))
        proxy = self.bind(contract_addr, private_key)
        return proxy, contract_addr, tx_hash

    def batch_instantiate(self, private_key: PARAM, param_list: Iterable) -> List[Tuple['ContractProxy', str, str]]:
        """
        批量的部署合约, 等待交易回执.

        :param private_key: 用于部署合约的私钥
        :param param_list: 每个合约构造函数的参数
        :return: (合约实例的封装, 合约地址, 部署交易hash)
        """
        result: List[Tuple['ContractProxy', str, str]] = []

        tx_hash_list = [self.instantiate_raw(private_key, *args if isinstance(args, tuple) else (args,)) for args in param_list]

        for tx_hash in tx_hash_list:
            r = self.client.get_transcation_receipt(tx_hash, self.client.timeout)
            contract_addr = r['contractAddress']
            proxy = self.bind(contract_addr, private_key)
            result.append((proxy, contract_addr, tx_hash))
        return result

    def bind(self, contract_addr: PARAM, private_key: PARAM) -> 'ContractProxy':
        """
        绑定到一个以部署的合约地址.

        :param private_key: 用于部署合约的私钥
        :return: 合约实例的封装
        """
        return ContractProxy(self.name, self.func_mapping, self.client, private_key, contract_addr)


class ContractProxy:
    """合约对象的代理, 用于转发函数调用. 通过proxy._ContractProxy__contract_addr可以获得合约地址."""

    def __init__(self, class_name: str, func_mapping: Dict[str, ABI], client: CitaClient, private_key: PARAM, contract_addr: PARAM):
        """
        初始化.

        :param class_name: 合约名称
        :param func_mapping: func_name -> (func_name, func_addr, param_types, return_types, quota)
        :param client: CitaClient对象
        :param private_key: 私钥
        :param contract_addr: 合约部署地址, 20字节
        """
        # 注意. 使用特殊的成员变量命名方式, 尽力避免与合约方法的冲突
        self.class_name__ = class_name
        self.func_mapping__ = func_mapping.copy()
        self.client__ = client
        self.private_key__ = private_key
        self.contract_addr__ = contract_addr
        self.call_mode__ = 'latest'

    def set_call_mode(self, mode):
        """
        设置读取只读方法时, 是使用已确认区块的数据, 还是待确认区块的数据.
        :param mode: 默认'latest', 使用已确认区块; 'pending', 使用待确认区块.
        """
        assert mode in ('latest', 'pending')
        self.call_mode__ = mode

    def do_call_func__(self, func_addr: str, args):
        """
        执行合约方法调用.

        :param func_addr: 合约方法地址.
        :param args: 参数, 需配合合约方法的 param_type.
        :return 对普通方法返回tx_hash, 对只读方法返回解码后的返回值.
        """
        abi = self.func_mapping__[func_addr]

        if not args:
            arg_bytes = b''
        else:
            arg_bytes = encode_param(abi.param_types, args)

        if abi.mutable:  # 普通方法调用, 返回回执哈希
            return self.client__.call_func(self.private_key__, self.contract_addr__, func_addr, param=arg_bytes, quota=abi.quota)

        # 只读方法调用, 返回结果
        return_bytes = self.client__.call_readonly_func(self.contract_addr__, func_addr, param=arg_bytes, block_number=self.call_mode__)
        return decode_param(abi.return_types, return_bytes)

    def get_tx_code(self, func_name_or_addr: str, args=()) -> str:
        """
        计算调用合约方法时的tx_code. 往往用于批量调用.

        :param func_addr: 合约方法地址.
        :param args: 参数, 需配合合约方法的 param_type.
        :return: '0x'开头的字符串, 由(合约地址 + 方法地址 + 编码后的参数)拼接而成.
        """
        abi = self.func_mapping__[func_name_or_addr]
        if args == ():
            arg_bytes = b''
        else:
            arg_bytes = encode_param(abi.param_types, args)
        return join_param(self.contract_addr__, abi.func_addr, arg_bytes)

    def __getattr__(self, func_name_or_addr: str) -> "Functor":
        """
        选中一个合约方法. (仅在找不到名字时才会进入此函数)

        :param func_name_or_addr: 合约方法名或方法地址.
        :return: Functor
        """
        if func_name_or_addr.startswith('__'):  # 请求未实现的内部属性或方法
            raise AttributeError(func_name_or_addr)
        return self[func_name_or_addr]

    def __getitem__(self, func_name_or_addr: str) -> "Functor":
        """
        contract_obj['xxx'] 选中一个合约函数.

        :param func_name_or_addr: 合约方法名或方法地址.
        :return: Functor
        """
        abi = self.func_mapping__.get(func_name_or_addr)
        if abi is None:
            raise KeyError(f'function `{func_name_or_addr}` is not registered in Contract: `{self.class_name__}`')
        return Functor(self, abi.func_addr)


class Functor:
    """合约函数的封装."""

    def __init__(self, proxy: ContractProxy, func_addr: str):
        """
        初始化.

        :param proxy: ContractProxy对象
        :param func_addr: 合约函数地址
        """
        self.proxy = proxy
        self.func_addr = func_addr

    def __call__(self, *args):
        """
        发起合约调用.

        :param argument: 实际参数, 注意必须把所有参数都放在tuple中, 比如('abc', 1)
        :return: 如果是普通函数调用, 返回交易回执, '0x...'. 如果是只读函数调用, 返回解码后的python结果
        """
        return self.proxy.do_call_func__(self.func_addr, args)

    @property
    def name(self) -> str:
        return self.proxy.func_mapping__[self.func_addr].func_name

    @property
    def address(self) -> str:
        return self.func_addr

    @property
    def param_types(self) -> str:
        return self.proxy.func_mapping__[self.func_addr].param_types

    @property
    def return_types(self) -> str:
        return self.proxy.func_mapping__[self.func_addr].return_types

    @property
    def mutable(self) -> bool:
        return self.proxy.func_mapping__[self.func_addr].mutable

    @property
    def quota(self) -> int:
        return self.proxy.func_mapping__[self.func_addr].quota

    @quota.setter
    def quota(self, new_quota: int):
        self.proxy.func_mapping__[self.func_addr].quota = new_quota
