pragma solidity ^0.4.19;

contract Advance {
    uint count = 0;

    function add() {
        count += 1;
    }

    function get() constant returns(uint) {
        return count;
    }

    function reset() {
        count = 0;
    }
}
