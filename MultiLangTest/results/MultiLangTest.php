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
$map = array(
  "a" => 2,
  "b" => 3,
);
print("map['a'] = " . $map["a"] . ", arr[1] = " . $arr[1] . "\n");

if ($arr[0] == 1) {
    print("TRUE-X" . "\n");
} else {
    print("FALSE" . "\n");
}

$sum = 0;
for ($i = 0; $i < 10; $i++) {
    $sum += $i + 2;
}
print($sum . "\n");