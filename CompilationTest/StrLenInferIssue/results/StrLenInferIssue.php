<?php

class StrLenInferIssue {
    static function test($str) {
        return strlen($str);
    }
}

print(StrLenInferIssue::test("hello") . "\n");