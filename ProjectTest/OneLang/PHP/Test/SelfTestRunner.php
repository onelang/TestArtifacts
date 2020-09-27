<?php

namespace Test\SelfTestRunner;

use onepkg\OneFile\OneFile;
use Generator\IGenerator\IGenerator;
use One\Compiler\Compiler;
use One\Compiler\ICompilerHooks;
use Test\PackageStateCapture\PackageStateCapture;

class CompilerHooks implements ICompilerHooks {
    public $stage = 0;
    public $compiler;
    public $baseDir;
    
    function __construct($compiler, $baseDir) {
        $this->compiler = $compiler;
        $this->baseDir = $baseDir;
    }
    
    function afterStage($stageName) {
        $state = new PackageStateCapture($this->compiler->projectPkg);
        $stageFn = $this->baseDir . "/test/artifacts/ProjectTest/OneLang/stages/" . $this->stage . "_" . $stageName . ".txt";
        $this->stage++;
        $stageSummary = $state->getSummary();
        $expected = OneFile::readText($stageFn);
        if ($stageSummary !== $expected) {
            OneFile::writeText($stageFn . "_diff.txt", $stageSummary);
            throw new \OneLang\Error("Stage result differs from expected: " . $stageName . " -> " . $stageFn);
        }
        else
            \OneLang\console::log("[+] Stage passed: " . $stageName);
    }
}

class SelfTestRunner {
    public $baseDir;
    
    function __construct($baseDir) {
        $this->baseDir = $baseDir;
    }
    
    function runTest($generator) {
        \OneLang\console::log("[-] SelfTestRunner :: START");
        $compiler = new Compiler();
        $compiler->init($this->baseDir . "packages/");
        $compiler->setupNativeResolver(OneFile::readText($this->baseDir . "langs/NativeResolvers/typescript.ts"));
        $compiler->newWorkspace("OneLang");
        
        $projDir = $this->baseDir . "src/";
        foreach (array_values(array_filter(OneFile::listFiles($projDir, true), function ($x) { return substr_compare($x, ".ts", strlen($x) - strlen(".ts"), strlen(".ts")) === 0; })) as $file)
            $compiler->addProjectFile($file, OneFile::readText($projDir . "/" . $file));
        
        $compiler->hooks = new CompilerHooks($compiler, $this->baseDir);
        
        $compiler->processWorkspace();
        $generated = $generator->generate($compiler->projectPkg);
        
        $langName = $generator->getLangName();
        $ext = "." . $generator->getExtension();
        
        $allMatch = true;
        foreach ($generated as $genFile) {
            $fn = preg_replace("/\\.ts$/", $ext, $genFile->path);
            $projBase = $this->baseDir . "test/artifacts/ProjectTest/OneLang";
            $tsGenPath = $projBase . "/" . $langName . "/" . $fn;
            $reGenPath = $projBase . "/" . $langName . "_Regen/" . $fn;
            $tsGenContent = OneFile::readText($tsGenPath);
            $reGenContent = $genFile->content;
            
            if ($tsGenContent !== $reGenContent) {
                OneFile::writeText($reGenPath, $genFile->content);
                \OneLang\console::error("Content does not match: " . $genFile->path);
                $allMatch = false;
            }
            else
                \OneLang\console::log("[+] Content matches: " . $genFile->path);
        }
        
        \OneLang\console::log($allMatch ? "[+} SUCCESS! All generated files are the same" : "[!] FAIL! Not all files are the same");
        \OneLang\console::log("[-] SelfTestRunner :: DONE");
        return $allMatch;
    }
}
