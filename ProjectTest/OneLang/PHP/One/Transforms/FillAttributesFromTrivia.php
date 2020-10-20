<?php

namespace One\Transforms\FillAttributesFromTrivia;

use One\Ast\Types\SourceFile;
use One\Ast\Types\IMethodBase;
use One\Ast\Types\IHasAttributesAndTrivia;
use One\Ast\Types\Package;
use One\Ast\Types\IMethodBaseWithTrivia;
use One\Ast\Statements\ForeachStatement;
use One\Ast\Statements\ForStatement;
use One\Ast\Statements\IfStatement;
use One\Ast\Statements\Block;
use One\Ast\Statements\WhileStatement;
use One\Ast\Statements\DoStatement;

class FillAttributesFromTrivia {
    static function processTrivia($trivia) {
        $result = Array();
        if ($trivia !== null && $trivia !== "") {
            $regex = new \OneLang\RegExp("(?:\\n|^)\\s*(?://|#|/\\*\\*?)\\s*@([a-z0-9_.-]+) ?((?!\\n|\\*/|$).+)?");
            while (true) {
                $match = $regex->exec($trivia);
                if ($match === null)
                    break;
                if (array_key_exists($match[1], $result))
                    $result[$match[1]] = @$result[$match[1]] ?? null . "\n" . $match[2];
                else
                    $result[$match[1]] = $match[2] ?? "true";
            }
        }
        return $result;
    }
    
    private static function process($items) {
        foreach ($items as $item)
            $item->attributes = FillAttributesFromTrivia::processTrivia($item->leadingTrivia);
    }
    
    private static function processBlock($block) {
        if ($block === null)
            return;
        FillAttributesFromTrivia::process($block->statements);
        foreach ($block->statements as $stmt) {
            if ($stmt instanceof ForeachStatement)
                FillAttributesFromTrivia::processBlock($stmt->body);
            else if ($stmt instanceof ForStatement)
                FillAttributesFromTrivia::processBlock($stmt->body);
            else if ($stmt instanceof WhileStatement)
                FillAttributesFromTrivia::processBlock($stmt->body);
            else if ($stmt instanceof DoStatement)
                FillAttributesFromTrivia::processBlock($stmt->body);
            else if ($stmt instanceof IfStatement) {
                FillAttributesFromTrivia::processBlock($stmt->then);
                FillAttributesFromTrivia::processBlock($stmt->else_);
            }
        }
    }
    
    private static function processMethod($method) {
        if ($method === null)
            return;
        FillAttributesFromTrivia::process(array($method));
        FillAttributesFromTrivia::process($method->parameters);
        FillAttributesFromTrivia::processBlock($method->body);
    }
    
    static function processFile($file) {
        FillAttributesFromTrivia::process($file->imports);
        FillAttributesFromTrivia::process($file->enums);
        FillAttributesFromTrivia::process($file->interfaces);
        FillAttributesFromTrivia::process($file->classes);
        FillAttributesFromTrivia::processBlock($file->mainBlock);
        
        foreach ($file->interfaces as $intf)
            foreach ($intf->methods as $method)
                FillAttributesFromTrivia::processMethod($method);
        
        foreach ($file->classes as $cls) {
            FillAttributesFromTrivia::processMethod($cls->constructor_);
            FillAttributesFromTrivia::process($cls->fields);
            FillAttributesFromTrivia::process($cls->properties);
            foreach ($cls->properties as $prop) {
                FillAttributesFromTrivia::processBlock($prop->getter);
                FillAttributesFromTrivia::processBlock($prop->setter);
            }
            foreach ($cls->methods as $method)
                FillAttributesFromTrivia::processMethod($method);
        }
    }
    
    static function processPackage($pkg) {
        foreach (array_values($pkg->files) as $file)
            FillAttributesFromTrivia::processFile($file);
    }
}
