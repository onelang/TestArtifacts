<?php

namespace One\Transforms\ResolveImports;

use One\Ast\Types\Workspace;
use One\Ast\Types\UnresolvedImport;

class ResolveImports {
    static function processWorkspace($ws) {
        foreach (array_values($ws->packages) as $pkg)
            foreach (array_values($pkg->files) as $file)
                foreach ($file->imports as $imp) {
                    $impPkg = $ws->getPackage($imp->exportScope->packageName);
                    $scope = $impPkg->getExportedScope($imp->exportScope->scopeName);
                    $imp->imports = $imp->importAll ? $scope->getAllExports() : array_map(function ($x) use ($scope) { return $x instanceof UnresolvedImport ? $scope->getExport($x->name) : $x; }, $imp->imports);
                    $file->addAvailableSymbols($imp->imports);
                }
    }
}
