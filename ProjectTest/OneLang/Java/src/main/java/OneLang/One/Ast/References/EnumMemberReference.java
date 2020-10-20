public class EnumMemberReference extends Reference {
    public EnumMember decl;
    
    public EnumMemberReference(EnumMember decl)
    {
        super();
        this.decl = decl;
        decl.references.add(this);
    }
    
    public void setActualType(IType type, Boolean allowVoid, Boolean allowGeneric) {
        if (!(type instanceof EnumType))
            throw new Error("Expected EnumType!");
        super.setActualType(type);
    }
    
    public void setActualType(IType type, Boolean allowVoid) {
        this.setActualType(type, allowVoid, false);
    }
    
    public void setActualType(IType type) {
        this.setActualType(type, false, false);
    }
}