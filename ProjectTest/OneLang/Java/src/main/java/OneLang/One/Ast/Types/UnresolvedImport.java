public class UnresolvedImport implements IImportable {
    String name;
    public String getName() { return this.name; }
    public void setName(String value) { this.name = value; }
    
    Boolean isExported;
    public Boolean getIsExported() { return this.isExported; }
    public void setIsExported(Boolean value) { this.isExported = value; }
    
    public UnresolvedImport(String name)
    {
        this.setName(name);
    }
}