<?php

class TestClass {
    function getResult() {
        $map_obj = array(
          "x" => 5,
        );
        //let containsX = "x" in mapObj;
        //delete mapObj["x"];
        $map_obj["x"] = 3;
        return $map_obj["x"];
    }
    
    function testMethod() {
        print(("Result = " . ($this->getResult()) . "") . "\n");
    }
}

try {
    $c = new TestClass();
    $c->testMethod();
} catch (Exception $err) {
    echo 'Exception: ' . $err->getMessage() . "\n";
}