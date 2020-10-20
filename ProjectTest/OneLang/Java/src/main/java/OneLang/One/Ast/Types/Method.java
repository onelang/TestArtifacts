import java.util.Map;
import java.util.List;
import java.util.ArrayList;

public class Method implements IMethodBaseWithTrivia, IClassMember {
    public String name;
    public String[] typeArguments;
    public IType returns;
    public Boolean async;
    public IInterface parentInterface;
    public Method[] interfaceDeclarations;
    public Method overrides;
    public List<Method> overriddenBy;
    
    MethodParameter[] parameters;
    public MethodParameter[] getParameters() { return this.parameters; }
    public void setParameters(MethodParameter[] value) { this.parameters = value; }
    
    Block body;
    public Block getBody() { return this.body; }
    public void setBody(Block value) { this.body = value; }
    
    Visibility visibility;
    public Visibility getVisibility() { return this.visibility; }
    public void setVisibility(Visibility value) { this.visibility = value; }
    
    Boolean isStatic = false;
    public Boolean getIsStatic() { return this.isStatic; }
    public void setIsStatic(Boolean value) { this.isStatic = value; }
    
    String leadingTrivia;
    public String getLeadingTrivia() { return this.leadingTrivia; }
    public void setLeadingTrivia(String value) { this.leadingTrivia = value; }
    
    Map<String, String> attributes = null;
    public Map<String, String> getAttributes() { return this.attributes; }
    public void setAttributes(Map<String, String> value) { this.attributes = value; }
    
    Boolean throws_ = false;
    public Boolean getThrows() { return this.throws_; }
    public void setThrows(Boolean value) { this.throws_ = value; }
    
    public Method(String name, String[] typeArguments, MethodParameter[] parameters, Block body, Visibility visibility, Boolean isStatic, IType returns, Boolean async, String leadingTrivia)
    {
        this.name = name;
        this.typeArguments = typeArguments;
        this.setParameters(parameters);
        this.setBody(body);
        this.setVisibility(visibility);
        this.setIsStatic(isStatic);
        this.returns = returns;
        this.async = async;
        this.setLeadingTrivia(leadingTrivia);
        this.parentInterface = null;
        this.interfaceDeclarations = null;
        this.overrides = null;
        this.overriddenBy = new ArrayList<Method>();
    }
}