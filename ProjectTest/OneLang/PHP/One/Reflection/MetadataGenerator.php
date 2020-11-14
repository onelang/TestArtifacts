<?php

namespace One\Reflection\MetadataGenerator;

use One\Ast\Types\Package;

class MetadataGenerator {
    public $pkg;
    
    function __construct($pkg) {
        $this->pkg = $pkg;
    }
}
