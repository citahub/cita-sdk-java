pragma solidity ^0.4.19;

contract SampleContract {
    string value;

    function SampleContract() public {
        value = "hello world";
    }

    function getValue() public view returns (string){
        return value;
    }

    function setValue(string str) public {
        value = str;
    }
}
