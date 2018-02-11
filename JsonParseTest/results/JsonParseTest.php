<?php

$obj1 = json_decode("{ \"a\":1, \"b\":2 }");
if (!is_object($obj1)) {
    throw new Exception("expected to be object!");
}
$obj1_props = array_keys((array)$obj1);
if (count($obj1_props) != 2) {
    throw new Exception("expected 2 properties");
}
if ($obj1_props[0] != "a") {
    throw new Exception("expected first property to be named 'a'");
}
$obj1_prop0_value = $obj1->{$obj1_props[0]};
if (!is_numeric($obj1_prop0_value) || $obj1_prop0_value != 1) {
    throw new Exception("expected 'a' to be 1 (number)");
}
print(("b = " . ($obj1->{"b"}) . "") . "\n");