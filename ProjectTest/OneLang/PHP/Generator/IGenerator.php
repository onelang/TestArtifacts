<?php

namespace Generator\IGenerator;

use Generator\GeneratedFile\GeneratedFile;
use One\Ast\Types\Package;

interface IGenerator {
    function getLangName();
    
    function getExtension();
    
    function generate($pkg);
}
