public class SourcePath {
    public Package pkg;
    public String path;
    
    public SourcePath(Package pkg, String path)
    {
        this.pkg = pkg;
        this.path = path;
    }
    
    public String toString()
    {
        return this.pkg.name + "/" + this.path;
    }
}