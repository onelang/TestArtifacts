import java.util.Map;
import java.util.List;
import java.util.ArrayList;

public class Property implements IVariable, IHasAttributesAndTrivia, IClassMember, IAstNode {
    public Block getter;
    public Block setter;
    public Class parentClass;
    public List<StaticPropertyReference> staticReferences;
    public List<InstancePropertyReference> instanceReferences;
    
    String name;
    public String getName() { return this.name; }
    public void setName(String value) { this.name = value; }
    
    IType type;
    public IType getType() { return this.type; }
    public void setType(IType value) { this.type = value; }
    
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
    
    public Property(String name, IType type, Block getter, Block setter, Visibility visibility, Boolean isStatic, String leadingTrivia)
    {
        this.setName(name);
        this.setType(type);
        this.getter = getter;
        this.setter = setter;
        this.setVisibility(visibility);
        this.setIsStatic(isStatic);
        this.setLeadingTrivia(leadingTrivia);
        this.parentClass = null;
        this.staticReferences = new ArrayList<StaticPropertyReference>();
        this.instanceReferences = new ArrayList<InstancePropertyReference>();
    }
}