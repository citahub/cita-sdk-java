pragma solidity ^0.4.19;
import "./SafeMath.sol";

contract Advance {
    using SafeMath for uint256;
    uint256 count = 0;

    function add() {
        count = count.add(1);
    }

    function get() constant returns(uint256) {
        return count;
    }

    function reset() {
        count = 0;
    }
}
