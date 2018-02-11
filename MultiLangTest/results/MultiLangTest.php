<?php

class Calculator {
    function calc() {
        return 4;
    }
}

print("Hello!" . "\n");

$calc = new Calculator();
print("n = " . $calc->calc() . "\n");

$arr = array(1, 2, 3);
print("arr[1] = " . $arr[1] . "\n");