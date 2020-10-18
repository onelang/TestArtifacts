import java.util.Map;
import java.util.HashMap;

public class Workspace {
    public Map<String, Package> packages;
    
    public Workspace()
    {
        this.packages = new HashMap<String, Package>();
    }
    
    public void addPackage(Package pkg)
    {
        this.packages.put(pkg.name, pkg);
    }
    
    public Package getPackage(String name)
    {
        var pkg = this.packages.get(name);
        if (pkg == null)
            throw new Error("Package was not found: \"" + name + "\"");
        return pkg;
    }
}