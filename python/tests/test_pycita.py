#!/usr/bin/env python

"""测试CitaClient."""
from pathlib import Path
import json
import pytest

from pycita import CitaClient, ContractClass, equal_param, encode_param, decode_param, join_param, param_to_bytes, param_to_str


# uncomment下述代码, 会开始输出日志
# import logging
# logging.basicConfig(level=logging.INFO)
# 使用了SimpleStorage.sol作为样本

client = CitaClient('bin/cita-cli', 'http://192.168.127.216:1338')


def test_create_key():
    r = client.create_key()
    assert len(r['address'][2:]) == 20 * 2
    assert len(r['public'][2:]) == 64 * 2
    assert len(r['private'][2:]) == 32 * 2


def test_encode_decode():
    r = encode_param('string', 'abc')
    ans = '0x000000000000000000000000000000000000000000000000000000000000002000000000000000000000000000000000000000000000000000000000000000036162630000000000000000000000000000000000000000000000000000000000'
    assert param_to_str(r) == ans
    assert param_to_bytes(ans) == r
    assert equal_param(r, ans) is True
    assert decode_param('string', r) == 'abc'

    r = encode_param('(int,bool)', (-1, True))
    ans = '0xffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffff0000000000000000000000000000000000000000000000000000000000000001'
    assert equal_param(r, ans) is True
    assert decode_param('(int,bool)', r) == (-1, True)

    r = encode_param('int', 1)
    ans = '0x0000000000000000000000000000000000000000000000000000000000000001'
    assert equal_param(r, ans) is True
    assert decode_param('int', r) == 1

    ans = '0x0badf00d'
    assert equal_param(b'\x0b\xad\xf0\x0d', '0x0badf00d') is True
    assert join_param(b'\x0b\xad', '0xf00d') == '0x0badf00d'
    assert join_param(b'\x0b\xad', b'\xf0\x0d') == ans

    r = encode_param('', '')
    assert r == b''

    assert equal_param(b'\x00', b'\x00') is True
    assert encode_param('', ()) == b''
    assert decode_param('', b'') == ()
    assert join_param() == ''


def test_block_number():
    assert client.get_latest_block_number() > 0


def test_peer_count():
    assert client.get_peer_count() >= 0


def test_peers():
    assert isinstance(client.get_peers(), dict)


def test_get_block():
    block = client.get_block_by_number(0)
    assert block['header']['number'] == '0x0'
    h = block['hash']

    block2 = client.get_block_by_hash(h)
    assert block == block2


def test_get_transcation_receipt_bad():
    # 读取不存在的交易
    assert client.get_transcation_receipt(b'\x00' * 32, 0) == {}
    with pytest.raises(RuntimeError, match='timeout'):
        client.get_transcation_receipt(b'\x00' * 32, 1)

    with pytest.raises(RuntimeError, match='jsonrpc failed'):
        client.get_transcation_receipt(b'\x00')


def test_get_abi_bad():
    # 不存在的地址
    r = client.get_abi(b'\x00' * 20)
    assert r == []
    r = client.get_code(b'\x00' * 20)
    assert r == b''


def test_decode_transaction_content():
    r = client.decode_transaction_content('0x0aae03122032626131393465636637633034323333623862383764346633353938343061311880ade2042097ce0d2aa4024b2173f53863643239306334326138383430303361396234643566613263313665346138000000000000000000000000000000000000000000000000000000000000004000000000000000000000000000000000000000000000000000000000000000a1789c8bae562ac9cc4d55b25232323032d03530d4353650d2514a2bcacf058abdd8d6fa7cd7f2a7adcdcf364c313035303bb4fd65d364a0205004a82825b53819bfa2a7fd4d4f76f43ddfd4f964c7dca7b317bd689cf27442c7d309139fed9cf6b467fad3b5739fb5cc7fba03a866edd3fdcd4f7b76bf9cb9e859ef84175b173c6e986064f2b861e2931dab9e752d793e7bddcba973946a7586926363010adec36d000000000000000000000000000000000000000000000000000000000000003220000000000000000000000000000000000000000000000000000000000000000040024a14a26afce7d4bf0308b38970b1dadf6adfea2c7501522000000000000000000000000000000000000000000000000000000000000000011241f098ce3832fc5bd467083119292ec1a7e2239784f7734204fdf5e01d84a4e63c2b37cb4a05ef919c5ada1db6775c86dadacaf69f53fd54f09efb326d6b658e0601')
    assert r['transaction']['nonce'] == '2ba194ecf7c04233b8b87d4f359840a1'


def test_client():
    print('读取合约文件...')
    simple_class = ContractClass(Path('tests/SimpleStorage.sol'), client)

    print('创建账户...')
    account = client.create_key()
    user_addr = account['address']
    private_key = account['private']

    print('合约实例化...')
    value = 100
    simple_obj, contract_addr, tx_hash = simple_class.instantiate(private_key, value)
    with pytest.raises(RuntimeError, match='timeout'):
        client.confirm_transaction(tx_hash, 1)  # 等待超时.
    client.confirm_transaction(tx_hash)

    tx_hash = client.store_abi(private_key, contract_addr, simple_class.get_raw_abi())
    client.confirm_transaction(tx_hash)
    client.get_code(contract_addr) == simple_class.get_code()
    client.get_abi(contract_addr) == json.loads(simple_class.get_raw_abi())

    print('修改合约状态...')
    new_value = 200
    tx_hash = simple_obj.set(new_value)
    client.get_transcation_receipt(tx_hash)

    # 读取状态
    r = client.call_readonly_func(contract_addr, simple_obj.get.address, from_addr=user_addr)
    assert decode_param('uint', r) == value
    assert simple_obj.get() == value  # 因为交易还未被确认.
    simple_obj.set_call_mode('pending')
    assert simple_obj.get() == new_value  # 读取未确认的数据.

    client.confirm_transaction(tx_hash)

    # 读取状态
    assert simple_obj.get() == new_value
    simple_obj.set_call_mode('latest')
    assert simple_obj.get() == new_value  # 交易已确认.


