import java.util.Map;

public class Constructor implements IMethodBaseWithTrivia {
    public Expression[] superCallArgs;
    public Class parentClass;
    
    MethodParameter[] parameters;
    public MethodParameter[] getParameters() { return this.parameters; }
    public void setParameters(MethodParameter[] value) { this.parameters = value; }
    
    Block body;
    public Block getBody() { return this.body; }
    public void setBody(Block value) { this.body = value; }
    
    String leadingTrivia;
    public String getLeadingTrivia() { return this.leadingTrivia; }
    public void setLeadingTrivia(String value) { this.leadingTrivia = value; }
    
    Map<String, String> attributes = null;
    public Map<String, String> getAttributes() { return this.attributes; }
    public void setAttributes(Map<String, String> value) { this.attributes = value; }
    
    Boolean throws_ = false;
    public Boolean getThrows() { return this.throws_; }
    public void setThrows(Boolean value) { this.throws_ = value; }
    
    public Constructor(MethodParameter[] parameters, Block body, Expression[] superCallArgs, String leadingTrivia)
    {
        this.setParameters(parameters);
        this.setBody(body);
        this.superCallArgs = superCallArgs;
        this.setLeadingTrivia(leadingTrivia);
        this.parentClass = null;
    }
}