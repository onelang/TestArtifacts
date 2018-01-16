<?php

class BasePrinter {
    function getValue() {
        return "Base";
    }
    
    function printIt() {
        print(("BasePrinter: " . ($this->getValue()) . "") . "\n");
    }
    
    function someBaseFunc() {
        return 42;
    }
}

class ChildPrinter extends BasePrinter {
    function getValue() {
        return "Child";
    }
}

class TestClass {
    function getPrinter($name) {
        $result = $name == "child" ? new ChildPrinter() : (new BasePrinter());
        return $result;
    }
    
    function testMethod() {
        $base_printer = $this->getPrinter("base");
        $child_printer = $this->getPrinter("child");
        $base_printer->printIt();
        $child_printer->printIt();
        print(("" . ($base_printer->someBaseFunc()) . " == " . ($child_printer->someBaseFunc()) . "") . "\n");
    }
}

try {
    $c = new TestClass();
    $c->testMethod();
} catch (Exception $err) {
    echo 'Exception: ' . $err->getMessage() . "\n";
}