using Generator;
using One;
using Test;
using System.Threading.Tasks;

namespace Test
{
    public class CompilerHooks : ICompilerHooks {
        public int stage = 0;
        public Compiler compiler;
        public string baseDir;
        
        public CompilerHooks(Compiler compiler, string baseDir)
        {
            this.compiler = compiler;
            this.baseDir = baseDir;
        }
        
        public void afterStage(string stageName)
        {
            var state = new PackageStateCapture(this.compiler.projectPkg);
            var stageFn = $"{this.baseDir}/test/artifacts/ProjectTest/OneLang/stages/{this.stage}_{stageName}.txt";
            this.stage++;
            var stageSummary = state.getSummary();
            var expected = OneFile.readText(stageFn);
            if (stageSummary != expected) {
                OneFile.writeText(stageFn + "_diff.txt", stageSummary);
                throw new Error($"Stage result differs from expected: {stageName} -> {stageFn}");
            }
            else
                console.log($"[+] Stage passed: {stageName}");
        }
    }
    
    public class SelfTestRunner {
        public string baseDir;
        
        public SelfTestRunner(string baseDir)
        {
            this.baseDir = baseDir;
        }
        
        public async Task<bool> runTest(IGenerator generator)
        {
            console.log("[-] SelfTestRunner :: START");
            var compiler = new Compiler();
            await compiler.init($"{this.baseDir}packages/");
            compiler.setupNativeResolver(OneFile.readText($"{this.baseDir}langs/NativeResolvers/typescript.ts"));
            compiler.newWorkspace("OneLang");
            
            var projDir = $"{this.baseDir}src/";
            foreach (var file in OneFile.listFiles(projDir, true).filter(x => x.endsWith(".ts")))
                compiler.addProjectFile(file, OneFile.readText($"{projDir}/{file}"));
            
            compiler.hooks = new CompilerHooks(compiler, this.baseDir);
            
            compiler.processWorkspace();
            var generated = generator.generate(compiler.projectPkg);
            
            var langName = generator.getLangName();
            var ext = $".{generator.getExtension()}";
            
            var allMatch = true;
            foreach (var genFile in generated) {
                var fn = genFile.path.replace(new RegExp("\\.ts$"), ext);
                var projBase = $"{this.baseDir}test/artifacts/ProjectTest/OneLang";
                var tsGenPath = $"{projBase}/{langName}/{fn}";
                var reGenPath = $"{projBase}/{langName}_Regen/{fn}";
                var tsGenContent = OneFile.readText(tsGenPath);
                var reGenContent = genFile.content;
                
                if (tsGenContent != reGenContent) {
                    OneFile.writeText(reGenPath, genFile.content);
                    console.error($"Content does not match: {genFile.path}");
                    allMatch = false;
                }
                else
                    console.log($"[+] Content matches: {genFile.path}");
            }
            
            console.log(allMatch ? "[+} SUCCESS! All generated files are the same" : "[!] FAIL! Not all files are the same");
            console.log("[-] SelfTestRunner :: DONE");
            return allMatch;
        }
    }
}