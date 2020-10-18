public class ClassType implements IType, IHasTypeArguments, IInterfaceType {
    public Class decl;
    
    IType[] typeArguments;
    public IType[] getTypeArguments() { return this.typeArguments; }
    public void setTypeArguments(IType[] value) { this.typeArguments = value; }
    
    public ClassType(Class decl, IType[] typeArguments)
    {
        this.decl = decl;
        this.setTypeArguments(typeArguments);
    }
    
    public IInterface getDecl()
    {
        return this.decl;
    }
    
    public String repr()
    {
        return "C:" + this.decl.getName() + TypeHelper.argsRepr(this.getTypeArguments());
    }
}