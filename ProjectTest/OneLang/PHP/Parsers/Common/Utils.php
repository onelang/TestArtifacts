<?php

namespace Parsers\Common\Utils;

class Utils {
    static function getPadLen($line) {
        for ($i = 0; $i < strlen($line); $i++) {
            if ($line[$i] !== " ")
                return $i;
        }
        return -1;
    }
    
    static function deindent($str) {
        $lines = preg_split("/\\n/", $str);
        if (count($lines) === 1)
            return $str;
        
        if (Utils::getPadLen($lines[0]) === -1)
            array_shift($lines);
        
        $minPadLen = 9999;
        foreach (array_values(array_filter(array_map(function ($x) { return Utils::getPadLen($x); }, $lines), function ($x) { return $x !== -1; })) as $padLen) {
            if ($padLen < $minPadLen)
                $minPadLen = $padLen;
        }
        $newStr = implode("\n", array_map(function ($x) use ($minPadLen) { return strlen($x) !== 0 ? substr($x, $minPadLen) : $x; }, $lines));
        return $newStr;
    }
}