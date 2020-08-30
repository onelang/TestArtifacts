using Generator;
using One;
using System.Threading.Tasks;

namespace Test
{
    public class SelfTestRunner {
        public string baseDir;
        
        public SelfTestRunner(string baseDir)
        {
            this.baseDir = baseDir;
        }
        
        public async Task runTest(IGenerator generator) {
            console.log("[-] SelfTestRunner :: START");
            var compiler = new Compiler();
            await compiler.init($"{this.baseDir}packages/");
            compiler.setupNativeResolver(OneFile.readText($"{this.baseDir}langs/NativeResolvers/typescript.ts"));
            compiler.newWorkspace();
            
            var projDir = $"{this.baseDir}src/";
            foreach (var file in OneFile.listFiles(projDir, true).filter(x => x.endsWith(".ts")))
                compiler.addProjectFile(file, OneFile.readText($"{projDir}/{file}"));
            
            compiler.processWorkspace();
            var generated = generator.generate(compiler.projectPkg);
            
            var langName = generator.getLangName();
            var ext = $".{generator.getExtension()}";
            
            var allMatch = true;
            foreach (var genFile in generated) {
                var fn = genFile.path.replace(new RegExp("\\.ts$"), ext);
                var projBase = $"{this.baseDir}test/artifacts/ProjectTest/OneLang";
                var tsGenPath = $"{projBase}/{langName}/{fn}";
                var reGenPath = $"{projBase}/{langName}_Regen_{langName}/{fn}";
                var tsGenContent = OneFile.readText(tsGenPath);
                var reGenContent = genFile.content;
                
                OneFile.writeText(reGenPath, genFile.content);
                
                if (tsGenContent != reGenContent) {
                    console.error($"Content does not match: {genFile.path}");
                    allMatch = false;
                }
            }
            
            console.log(allMatch ? "[+} SUCCESS! All generated files are the same" : "[!] FAIL! Not all files are the same");
            console.log("[-] SelfTestRunner :: DONE");
        }
    }
}