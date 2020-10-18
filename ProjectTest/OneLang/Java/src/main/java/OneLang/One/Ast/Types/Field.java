import java.util.Map;
import java.util.List;
import java.util.ArrayList;

public class Field implements IVariableWithInitializer, IHasAttributesAndTrivia, IClassMember, IAstNode {
    public MethodParameter constructorParam;
    public IInterface parentInterface;
    public List<StaticFieldReference> staticReferences;
    public List<InstanceFieldReference> instanceReferences;
    public Field[] interfaceDeclarations;
    
    String name;
    public String getName() { return this.name; }
    public void setName(String value) { this.name = value; }
    
    IType type;
    public IType getType() { return this.type; }
    public void setType(IType value) { this.type = value; }
    
    Expression initializer;
    public Expression getInitializer() { return this.initializer; }
    public void setInitializer(Expression value) { this.initializer = value; }
    
    Visibility visibility;
    public Visibility getVisibility() { return this.visibility; }
    public void setVisibility(Visibility value) { this.visibility = value; }
    
    Boolean isStatic;
    public Boolean getIsStatic() { return this.isStatic; }
    public void setIsStatic(Boolean value) { this.isStatic = value; }
    
    String leadingTrivia;
    public String getLeadingTrivia() { return this.leadingTrivia; }
    public void setLeadingTrivia(String value) { this.leadingTrivia = value; }
    
    Map<String, String> attributes;
    public Map<String, String> getAttributes() { return this.attributes; }
    public void setAttributes(Map<String, String> value) { this.attributes = value; }
    
    MutabilityInfo mutability;
    public MutabilityInfo getMutability() { return this.mutability; }
    public void setMutability(MutabilityInfo value) { this.mutability = value; }
    
    public Field(String name, IType type, Expression initializer, Visibility visibility, Boolean isStatic, MethodParameter constructorParam, String leadingTrivia)
    {
        this.setName(name);
        this.setType(type);
        this.setInitializer(initializer);
        this.setVisibility(visibility);
        this.setIsStatic(isStatic);
        this.constructorParam = constructorParam;
        this.setLeadingTrivia(leadingTrivia);
        this.parentInterface = null;
        this.staticReferences = new ArrayList<StaticFieldReference>();
        this.instanceReferences = new ArrayList<InstanceFieldReference>();
        this.interfaceDeclarations = null;
    }
}