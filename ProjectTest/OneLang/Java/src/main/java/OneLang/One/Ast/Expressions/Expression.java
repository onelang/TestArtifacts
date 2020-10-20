public class Expression implements IAstNode, IExpression {
    public IAstNode parentNode;
    public IType expectedType;
    public IType actualType;
    
    public Expression()
    {
        this.parentNode = null;
        this.expectedType = null;
        this.actualType = null;
    }
    
    protected void typeCheck(IType type, Boolean allowVoid) {
        if (type == null)
            throw new Error("New type cannot be null!");
        
        if (type instanceof VoidType && !allowVoid)
            throw new Error("Expression's type cannot be VoidType!");
        
        if (type instanceof UnresolvedType)
            throw new Error("Expression's type cannot be UnresolvedType!");
    }
    
    public void setActualType(IType actualType, Boolean allowVoid, Boolean allowGeneric) {
        if (this.actualType != null)
            throw new Error("Expression already has actual type (current type = " + this.actualType.repr() + ", new type = " + actualType.repr() + ")");
        
        this.typeCheck(actualType, allowVoid);
        
        if (this.expectedType != null && !TypeHelper.isAssignableTo(actualType, this.expectedType))
            throw new Error("Actual type (" + actualType.repr() + ") is not assignable to the declared type (" + this.expectedType.repr() + ")!");
        
        // TODO: decide if this check needed or not
        //if (!allowGeneric && TypeHelper.isGeneric(actualType))
        //    throw new Error(`Actual type cannot be generic (${actualType.repr()})!`);
        
        this.actualType = actualType;
    }
    
    public void setActualType(IType actualType, Boolean allowVoid) {
        this.setActualType(actualType, allowVoid, false);
    }
    
    public void setActualType(IType actualType) {
        this.setActualType(actualType, false, false);
    }
    
    public void setExpectedType(IType type, Boolean allowVoid) {
        if (this.actualType != null)
            throw new Error("Cannot set expected type after actual type was already set!");
        
        if (this.expectedType != null)
            throw new Error("Expression already has a expected type!");
        
        this.typeCheck(type, allowVoid);
        
        this.expectedType = type;
    }
    
    public void setExpectedType(IType type) {
        this.setExpectedType(type, false);
    }
    
    public IType getType() {
        return this.actualType != null ? this.actualType : this.expectedType;
    }
    
    public IExpression copy() {
        throw new Error("Copy is not implemented!");
    }
}