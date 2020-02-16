<?php

class TestClass {
    function testMethod() {
        $result = array("y");
        $map = array(
          "x" => 5,
        );
        $keys = array_keys($map);
        print($result[0] . "\n");
        print($keys[0] . "\n");
    }
}

try {
    $c = new TestClass();
    $c->testMethod();
} catch (Exception $err) {
    echo 'Exception: ' . $err->getMessage() . "\n";
}