<?php

namespace Utils\ArrayHelper;

class ArrayHelper {
    static function sortBy<T>($items, $keySelector) {
        return $items->sort(function ($a, $b) { return $keySelector($a) - $keySelector($b); });
    }
    
    static function removeLastN<T>($items, $count) {
        $items->splice(count($items) - $count, $count);
    }
}
