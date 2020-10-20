public class StaticFieldReference extends VariableReference {
    public Field decl;
    
    public StaticFieldReference(Field decl)
    {
        super();
        this.decl = decl;
        decl.staticReferences.add(this);
    }
    
    public void setActualType(IType type, Boolean allowVoid, Boolean allowGeneric) {
        if (TypeHelper.isGeneric(type))
            throw new Error("StaticField's type cannot be Generic");
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