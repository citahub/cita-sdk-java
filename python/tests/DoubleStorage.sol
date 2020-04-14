pragma solidity ^0.4.24;

contract Dummy {  // forward declaration
    string dummy;
}

contract DoubleStorage {
    uint public x;
    uint public y;

    constructor(uint _x, uint _y) public {
        x = _x;
        y = _y;
    }
}
