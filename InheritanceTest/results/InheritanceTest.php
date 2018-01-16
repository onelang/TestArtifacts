<?php

class BasePrinter {
    public $num_value = 42;

    function getValue() {
        return "Base";
    }
    
    function printIt() {
        print(("BasePrinter: " . ($this->getValue()) . "") . "\n");
    }
    
    function someBaseFunc() {
        return $this->num_value;
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
        
        $base_p2 = new BasePrinter();
        $child_p2 = new ChildPrinter();
        print(("" . ($base_p2->num_value) . " == " . ($child_p2->num_value) . "") . "\n");
    }
}

try {
    $c = new TestClass();
    $c->testMethod();
} catch (Exception $err) {
    echo 'Exception: ' . $err->getMessage() . "\n";
}