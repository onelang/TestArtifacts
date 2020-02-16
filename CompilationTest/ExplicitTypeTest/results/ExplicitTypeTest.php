<?php

class TestClass {
    function testMethod() {
        $op = "x";
        print(strlen($op) . "\n");
    }
}

try {
    $c = new TestClass();
    $c->testMethod();
} catch (Exception $err) {
    echo 'Exception: ' . $err->getMessage() . "\n";
}