<?php

namespace One\ITransform;

use One\Ast\Types\Package;

interface ITransformer {
    function visitPackage($pkg);
}
