<?php

class TestClass {
    function testMethod() {
        $a = true;
        $b = false;
        $c = $a && $b;
        $d = $a || $b;
        print(("a: " . (($a) ? "true" : "false") . ", b: " . (($b) ? "true" : "false") . ", c: " . (($c) ? "true" : "false") . ", d: " . (($d) ? "true" : "false") . "") . "\n");
    }
}

try {
    $c = new TestClass();
    $c->testMethod();
} catch (Exception $err) {
    echo 'Exception: ' . $err->getMessage() . "\n";
}