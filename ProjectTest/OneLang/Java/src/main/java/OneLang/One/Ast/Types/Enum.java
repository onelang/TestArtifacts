import java.util.Map;
import java.util.List;
import java.util.ArrayList;

public class Enum implements IHasAttributesAndTrivia, IResolvedImportable, ISourceFileMember, IReferencable {
    public EnumMember[] values;
    public List<EnumReference> references;
    public EnumType type;
    
    String name;
    public String getName() { return this.name; }
    public void setName(String value) { this.name = value; }
    
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
    
    public Enum(String name, EnumMember[] values, Boolean isExported, String leadingTrivia)
    {
        this.setName(name);
        this.values = values;
        this.setIsExported(isExported);
        this.setLeadingTrivia(leadingTrivia);
        this.references = new ArrayList<EnumReference>();
        this.type = new EnumType(this);
    }
    
    public Reference createReference()
    {
        return new EnumReference(this);
    }
}