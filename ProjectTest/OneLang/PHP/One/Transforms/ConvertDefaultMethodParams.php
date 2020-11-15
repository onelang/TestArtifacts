<?php

namespace One\Transforms\ConvertDefaultMethodParams;

use One\AstTransformer\AstTransformer;

class ConvertDefaultMethodParams extends AstTransformer {
    function __construct() {
        parent::__construct("ConvertDefaultMethodParams");
        
    }
}