@pytest.mark.only
def test_single_call():
    """测试常见的合约调用操作."""
    print('读取合约文件...')
    simple_class = ContractClass(Path('tests/SimpleStorage.sol'), client)
    double_class = ContractClass(Path('tests/DoubleStorage.sol'), client)
    dummy_class = ContractClass(Path('tests/Dummy.sol'), client)
    assert simple_class.name == 'SimpleStorage'
    assert double_class.name == 'DoubleStorage'
    assert dummy_class.name == 'Dummy'

    print('创建账户...')
    account = client.create_key()
    user_addr = account['address']
    private_key = account['private']

    # 部署合约, 使用(0, 1, 2)个参数.
    print('合约实例化...')
    value = 100
    simple_obj, contract_addr, _ = simple_class.instantiate(private_key, value)
    double_obj, _, _ = double_class.instantiate(private_key, value, value * 10)
    dummy_obj, _, tx_hash = dummy_class.instantiate(private_key)

    # 绑定另一个合约对象到contract_addr, 这个对象的行为应该跟simple_obj一样
    ref_simple_obj = simple_class.bind(contract_addr, private_key)

    print('等待交易确认...')
    client.confirm_transaction(tx_hash)

    with pytest.raises(KeyError, match='not registered'):
        simple_obj.bad_call()

    with pytest.raises(AttributeError, match='__getstate__'):
        simple_obj.__getstate__()

    assert simple_obj.get() == value
    assert ref_simple_obj.get() == value  # 测试bind
    assert double_obj.x() == value
    assert double_obj.y() == value * 10

    assert client.get_transaction(tx_hash)['hash'] == tx_hash
    assert client.get_transaction_count(user_addr) == 3

    print('修改合约状态...')
    value = 200
    tx_hash = simple_obj.set(value)  # 使用1个参数.
    receipt = client.get_transcation_receipt(tx_hash)
    assert receipt['errorMessage'] is None
    client.confirm_transaction(tx_hash)
    assert simple_obj.get() == value

    v1 = [1, 2]
    v2 = [3, 4]
    ans = 1 * 3 + 2 * 4
    simple_obj.reset()  # 使用0个参数
    tx_hash = simple_obj.add_by_vec(v1, v2)  # 使用2个参数
    client.confirm_transaction(tx_hash)
    assert simple_obj.get() == ans

    print('修改Quota...')
    simple_obj.set.quota = 1  # 设置很小的Quota.
    assert simple_obj.set.quota == 1
    assert simple_obj.set.name == 'set'
    assert simple_obj.set.address.startswith('0x') and len(param_to_bytes(simple_obj.set.address)) == 4
    assert simple_obj.set.param_types == 'uint256'
    assert simple_obj.set.return_types == ''
    assert simple_obj.set.mutable is True
    assert simple_obj.add_by_vec.param_types == 'uint256[],uint256[]'
    assert simple_obj.add_by_vec.return_types == 'uint256'

    tx_hash = simple_obj.set(300)
    with pytest.raises(RuntimeError, match='Not enough base quota.'):
        client.get_transcation_receipt(tx_hash)

    assert simple_obj.get() == ans


def test_batch_call():
    """测试常见的合约批量调用操作."""
    print('读取合约文件...')
    simple_class = ContractClass(Path('tests/SimpleStorage.sol'), client)
    double_class = ContractClass(Path('tests/DoubleStorage.sol'), client)
    dummy_class = ContractClass(Path('tests/Dummy.sol'), client)

    print('创建账户...')
    account = client.create_key()
    private_key = account['private']

    print('批量实例化...')
    init_values = [(), (), ()]
    obj_list = []
    for dummy_obj, contract_addr, tx_hash in dummy_class.batch_instantiate(private_key, obj_list):  # 使用0个参数
        client.get_transcation_receipt(tx_hash)

    init_values = [100, 200, (300,)]
    obj_list = []
    for value, (simple_obj, contract_addr, tx_hash) in zip(init_values, simple_class.batch_instantiate(private_key, init_values)):  # 使用1个参数
        client.confirm_transaction(tx_hash)
        if value == (300,):
            assert simple_obj.get() == value[0]
        else:
            assert simple_obj.get() == value
        obj_list.append(simple_obj)

    init_values = [(100, 200), (300, 400), (500, 600)]
    for (x, y), (double_obj, contract_addr, tx_hash) in zip(init_values, double_class.batch_instantiate(private_key, init_values)):  # 使用2个参数
        client.confirm_transaction(tx_hash)
        assert double_obj.x() == x
        assert double_obj.y() == y

    print('批量调用reset...')
    tx_code_list = [simple_obj.get_tx_code('reset')  # 使用0个参数
                    for simple_obj in obj_list]
    tx_hash = client.batch_call_func(private_key, tx_code_list)
    client.confirm_transaction(tx_hash)

    for simple_obj in obj_list:
        assert simple_obj.get() == 0

    print('批量调用set...')
    new_values = [101, 202, (303,)]
    tx_code_list = [simple_obj.get_tx_code('set', value)  # 使用1个参数
                    for value, simple_obj in zip(new_values, obj_list)]
    tx_hash = client.batch_call_func(private_key, tx_code_list)
    client.confirm_transaction(tx_hash)

    for value, simple_obj in zip(new_values, obj_list):
        if value == (303,):
            assert simple_obj.get() == value[0]
        else:
            assert simple_obj.get() == value


# 226, 249, 313, 337, 358, 403, 443, 458, 473, 544
