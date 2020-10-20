public class EnumReference extends Reference {
    public Enum decl;
    
    public EnumReference(Enum decl)
    {
        super();
        this.decl = decl;
        decl.references.add(this);
    }
    
    public void setActualType(IType type, Boolean allowVoid, Boolean allowGeneric) {
        throw new Error("EnumReference cannot have a type!");
    }
    
    public void setActualType(IType type, Boolean allowVoid) {
        this.setActualType(type, allowVoid, false);
    }
    
    public void setActualType(IType type) {
        this.setActualType(type, false, false);
    }
}