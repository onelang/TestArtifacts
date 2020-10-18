import java.util.Arrays;
import java.util.regex.Pattern;

public class SelfTestRunner {
    public String baseDir;
    
    public SelfTestRunner(String baseDir)
    {
        this.baseDir = baseDir;
    }
    
    public Boolean runTest(IGenerator generator)
    {
        console.log("[-] SelfTestRunner :: START");
        var compiler = new Compiler();
        compiler.init(this.baseDir + "packages/");
        compiler.setupNativeResolver(OneFile.readText(this.baseDir + "langs/NativeResolvers/typescript.ts"));
        compiler.newWorkspace("OneLang");
        
        var projDir = this.baseDir + "src/";
        for (var file : Arrays.stream(OneFile.listFiles(projDir, true)).filter(x -> x.endsWith(".ts")).toArray(String[]::new))
            compiler.addProjectFile(file, OneFile.readText(projDir + "/" + file));
        
        compiler.hooks = new CompilerHooks(compiler, this.baseDir);
        
        compiler.processWorkspace();
        var generated = generator.generate(compiler.projectPkg);
        
        var langName = generator.getLangName();
        var ext = "." + generator.getExtension();
        
        var allMatch = true;
        for (var genFile : generated) {
            var fn = genFile.path.replaceAll(Pattern.quote("\\.ts$"), ext);
            var projBase = this.baseDir + "test/artifacts/ProjectTest/OneLang";
            var tsGenPath = projBase + "/" + langName + "/" + fn;
            var reGenPath = projBase + "/" + langName + "_Regen/" + fn;
            var tsGenContent = OneFile.readText(tsGenPath);
            var reGenContent = genFile.content;
            
            if (tsGenContent != reGenContent) {
                OneFile.writeText(reGenPath, genFile.content);
                console.error("Content does not match: " + genFile.path);
                allMatch = false;
            }
            else
                console.log("[+] Content matches: " + genFile.path);
        }
        
        console.log(allMatch ? "[+} SUCCESS! All generated files are the same" : "[!] FAIL! Not all files are the same");
        console.log("[-] SelfTestRunner :: DONE");
        return allMatch;
    }
}