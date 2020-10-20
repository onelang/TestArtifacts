public class StaticPropertyReference extends VariableReference {
    public Property decl;
    
    public StaticPropertyReference(Property decl)
    {
        super();
        this.decl = decl;
        decl.staticReferences.add(this);
    }
    
    public void setActualType(IType type, Boolean allowVoid, Boolean allowGeneric) {
        if (TypeHelper.isGeneric(type))
            throw new Error("StaticProperty's type cannot be Generic");
        super.setActualType(type);
    }
    
    public void setActualType(IType type, Boolean allowVoid) {
        this.setActualType(type, allowVoid, false);
    }
    
    public void setActualType(IType type) {
        this.setActualType(type, false, false);
    }
    
    public IVariable getVariable() {
        return this.decl;
    }
}