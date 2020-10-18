import java.util.Map;
import java.util.List;
import java.util.ArrayList;

public class MethodParameter implements IVariableWithInitializer, IReferencable, IHasAttributesAndTrivia {
    public IMethodBase parentMethod;
    public List<MethodParameterReference> references;
    
    String name;
    public String getName() { return this.name; }
    public void setName(String value) { this.name = value; }
    
    IType type;
    public IType getType() { return this.type; }
    public void setType(IType value) { this.type = value; }
    
    Expression initializer;
    public Expression getInitializer() { return this.initializer; }
    public void setInitializer(Expression value) { this.initializer = value; }
    
    String leadingTrivia;
    public String getLeadingTrivia() { return this.leadingTrivia; }
    public void setLeadingTrivia(String value) { this.leadingTrivia = value; }
    
    Map<String, String> attributes;
    public Map<String, String> getAttributes() { return this.attributes; }
    public void setAttributes(Map<String, String> value) { this.attributes = value; }
    
    MutabilityInfo mutability;
    public MutabilityInfo getMutability() { return this.mutability; }
    public void setMutability(MutabilityInfo value) { this.mutability = value; }
    
    public MethodParameter(String name, IType type, Expression initializer, String leadingTrivia)
    {
        this.setName(name);
        this.setType(type);
        this.setInitializer(initializer);
        this.setLeadingTrivia(leadingTrivia);
        this.parentMethod = null;
        this.references = new ArrayList<MethodParameterReference>();
    }
    
    public Reference createReference()
    {
        return new MethodParameterReference(this);
    }
}