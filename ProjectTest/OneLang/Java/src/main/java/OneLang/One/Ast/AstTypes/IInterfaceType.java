public interface IInterfaceType extends IType {
    IType[] getTypeArguments();
    void setTypeArguments(IType[] value);
    
    IInterface getDecl();
}