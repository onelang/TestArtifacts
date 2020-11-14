import java.util.Arrays;

public class CompilerHelper {
    public static String baseDir = "./";
    
    public static Compiler initProject(String projectName, String sourceDir, String lang, String packagesDir) {
        if (!lang.equals("ts"))
            throw new Error("Only typescript is supported.");
        
        var compiler = new Compiler();
        compiler.init(packagesDir != null ? packagesDir : CompilerHelper.baseDir + "packages/");
        compiler.setupNativeResolver(OneFile.readText(CompilerHelper.baseDir + "langs/NativeResolvers/typescript.ts"));
        compiler.newWorkspace(projectName);
        
        for (var file : Arrays.stream(OneFile.listFiles(sourceDir, true)).filter(x -> x.endsWith(".ts")).toArray(String[]::new))
            compiler.addProjectFile(file, OneFile.readText(sourceDir + "/" + file));
        
        return compiler;
    }
    
    public static Compiler initProject(String projectName, String sourceDir, String lang) {
        return CompilerHelper.initProject(projectName, sourceDir, lang, null);
    }
    
    public static Compiler initProject(String projectName, String sourceDir) {
        return CompilerHelper.initProject(projectName, sourceDir, "ts", null);
    }
}