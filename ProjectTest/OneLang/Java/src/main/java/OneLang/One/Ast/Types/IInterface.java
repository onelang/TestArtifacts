public interface IInterface {
    String getName();
    void setName(String value);
    
    String[] getTypeArguments();
    void setTypeArguments(String[] value);
    
    IType[] getBaseInterfaces();
    void setBaseInterfaces(IType[] value);
    
    Field[] getFields();
    void setFields(Field[] value);
    
    Method[] getMethods();
    void setMethods(Method[] value);
    
    String getLeadingTrivia();
    void setLeadingTrivia(String value);
    
    SourceFile getParentFile();
    void setParentFile(SourceFile value);
    
    IInterface[] getAllBaseInterfaces();
}