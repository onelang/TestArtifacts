import java.util.List;
import java.util.ArrayList;

public class ForVariable implements IVariableWithInitializer, IReferencable {
    public List<ForVariableReference> references;
    
    String name;
    public String getName() { return this.name; }
    public void setName(String value) { this.name = value; }
    
    IType type;
    public IType getType() { return this.type; }
    public void setType(IType value) { this.type = value; }
    
    Expression initializer;
    public Expression getInitializer() { return this.initializer; }
    public void setInitializer(Expression value) { this.initializer = value; }
    
    MutabilityInfo mutability;
    public MutabilityInfo getMutability() { return this.mutability; }
    public void setMutability(MutabilityInfo value) { this.mutability = value; }
    
    public ForVariable(String name, IType type, Expression initializer)
    {
        this.setName(name);
        this.setType(type);
        this.setInitializer(initializer);
        this.references = new ArrayList<ForVariableReference>();
    }
    
    public Reference createReference()
    {
        return new ForVariableReference(this);
    }
}