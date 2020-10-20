public class CompilerHooks implements ICompilerHooks {
    public Integer stage = 0;
    public Compiler compiler;
    public String baseDir;
    
    public CompilerHooks(Compiler compiler, String baseDir)
    {
        this.compiler = compiler;
        this.baseDir = baseDir;
    }
    
    public void afterStage(String stageName) {
        var state = new PackageStateCapture(this.compiler.projectPkg);
        var stageFn = this.baseDir + "/test/artifacts/ProjectTest/OneLang/stages/" + this.stage + "_" + stageName + ".txt";
        this.stage++;
        var stageSummary = state.getSummary();
        var expected = OneFile.readText(stageFn);
        if (!stageSummary.equals(expected)) {
            OneFile.writeText(stageFn + "_diff.txt", stageSummary);
            throw new Error("Stage result differs from expected: " + stageName + " -> " + stageFn);
        }
        else
            console.log("[+] Stage passed: " + stageName);
    }
}