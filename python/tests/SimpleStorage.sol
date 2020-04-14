pragma solidity ^0.4.24;

contract Dummy {  // forward declaration
    string dummy;
}

contract SimpleStorage {
    uint x;

    event Added(uint x);

    constructor(uint _x) public {
        x = _x;
    }

    function set(uint _x) public {
        x = _x;
    }

    function reset() public {
        x = 0;
    }

    function add(uint _x) public returns (uint) {
        emit Added(_x);
        x += _x;
        return x;
    }

    function add_by_vec(uint[] a, uint[] b) public returns (uint) {
        require(a.length == b.length);
        for (uint i = 0; i < a.length; i++) {
            x += a[i] * b[i];
        }
        return x;
    }

    // 只读方法.
    function get() public constant returns (uint) {
        return x;
    }
}
