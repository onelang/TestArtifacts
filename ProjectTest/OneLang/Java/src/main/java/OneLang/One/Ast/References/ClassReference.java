public class ClassReference extends Reference {
    public Class decl;
    
    public ClassReference(Class decl)
    {
        super();
        this.decl = decl;
        decl.classReferences.add(this);
    }
    
    public void setActualType(IType type, Boolean allowVoid, Boolean allowGeneric) {
        throw new Error("ClassReference cannot have a type!");
    }
    
    public void setActualType(IType type, Boolean allowVoid) {
        this.setActualType(type, allowVoid, false);
    }
    
    public void setActualType(IType type) {
        this.setActualType(type, false, false);
    }
}