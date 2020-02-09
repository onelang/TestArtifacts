<?php

class TestClass {
    function testMethod() {
        $value = 1 + 2 * 3 - 4;
        $map_ = array(
          "a" => 5,
          "b" => 6,
        );
        $text = "Hello world! value = " . $value . ", map[a] = " . $map_["a"];
        print($text . "\n");
    }
}

try {
    $c = new TestClass();
    $c->testMethod();
} catch (Exception $err) {
    echo 'Exception: ' . $err->getMessage() . "\n";
}