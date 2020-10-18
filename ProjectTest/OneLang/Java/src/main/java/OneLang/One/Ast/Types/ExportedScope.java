import java.util.Map;
import java.util.HashMap;

public class ExportedScope {
    public Map<String, IImportable> exports;
    
    public ExportedScope()
    {
        this.exports = new HashMap<String, IImportable>();
    }
    
    public IImportable getExport(String name)
    {
        var exp = this.exports.get(name);
        if (exp == null)
            throw new Error("Export " + name + " was not found in exported symbols.");
        return exp;
    }
    
    public void addExport(String name, IImportable value)
    {
        this.exports.put(name, value);
    }
    
    public IImportable[] getAllExports()
    {
        return this.exports.values().toArray(IImportable[]::new);
    }
}