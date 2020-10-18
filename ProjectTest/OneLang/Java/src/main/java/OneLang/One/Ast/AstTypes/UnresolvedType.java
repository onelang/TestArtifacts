public class UnresolvedType implements IType, IHasTypeArguments {
    public String typeName;
    
    IType[] typeArguments;
    public IType[] getTypeArguments() { return this.typeArguments; }
    public void setTypeArguments(IType[] value) { this.typeArguments = value; }
    
    public UnresolvedType(String typeName, IType[] typeArguments)
    {
        this.typeName = typeName;
        this.setTypeArguments(typeArguments);
    }
    
    public String repr()
    {
        return "X:" + this.typeName + TypeHelper.argsRepr(this.getTypeArguments());
    }
}