import java.util.Map;
import java.util.List;
import java.util.ArrayList;
import java.util.Arrays;

public class Class implements IHasAttributesAndTrivia, IInterface, IResolvedImportable, ISourceFileMember, IReferencable {
    public IType baseClass;
    public Property[] properties;
    public Constructor constructor_;
    public List<ClassReference> classReferences;
    public List<ThisReference> thisReferences;
    public List<StaticThisReference> staticThisReferences;
    public List<SuperReference> superReferences;
    public ClassType type;
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
    
    Boolean isExported = false;
    public Boolean getIsExported() { return this.isExported; }
    public void setIsExported(Boolean value) { this.isExported = value; }
    
    String leadingTrivia;
    public String getLeadingTrivia() { return this.leadingTrivia; }
    public void setLeadingTrivia(String value) { this.leadingTrivia = value; }
    
    SourceFile parentFile = null;
    public SourceFile getParentFile() { return this.parentFile; }
    public void setParentFile(SourceFile value) { this.parentFile = value; }
    
    Map<String, String> attributes = null;
    public Map<String, String> getAttributes() { return this.attributes; }
    public void setAttributes(Map<String, String> value) { this.attributes = value; }
    
    public Class(String name, String[] typeArguments, IType baseClass, IType[] baseInterfaces, Field[] fields, Property[] properties, Constructor constructor_, Method[] methods, Boolean isExported, String leadingTrivia)
    {
        this.setName(name);
        this.setTypeArguments(typeArguments);
        this.baseClass = baseClass;
        this.setBaseInterfaces(baseInterfaces);
        this.setFields(fields);
        this.properties = properties;
        this.constructor_ = constructor_;
        this.setMethods(methods);
        this.setIsExported(isExported);
        this.setLeadingTrivia(leadingTrivia);
        this.classReferences = new ArrayList<ClassReference>();
        this.thisReferences = new ArrayList<ThisReference>();
        this.staticThisReferences = new ArrayList<StaticThisReference>();
        this.superReferences = new ArrayList<SuperReference>();
        this.type = new ClassType(this, Arrays.stream(this.getTypeArguments()).map(x -> new GenericsType(x)).toArray(GenericsType[]::new));
        this._baseInterfaceCache = null;
    }
    
    public Reference createReference() {
        return new ClassReference(this);
    }
    
    public IInterface[] getAllBaseInterfaces() {
        if (this._baseInterfaceCache == null)
            this._baseInterfaceCache = AstHelper.collectAllBaseInterfaces(this);
        return this._baseInterfaceCache;
    }
}