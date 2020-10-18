import java.util.Map;
import java.util.HashMap;

public class SourceFile {
    public Import[] imports;
    public Interface[] interfaces;
    public Class[] classes;
    public Enum[] enums;
    public GlobalFunction[] funcs;
    public Block mainBlock;
    public SourcePath sourcePath;
    public ExportScopeRef exportScope;
    public Map<String, IImportable> availableSymbols;
    public LiteralTypes literalTypes;
    public ClassType[] arrayTypes;
    
    public SourceFile(Import[] imports, Interface[] interfaces, Class[] classes, Enum[] enums, GlobalFunction[] funcs, Block mainBlock, SourcePath sourcePath, ExportScopeRef exportScope)
    {
        this.imports = imports;
        this.interfaces = interfaces;
        this.classes = classes;
        this.enums = enums;
        this.funcs = funcs;
        this.mainBlock = mainBlock;
        this.sourcePath = sourcePath;
        this.exportScope = exportScope;
        this.availableSymbols = new HashMap<String, IImportable>();
        this.arrayTypes = new ClassType[0];
        var fileScope = Package.collectExportsFromFile(this, true);
        this.addAvailableSymbols(fileScope.getAllExports());
    }
    
    public void addAvailableSymbols(IImportable[] items)
    {
        for (var item : items)
            this.availableSymbols.put(item.getName(), item);
    }
}