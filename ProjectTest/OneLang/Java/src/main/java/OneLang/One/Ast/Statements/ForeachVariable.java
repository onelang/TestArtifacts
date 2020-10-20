import java.util.List;
import java.util.ArrayList;

public class ForeachVariable implements IVariable, IReferencable {
    public List<ForeachVariableReference> references;
    
    String name;
    public String getName() { return this.name; }
    public void setName(String value) { this.name = value; }
    
    IType type = null;
    public IType getType() { return this.type; }
    public void setType(IType value) { this.type = value; }
    
    MutabilityInfo mutability = null;
    public MutabilityInfo getMutability() { return this.mutability; }
    public void setMutability(MutabilityInfo value) { this.mutability = value; }
    
    public ForeachVariable(String name)
    {
        this.setName(name);
        this.references = new ArrayList<ForeachVariableReference>();
    }
    
    public Reference createReference() {
        return new ForeachVariableReference(this);
    }
}