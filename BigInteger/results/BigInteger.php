<?php

class MathUtils {
    static function calc($n) {
        $result = 1;
        for ($i = 2; $i <= $n; $i++) {
            $result = $result * $i;
            if ($result > 10) {
                $result = $result >> 2;
            }
        }
        return $result;
    }
    
    static function calcBig($n) {
        $result = OneBigInteger::fromInt(1);
        for ($i = 2; $i <= $n; $i++) {
            $result = $result * $i + 123;
            $result = $result + $result;
            if ($result > 10) {
                $result = $result >> 2;
            }
        }
        return $result;
    }
}


print(("5 -> " . (MathUtils::calc(5)) . ", 24 -> " . (MathUtils::calcBig(24)) . "") . "\n");