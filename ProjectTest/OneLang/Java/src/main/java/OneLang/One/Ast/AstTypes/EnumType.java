public class EnumType implements IType {
    public Enum decl;
    
    public EnumType(Enum decl)
    {
        this.decl = decl;
    }
    
    public String repr() {
        return "E:" + this.decl.getName();
    }
}