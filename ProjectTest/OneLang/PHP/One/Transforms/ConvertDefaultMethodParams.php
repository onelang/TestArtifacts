<?php

namespace One\Transforms\ConvertDefaultMethodParams;

use One\Ast\Expressions\Expression;
use One\Ast\Expressions\IMethodCallExpression;
use One\Ast\Expressions\InstanceMethodCallExpression;
use One\Ast\Expressions\StaticMethodCallExpression;
use One\Ast\Types\IMethodBase;
use One\AstTransformer\AstTransformer;

class ConvertDefaultMethodParams extends AstTransformer {
    function __construct() {
        parent::__construct("ConvertDefaultMethodParams");
        
    }
    
    protected function visitExpression($expr) {
        if ($expr instanceof InstanceMethodCallExpression || $expr instanceof StaticMethodCallExpression) {
            $methodCall = ($expr);
            for ($i = count($methodCall->args); $i < count($methodCall->method->parameters); $i++) {
                $init = $methodCall->method->parameters[$i]->initializer;
                if ($init === null)
                    $this->errorMan->throw("Missing default value for parameter #" . $i + 1 . "!");
                $methodCall->args[] = $init;
            }
        }
        return null;
    }
}
