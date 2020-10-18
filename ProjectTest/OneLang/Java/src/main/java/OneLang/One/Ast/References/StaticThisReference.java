public class StaticThisReference extends Reference {
    public Class cls;
    
    public StaticThisReference(Class cls)
    {
        super();
        this.cls = cls;
        cls.staticThisReferences.add(this);
    }
    
    public void setActualType(IType type, Boolean allowVoid, Boolean allowGeneric)
    {
        throw new Error("StaticThisReference cannot have a type!");
    }
    
    public void setActualType(IType type, Boolean allowVoid) {
        this.setActualType(type, allowVoid, false);
    }
    
    public void setActualType(IType type) {
        this.setActualType(type, false, false);
    }
}