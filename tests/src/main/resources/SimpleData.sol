pragma solidity ^0.4.24;

contract SimpleData {
    uint256 public userId;
    bytes public userName;
    string public userDesc;
    address public userAddr;

    constructor(uint256 number, bytes name, string desc, address addr)
        public
    {
        userId = number;
        userName = name;
        userDesc = desc;
        userAddr = addr;
    }
}
