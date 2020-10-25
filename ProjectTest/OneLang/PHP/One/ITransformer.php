<?php

namespace One\ITransformer;

use One\Ast\Types\Package;

interface ITransformer {
    function visitPackage($pkg);
}
