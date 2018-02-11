<?php

class XorByte {
    public $xor_value;

    function __construct($xor_value) {
        $this->xor_value = $xor_value;
    }

    function decode($src) {
        $dest = array();
        
        for ($i = 0; $i < count($src); $i++) {
            $dest[] = $src[$i] ^ $this->xor_value;
        }
        
        return $dest;
    }
}

class Base64 {
    function decode($src) {
        $dest = array();
        
        // 4 base64 chars => 3 bytes
        for ($i = 0; $i < count($src); $i += 4) {
            $ch0 = $this->decodeChar($src[$i]);
            $ch1 = $this->decodeChar($src[$i + 1]);
            $ch2 = $this->decodeChar($src[$i + 2]);
            $ch3 = $this->decodeChar($src[$i + 3]);
            
            $trinity = ($ch0 << 18) + ($ch1 << 12) + ($ch2 << 6) + ($ch3);
            
            $dest[] = $trinity >> 16;
            $dest[] = ($trinity >> 8) & 0xff;
            $dest[] = $trinity & 0xff;
        }
        
        return $dest;
    }
    
    function decodeChar($ch) {
        $value = -1;
        if ($ch >= 65 && $ch <= 90) {
            // `A-Z` => 0..25
            $value = $ch - 65;
        } elseif ($ch >= 97 && $ch <= 122) {
            // `a-z` => 26..51
            $value = $ch - 97 + 26;
        } elseif ($ch >= 48 && $ch <= 57) {
            // `0-9` => 52..61
            $value = $ch - 48 + 52;
        } elseif ($ch == 43 || $ch == 45) {
            // `+` or `-` => 62
            $value = 62;
        } elseif ($ch == 47 || $ch == 95) {
            // `/` or `_` => 63
            $value = 63;
        } elseif ($ch == 61) {
            // `=` => padding
            $value = 0;
        } else {
        }
        return $value;
    }
}

class TestClass {
    function testMethod() {
        $src1 = array(4, 5, 6);
        $decoder = new XorByte(0xff);
        $dst1 = $decoder->decode($src1);
        foreach ($dst1 as $x) {
            print(($x) . "\n");
        }
        
        print(("|") . "\n");
        
        $src2 = array(97, 71, 86, 115, 98, 71, 56, 61);
        $decoder2 = new Base64();
        $dst2 = $decoder2->decode($src2);
        foreach ($dst2 as $x) {
            print(($x) . "\n");
        }
    }
}

try {
    $c = new TestClass();
    $c->testMethod();
} catch (Exception $err) {
    echo 'Exception: ' . $err->getMessage() . "\n";
}