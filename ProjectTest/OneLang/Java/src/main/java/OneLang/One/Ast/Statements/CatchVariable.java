import java.util.List;
import java.util.ArrayList;

public class CatchVariable implements IVariable, IReferencable {
    public List<CatchVariableReference> references;
    
    String name;
    public String getName() { return this.name; }
    public void setName(String value) { this.name = value; }
    
    IType type;
    public IType getType() { return this.type; }
    public void setType(IType value) { this.type = value; }
    
    MutabilityInfo mutability = null;
    public MutabilityInfo getMutability() { return this.mutability; }
    public void setMutability(MutabilityInfo value) { this.mutability = value; }
    
    public CatchVariable(String name, IType type)
    {
        this.setName(name);
        this.setType(type);
        this.references = new ArrayList<CatchVariableReference>();
    }
    
    public Reference createReference() {
        return new CatchVariableReference(this);
    }
}