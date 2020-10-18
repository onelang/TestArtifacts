import java.util.Map;
import java.util.List;
import java.util.ArrayList;

public class GlobalFunction implements IMethodBaseWithTrivia, IResolvedImportable, IReferencable {
    public IType returns;
    public List<GlobalFunctionReference> references;
    
    String name;
    public String getName() { return this.name; }
    public void setName(String value) { this.name = value; }
    
    MethodParameter[] parameters;
    public MethodParameter[] getParameters() { return this.parameters; }
    public void setParameters(MethodParameter[] value) { this.parameters = value; }
    
    Block body;
    public Block getBody() { return this.body; }
    public void setBody(Block value) { this.body = value; }
    
    Boolean isExported;
    public Boolean getIsExported() { return this.isExported; }
    public void setIsExported(Boolean value) { this.isExported = value; }
    
    String leadingTrivia;
    public String getLeadingTrivia() { return this.leadingTrivia; }
    public void setLeadingTrivia(String value) { this.leadingTrivia = value; }
    
    SourceFile parentFile;
    public SourceFile getParentFile() { return this.parentFile; }
    public void setParentFile(SourceFile value) { this.parentFile = value; }
    
    Map<String, String> attributes;
    public Map<String, String> getAttributes() { return this.attributes; }
    public void setAttributes(Map<String, String> value) { this.attributes = value; }
    
    Boolean throws_;
    public Boolean getThrows() { return this.throws_; }
    public void setThrows(Boolean value) { this.throws_ = value; }
    
    public GlobalFunction(String name, MethodParameter[] parameters, Block body, IType returns, Boolean isExported, String leadingTrivia)
    {
        this.setName(name);
        this.setParameters(parameters);
        this.setBody(body);
        this.returns = returns;
        this.setIsExported(isExported);
        this.setLeadingTrivia(leadingTrivia);
        this.references = new ArrayList<GlobalFunctionReference>();
    }
    
    public Reference createReference()
    {
        return new GlobalFunctionReference(this);
    }
}