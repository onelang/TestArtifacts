import java.util.Map;
import java.util.HashMap;
import java.util.Arrays;

public class Package {
    public String name;
    public Boolean definitionOnly;
    public static String INDEX = "index";
    public Map<String, SourceFile> files;
    public Map<String, ExportedScope> exportedScopes;
    
    public Package(String name, Boolean definitionOnly)
    {
        this.name = name;
        this.definitionOnly = definitionOnly;
        this.files = new HashMap<String, SourceFile>();
        this.exportedScopes = new HashMap<String, ExportedScope>();
    }
    
    public static ExportedScope collectExportsFromFile(SourceFile file, Boolean exportAll, ExportedScope scope)
    {
        if (scope == null)
            scope = new ExportedScope();
        
        for (var cls : Arrays.stream(file.classes).filter(x -> x.getIsExported() || exportAll).toArray(Class[]::new))
            scope.addExport(cls.getName(), cls);
        
        for (var intf : Arrays.stream(file.interfaces).filter(x -> x.getIsExported() || exportAll).toArray(Interface[]::new))
            scope.addExport(intf.getName(), intf);
        
        for (var enum_ : Arrays.stream(file.enums).filter(x -> x.getIsExported() || exportAll).toArray(Enum[]::new))
            scope.addExport(enum_.getName(), enum_);
        
        for (var func : Arrays.stream(file.funcs).filter(x -> x.getIsExported() || exportAll).toArray(GlobalFunction[]::new))
            scope.addExport(func.getName(), func);
        
        return scope;
    }
    
    public static ExportedScope collectExportsFromFile(SourceFile file, Boolean exportAll) {
        return Package.collectExportsFromFile(file, exportAll, null);
    }
    
    public void addFile(SourceFile file, Boolean exportAll)
    {
        if (file.sourcePath.pkg != this || file.exportScope.packageName != this.name)
            throw new Error("This file belongs to another package!");
        
        this.files.put(file.sourcePath.path, file);
        var scopeName = file.exportScope.scopeName;
        this.exportedScopes.put(scopeName, Package.collectExportsFromFile(file, exportAll, this.exportedScopes.get(scopeName)));
    }
    
    public void addFile(SourceFile file) {
        this.addFile(file, false);
    }
    
    public ExportedScope getExportedScope(String name)
    {
        var scope = this.exportedScopes.get(name);
        if (scope == null)
            throw new Error("Scope \"" + name + "\" was not found in package \"" + this.name + "\"");
        return scope;
    }
}