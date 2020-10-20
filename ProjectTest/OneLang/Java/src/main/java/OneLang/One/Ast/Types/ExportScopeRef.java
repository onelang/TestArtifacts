public class ExportScopeRef {
    public String packageName;
    public String scopeName;
    
    public ExportScopeRef(String packageName, String scopeName)
    {
        this.packageName = packageName;
        this.scopeName = scopeName;
    }
    
    public String getId() {
        return this.packageName + "." + this.scopeName;
    }
}