import java.util.List;
import java.util.ArrayList;

public class VariableDeclaration extends Statement implements IVariableWithInitializer, IReferencable {
    public List<VariableDeclarationReference> references;
    
    String name;
    public String getName() { return this.name; }
    public void setName(String value) { this.name = value; }
    
    IType type;
    public IType getType() { return this.type; }
    public void setType(IType value) { this.type = value; }
    
    Expression initializer;
    public Expression getInitializer() { return this.initializer; }
    public void setInitializer(Expression value) { this.initializer = value; }
    
    MutabilityInfo mutability = null;
    public MutabilityInfo getMutability() { return this.mutability; }
    public void setMutability(MutabilityInfo value) { this.mutability = value; }
    
    public VariableDeclaration(String name, IType type, Expression initializer)
    {
        super();
        this.setName(name);
        this.setType(type);
        this.setInitializer(initializer);
        this.references = new ArrayList<VariableDeclarationReference>();
    }
    
    public Reference createReference() {
        return new VariableDeclarationReference(this);
    }
}