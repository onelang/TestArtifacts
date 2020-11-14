<?php

namespace One\Serialization\ObjectSerializer;

use One\Ast\AstTypes\ClassType;
use One\Ast\Interfaces\IType;
use One\Ast\Types\Class_;
use One\Ast\Types\Package;

class JsonSerializer {
    static function serialize($obj) {
        if ($obj->type instanceof ClassType) {
            $members = array();
            foreach ($obj->type->decl->fields as $field)
                $members[] = "\"" . $field->name . "\": " . JsonSerializer::serialize($obj->getField($field->name));
            return count($members) === 0 ? "{}" : "{\n" . Global.pad(/* TODO: UnresolvedMethodCallExpression */ $members->join(",\n")) . "\n}";
        }
        else
            return "\"<UNKNOWN-TYPE>\"";
    }
}
