<?php

class Calculator {
    function factor($n) {
        if ($n <= 1) {
            return 1;
        } else {
            return $this->factor($n - 1) * $n;
        }
    }
}

print("Hello!" . "\n");

$arr = array(1, 2, 3);
$arr[] = 4;

print("n = " . count($arr) . ", arr[0] = " . $arr[0] . "\n");

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

$calc = new Calculator();
print("5! = " . $calc->factor(5) . "\n");