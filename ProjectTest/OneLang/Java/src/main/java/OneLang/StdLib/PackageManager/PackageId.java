public class PackageId {
    public PackageType type;
    public String name;
    public String version;
    
    public PackageId(PackageType type, String name, String version)
    {
        this.type = type;
        this.name = name;
        this.version = version;
    }
}