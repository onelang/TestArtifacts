<?php

namespace One\Compiler;

use One\Ast\Types\Workspace;
use One\Ast\Types\Package;
use One\Ast\Types\SourcePath;
use One\Ast\Types\SourceFile;
use One\Ast\Types\ExportedScope;
use One\Ast\Types\LiteralTypes;
use One\Ast\Types\Class_;
use One\Ast\Types\ExportScopeRef;
use Parsers\TypeScriptParser2\TypeScriptParser2;
use StdLib\PackageManager\PackageManager;
use StdLib\PackagesFolderSource\PackagesFolderSource;
use One\Transforms\FillParent\FillParent;
use One\Transforms\FillAttributesFromTrivia\FillAttributesFromTrivia;
use One\Transforms\ResolveGenericTypeIdentifiers\ResolveGenericTypeIdentifiers;
use One\Transforms\ResolveUnresolvedTypes\ResolveUnresolvedTypes;
use One\Transforms\ResolveImports\ResolveImports;
use One\Transforms\ConvertToMethodCall\ConvertToMethodCall;
use One\Transforms\ResolveIdentifiers\ResolveIdentifiers;
use One\Transforms\InstanceOfImplicitCast\InstanceOfImplicitCast;
use One\Transforms\DetectMethodCalls\DetectMethodCalls;
use One\Transforms\InferTypes\InferTypes;
use One\Transforms\CollectInheritanceInfo\CollectInheritanceInfo;
use One\Transforms\FillMutabilityInfo\FillMutabilityInfo;
use One\AstTransformer\AstTransformer;
use One\ITransform\ITransformer;
use One\Transforms\LambdaCaptureCollector\LambdaCaptureCollector;

interface ICompilerHooks {
    function afterStage($stageName);
}

class Compiler {
    public $pacMan;
    public $workspace;
    public $nativeFile;
    public $nativeExports;
    public $projectPkg;
    public $hooks;
    
    function __construct()
    {
        $this->pacMan = null;
        $this->workspace = null;
        $this->nativeFile = null;
        $this->nativeExports = null;
        $this->projectPkg = null;
        $this->hooks = null;
    }
    
    function init($packagesDir) {
        $this->pacMan = new PackageManager(new PackagesFolderSource($packagesDir));
        $this->pacMan->loadAllCached();
    }
    
    function setupNativeResolver($content) {
        $this->nativeFile = TypeScriptParser2::parseFile($content);
        $this->nativeExports = Package::collectExportsFromFile($this->nativeFile, true);
        (new FillParent())->visitSourceFile($this->nativeFile);
        FillAttributesFromTrivia::processFile($this->nativeFile);
        (new ResolveGenericTypeIdentifiers())->visitSourceFile($this->nativeFile);
        (new ResolveUnresolvedTypes())->visitSourceFile($this->nativeFile);
        (new FillMutabilityInfo())->visitSourceFile($this->nativeFile);
    }
    
    function newWorkspace($pkgName = "@") {
        $this->workspace = new Workspace();
        foreach ($this->pacMan->interfacesPkgs as $intfPkg) {
            $libName = $intfPkg->interfaceYaml->vendor . "." . $intfPkg->interfaceYaml->name . "-v" . $intfPkg->interfaceYaml->version;
            $this->addInterfacePackage($libName, $intfPkg->definition);
        }
        
        $this->projectPkg = new Package($pkgName, false);
        $this->workspace->addPackage($this->projectPkg);
    }
    
    function addInterfacePackage($libName, $definitionFileContent) {
        $libPkg = new Package($libName, true);
        $file = TypeScriptParser2::parseFile($definitionFileContent, new SourcePath($libPkg, Package::$INDEX));
        $this->setupFile($file);
        $libPkg->addFile($file, true);
        $this->workspace->addPackage($libPkg);
    }
    
    function setupFile($file) {
        $file->addAvailableSymbols($this->nativeExports->getAllExports());
        $file->literalTypes = new LiteralTypes(($file->availableSymbols->get("TsBoolean"))->type, ($file->availableSymbols->get("TsNumber"))->type, ($file->availableSymbols->get("TsString"))->type, ($file->availableSymbols->get("RegExp"))->type, ($file->availableSymbols->get("TsArray"))->type, ($file->availableSymbols->get("TsMap"))->type, ($file->availableSymbols->get("Error"))->type, ($file->availableSymbols->get("Promise"))->type);
        $file->arrayTypes = array(($file->availableSymbols->get("TsArray"))->type, ($file->availableSymbols->get("IterableIterator"))->type, ($file->availableSymbols->get("RegExpExecArray"))->type, ($file->availableSymbols->get("TsString"))->type, ($file->availableSymbols->get("Set"))->type);
    }
    
    function addProjectFile($fn, $content) {
        $file = TypeScriptParser2::parseFile($content, new SourcePath($this->projectPkg, $fn));
        $this->setupFile($file);
        $this->projectPkg->addFile($file);
    }
    
    function processWorkspace() {
        foreach (array_values(array_filter(array_values($this->workspace->packages), function ($x) { return $x->definitionOnly; })) as $pkg) {
            // sets method's parentInterface property
            (new FillParent())->visitPackage($pkg);
            FillAttributesFromTrivia::processPackage($pkg);
            (new ResolveGenericTypeIdentifiers())->visitPackage($pkg);
            (new ResolveUnresolvedTypes())->visitPackage($pkg);
        }
        
        (new FillParent())->visitPackage($this->projectPkg);
        if ($this->hooks !== null)
            $this->hooks->afterStage("FillParent");
        
        FillAttributesFromTrivia::processPackage($this->projectPkg);
        if ($this->hooks !== null)
            $this->hooks->afterStage("FillAttributesFromTrivia");
        
        ResolveImports::processWorkspace($this->workspace);
        if ($this->hooks !== null)
            $this->hooks->afterStage("ResolveImports");
        
        $transforms = array();
        $transforms[] = new ResolveGenericTypeIdentifiers();
        $transforms[] = new ConvertToMethodCall();
        $transforms[] = new ResolveUnresolvedTypes();
        $transforms[] = new ResolveIdentifiers();
        $transforms[] = new InstanceOfImplicitCast();
        $transforms[] = new DetectMethodCalls();
        $transforms[] = new InferTypes();
        $transforms[] = new CollectInheritanceInfo();
        $transforms[] = new FillMutabilityInfo();
        $transforms[] = new LambdaCaptureCollector();
        foreach ($transforms as $trans) {
            $trans->visitPackage($this->projectPkg);
            if ($this->hooks !== null)
                $this->hooks->afterStage($trans->name);
        }
    }
}
