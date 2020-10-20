public class ThisReference extends Reference {
    public Class cls;
    
    public ThisReference(Class cls)
    {
        super();
        this.cls = cls;
        cls.thisReferences.add(this);
    }
    
    public void setActualType(IType type, Boolean allowVoid, Boolean allowGeneric) {
        if (!(type instanceof ClassType))
            throw new Error("Expected ClassType!");
        super.setActualType(type, false, this.cls.getTypeArguments().length > 0);
    }
    
    public void setActualType(IType type, Boolean allowVoid) {
        this.setActualType(type, allowVoid, false);
    }
    
    public void setActualType(IType type) {
        this.setActualType(type, false, false);
    }
}