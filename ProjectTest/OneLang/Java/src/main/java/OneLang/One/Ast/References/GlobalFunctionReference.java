public class GlobalFunctionReference extends Reference implements IGetMethodBase {
    public GlobalFunction decl;
    
    public GlobalFunctionReference(GlobalFunction decl)
    {
        super();
        this.decl = decl;
        decl.references.add(this);
    }
    
    public void setActualType(IType type, Boolean allowVoid, Boolean allowGeneric) {
        throw new Error("GlobalFunctionReference cannot have a type!");
    }
    
    public void setActualType(IType type, Boolean allowVoid) {
        this.setActualType(type, allowVoid, false);
    }
    
    public void setActualType(IType type) {
        this.setActualType(type, false, false);
    }
    
    public IMethodBase getMethodBase() {
        return this.decl;
    }
}