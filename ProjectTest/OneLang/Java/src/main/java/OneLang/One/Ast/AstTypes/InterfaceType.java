public class InterfaceType implements IType, IHasTypeArguments, IInterfaceType {
    public Interface decl;
    
    IType[] typeArguments;
    public IType[] getTypeArguments() { return this.typeArguments; }
    public void setTypeArguments(IType[] value) { this.typeArguments = value; }
    
    public InterfaceType(Interface decl, IType[] typeArguments)
    {
        this.decl = decl;
        this.setTypeArguments(typeArguments);
    }
    
    public IInterface getDecl() {
        return this.decl;
    }
    
    public String repr() {
        return "I:" + this.decl.getName() + TypeHelper.argsRepr(this.getTypeArguments());
    }
}