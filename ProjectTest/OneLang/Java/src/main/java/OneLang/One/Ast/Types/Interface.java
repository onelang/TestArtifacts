import java.util.Map;
import java.util.Arrays;

public class Interface implements IHasAttributesAndTrivia, IInterface, IResolvedImportable, ISourceFileMember {
    public InterfaceType type;
    public IInterface[] _baseInterfaceCache;
    
    String name;
    public String getName() { return this.name; }
    public void setName(String value) { this.name = value; }
    
    String[] typeArguments;
    public String[] getTypeArguments() { return this.typeArguments; }
    public void setTypeArguments(String[] value) { this.typeArguments = value; }
    
    IType[] baseInterfaces;
    public IType[] getBaseInterfaces() { return this.baseInterfaces; }
    public void setBaseInterfaces(IType[] value) { this.baseInterfaces = value; }
    
    Field[] fields;
    public Field[] getFields() { return this.fields; }
    public void setFields(Field[] value) { this.fields = value; }
    
    Method[] methods;
    public Method[] getMethods() { return this.methods; }
    public void setMethods(Method[] value) { this.methods = value; }
    
    Boolean isExported;
    public Boolean getIsExported() { return this.isExported; }
    public void setIsExported(Boolean value) { this.isExported = value; }
    
    String leadingTrivia;
    public String getLeadingTrivia() { return this.leadingTrivia; }
    public void setLeadingTrivia(String value) { this.leadingTrivia = value; }
    
    SourceFile parentFile;
    public SourceFile getParentFile() { return this.parentFile; }
    public void setParentFile(SourceFile value) { this.parentFile = value; }
    
    Map<String, String> attributes;
    public Map<String, String> getAttributes() { return this.attributes; }
    public void setAttributes(Map<String, String> value) { this.attributes = value; }
    
    public Interface(String name, String[] typeArguments, IType[] baseInterfaces, Field[] fields, Method[] methods, Boolean isExported, String leadingTrivia)
    {
        this.setName(name);
        this.setTypeArguments(typeArguments);
        this.setBaseInterfaces(baseInterfaces);
        this.setFields(fields);
        this.setMethods(methods);
        this.setIsExported(isExported);
        this.setLeadingTrivia(leadingTrivia);
        this.type = new InterfaceType(this, Arrays.stream(this.getTypeArguments()).map(x -> new GenericsType(x)).toArray(GenericsType[]::new));
        this._baseInterfaceCache = null;
    }
    
    public IInterface[] getAllBaseInterfaces()
    {
        if (this._baseInterfaceCache == null)
            this._baseInterfaceCache = AstHelper.collectAllBaseInterfaces(this);
        return this._baseInterfaceCache;
    }
}