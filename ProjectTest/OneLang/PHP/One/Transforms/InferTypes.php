<?php

namespace One\Transforms\InferTypes;

use One\AstTransformer\AstTransformer;
use One\Ast\Expressions\Expression;
use One\Ast\Types\Package;
use One\Ast\Types\Property;
use One\Ast\Types\Field;
use One\Ast\Types\IMethodBase;
use One\Ast\Types\IVariableWithInitializer;
use One\Ast\Types\Lambda;
use One\Ast\Types\IVariable;
use One\Ast\Types\Method;
use One\Ast\Types\Class_;
use One\Ast\Types\SourceFile;
use One\Transforms\InferTypesPlugins\BasicTypeInfer\BasicTypeInfer;
use One\Transforms\InferTypesPlugins\Helpers\InferTypesPlugin\InferTypesPlugin;
use One\Transforms\InferTypesPlugins\ArrayAndMapLiteralTypeInfer\ArrayAndMapLiteralTypeInfer;
use One\Transforms\InferTypesPlugins\ResolveFieldAndPropertyAccess\ResolveFieldAndPropertyAccess;
use One\Transforms\InferTypesPlugins\ResolveMethodCalls\ResolveMethodCalls;
use One\Transforms\InferTypesPlugins\LambdaResolver\LambdaResolver;
use One\Ast\Statements\Statement;
use One\Ast\Statements\Block;
use One\Transforms\InferTypesPlugins\ResolveEnumMemberAccess\ResolveEnumMemberAccess;
use One\Transforms\InferTypesPlugins\InferReturnType\InferReturnType;
use One\Transforms\InferTypesPlugins\TypeScriptNullCoalesce\TypeScriptNullCoalesce;
use One\Transforms\InferTypesPlugins\InferForeachVarType\InferForeachVarType;
use One\Transforms\InferTypesPlugins\ResolveFuncCalls\ResolveFuncCalls;
use One\Transforms\InferTypesPlugins\NullabilityCheckWithNot\NullabilityCheckWithNot;
use One\Transforms\InferTypesPlugins\ResolveNewCall\ResolveNewCalls;
use One\Transforms\InferTypesPlugins\ResolveElementAccess\ResolveElementAccess;

class InferTypesStage {
    const INVALID = 1;
    const FIELDS = 2;
    const PROPERTIES = 3;
    const METHODS = 4;
}

class InferTypes extends AstTransformer {
    protected $stage;
    public $plugins;
    public $contextInfoIdx = 0;
    
    function __construct() {
        parent::__construct("InferTypes");
        $this->plugins = array();
        $this->addPlugin(new BasicTypeInfer());
        $this->addPlugin(new ArrayAndMapLiteralTypeInfer());
        $this->addPlugin(new ResolveFieldAndPropertyAccess());
        $this->addPlugin(new ResolveMethodCalls());
        $this->addPlugin(new LambdaResolver());
        $this->addPlugin(new InferReturnType());
        $this->addPlugin(new ResolveEnumMemberAccess());
        $this->addPlugin(new TypeScriptNullCoalesce());
        $this->addPlugin(new InferForeachVarType());
        $this->addPlugin(new ResolveFuncCalls());
        $this->addPlugin(new NullabilityCheckWithNot());
        $this->addPlugin(new ResolveNewCalls());
        $this->addPlugin(new ResolveElementAccess());
    }
    
    function processLambda($lambda) {
        parent::visitMethodBase($lambda);
    }
    
    function processMethodBase($method) {
        parent::visitMethodBase($method);
    }
    
    function processBlock($block) {
        parent::visitBlock($block);
    }
    
    function processVariable($variable) {
        parent::visitVariable($variable);
    }
    
    function processStatement($stmt) {
        parent::visitStatement($stmt);
    }
    
    function processExpression($expr) {
        parent::visitExpression($expr);
    }
    
    function addPlugin($plugin) {
        $plugin->main = $this;
        $plugin->errorMan = $this->errorMan;
        $this->plugins[] = $plugin;
    }
    
    protected function visitVariableWithInitializer($variable) {
        if ($variable->type !== null && $variable->initializer !== null)
            $variable->initializer->setExpectedType($variable->type);
        
        parent::visitVariableWithInitializer($variable);
        
        if ($variable->type === null && $variable->initializer !== null)
            $variable->type = $variable->initializer->getType();
        
        return null;
    }
    
