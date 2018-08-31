pragma solidity ^0.4.0;
contract SimpleGet {
    bytes32[5] val;
    bytes32 a;
    function setVal() public {
        for (uint i=0; i<5; i++) {
            val[i] = 0x12121212;
        }
        a = 0x12121212;
    }

    function getSingleVal() public view returns(bytes32) {
        return a;
    }

    function getVal() public view returns(bytes32,bytes32[5]) {
        return (a,val);
    }
}
