import java.util.Map;

public class Import implements IHasAttributesAndTrivia, ISourceFileMember {
    public ExportScopeRef exportScope;
    public Boolean importAll;
    public IImportable[] imports;
    public String importAs;
    
    String leadingTrivia;
    public String getLeadingTrivia() { return this.leadingTrivia; }
    public void setLeadingTrivia(String value) { this.leadingTrivia = value; }
    
    SourceFile parentFile;
    public SourceFile getParentFile() { return this.parentFile; }
    public void setParentFile(SourceFile value) { this.parentFile = value; }
    
    Map<String, String> attributes;
    public Map<String, String> getAttributes() { return this.attributes; }
    public void setAttributes(Map<String, String> value) { this.attributes = value; }
    
    public Import(ExportScopeRef exportScope, Boolean importAll, IImportable[] imports, String importAs, String leadingTrivia)
    {
        this.exportScope = exportScope;
        this.importAll = importAll;
        this.imports = imports;
        this.importAs = importAs;
        this.setLeadingTrivia(leadingTrivia);
        if (importAs != null && !importAll)
            throw new Error("importAs only supported with importAll!");
    }
}