    protected function runTransformRound($expr) {
        if ($expr->actualType !== null)
            return null;
        
        $this->errorMan->currentNode = $expr;
        
        $transformers = array_values(array_filter($this->plugins, function ($x) use ($expr) { return $x->canTransform($expr); }));
        if (count($transformers) > 1)
            $this->errorMan->throw("Multiple transformers found: " . implode(", ", array_map(function ($x) { return $x->name; }, $transformers)));
        if (count($transformers) !== 1)
            return null;
        
        $plugin = $transformers[0];
        $this->contextInfoIdx++;
        $this->errorMan->lastContextInfo = "[" . $this->contextInfoIdx . "] running transform plugin \"" . $plugin->name . "\"";
        try {
            $newExpr = $plugin->transform($expr);
            // expression changed, restart the type infering process on the new expression
            if ($newExpr !== null)
                $newExpr->parentNode = $expr->parentNode;
            return $newExpr;
        } catch (Exception $e) {
            $this->errorMan->currentNode = $expr;
            $this->errorMan->throw("Error while running type transformation phase: " . $e);
            return null;
        }
    }
    
    protected function detectType($expr) {
        foreach ($this->plugins as $plugin) {
            if (!$plugin->canDetectType($expr))
                continue;
            $this->contextInfoIdx++;
            $this->errorMan->lastContextInfo = "[" . $this->contextInfoIdx . "] running type detection plugin \"" . $plugin->name . "\"";
            $this->errorMan->currentNode = $expr;
            try {
                if ($plugin->detectType($expr))
                    return true;
            } catch (Exception $e) {
                $this->errorMan->throw("Error while running type detection phase: " . $e);
            }
        }
        return false;
    }
    
    protected function visitExpression($expr) {
        $transformedExpr = null;
        while (true) {
            $newExpr = $this->runTransformRound($transformedExpr ?? $expr);
            if ($newExpr === null || $newExpr === $transformedExpr)
                break;
            $transformedExpr = $newExpr;
        }
        // if the plugin did not handle the expression, we use the default visit method
        $expr2 = $transformedExpr !== null ? $transformedExpr : parent::visitExpression($expr) ?? $expr;
        
        if ($expr2->actualType !== null)
            return $expr2;
        
        $detectSuccess = $this->detectType($expr2);
        
        if ($expr2->actualType === null) {
            if ($detectSuccess)
                $this->errorMan->throw("Type detection failed, although plugin tried to handle it");
            else
                $this->errorMan->throw("Type detection failed: none of the plugins could resolve the type");
        }
        
        return $expr2;
    }
    
    protected function visitStatement($stmt) {
        $this->currentStatement = $stmt;
        
        foreach ($this->plugins as $plugin) {
            if ($plugin->handleStatement($stmt))
                return null;
        }
        
        return parent::visitStatement($stmt);
    }
    
    protected function visitField($field) {
        if ($this->stage !== InferTypesStage::FIELDS)
            return;
        parent::visitField($field);
    }
    
    protected function visitProperty($prop) {
        if ($this->stage !== InferTypesStage::PROPERTIES)
            return;
        
        foreach ($this->plugins as $plugin) {
            if ($plugin->handleProperty($prop))
                return;
        }
        
        parent::visitProperty($prop);
    }
    
    protected function visitMethodBase($method) {
        if ($this->stage !== InferTypesStage::METHODS)
            return;
        
        foreach ($this->plugins as $plugin) {
            if ($plugin->handleMethod($method))
                return;
        }
        
        parent::visitMethodBase($method);
    }
    
    protected function visitLambda($lambda) {
        if ($lambda->actualType !== null)
            return null;
        
        foreach ($this->plugins as $plugin) {
            if ($plugin->handleLambda($lambda))
                return $lambda;
        }
        
        return parent::visitLambda($lambda);
    }
    
    function runPluginsOn($expr) {
        return $this->visitExpression($expr);
    }
    
    protected function visitClass($cls) {
        if (@$cls->attributes["external"] ?? null === "true")
            return;
        parent::visitClass($cls);
    }
    
    function visitFiles($files) {
        foreach (array(InferTypesStage::FIELDS, InferTypesStage::PROPERTIES, InferTypesStage::METHODS) as $stage) {
            $this->stage = $stage;
            foreach ($files as $file)
                $this->visitFile($file);
        }
    }
}
