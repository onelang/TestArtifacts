public class CompilationError {
    public String msg;
    public Boolean isWarning;
    public String transformerName;
    public IAstNode node;
    
    public CompilationError(String msg, Boolean isWarning, String transformerName, IAstNode node)
    {
        this.msg = msg;
        this.isWarning = isWarning;
        this.transformerName = transformerName;
        this.node = node;
    }
}