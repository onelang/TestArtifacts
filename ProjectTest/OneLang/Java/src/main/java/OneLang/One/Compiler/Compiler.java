import java.util.Arrays;
import java.util.ArrayList;

public class Compiler {
    public PackageManager pacMan;
    public Workspace workspace;
    public SourceFile nativeFile;
    public ExportedScope nativeExports;
    public Package projectPkg;
    public ICompilerHooks hooks;
    
    public Compiler()
    {
        this.pacMan = null;
        this.workspace = null;
        this.nativeFile = null;
        this.nativeExports = null;
        this.projectPkg = null;
        this.hooks = null;
    }
    
    public void init(String packagesDir)
    {
        this.pacMan = new PackageManager(new PackagesFolderSource(packagesDir));
        this.pacMan.loadAllCached();
    }
    
    public void setupNativeResolver(String content)
    {
        this.nativeFile = TypeScriptParser2.parseFile(content);
        this.nativeExports = Package.collectExportsFromFile(this.nativeFile, true);
        new FillParent().visitSourceFile(this.nativeFile);
        FillAttributesFromTrivia.processFile(this.nativeFile);
        new ResolveGenericTypeIdentifiers().visitSourceFile(this.nativeFile);
        new ResolveUnresolvedTypes().visitSourceFile(this.nativeFile);
        new FillMutabilityInfo().visitSourceFile(this.nativeFile);
    }
    
    public void newWorkspace(String pkgName)
    {
        this.workspace = new Workspace();
        for (var intfPkg : this.pacMan.interfacesPkgs) {
            var libName = intfPkg.interfaceYaml.vendor + "." + intfPkg.interfaceYaml.name + "-v" + intfPkg.interfaceYaml.version;
            this.addInterfacePackage(libName, intfPkg.definition);
        }
        
        this.projectPkg = new Package(pkgName, false);
        this.workspace.addPackage(this.projectPkg);
    }
    
    public void newWorkspace() {
        this.newWorkspace("@");
    }
    
    public void addInterfacePackage(String libName, String definitionFileContent)
    {
        var libPkg = new Package(libName, true);
        var file = TypeScriptParser2.parseFile(definitionFileContent, new SourcePath(libPkg, Package.INDEX));
        this.setupFile(file);
        libPkg.addFile(file, true);
        this.workspace.addPackage(libPkg);
    }
    
    public void setupFile(SourceFile file)
    {
        file.addAvailableSymbols(this.nativeExports.getAllExports());
        file.literalTypes = new LiteralTypes((((Class)file.availableSymbols.get("TsBoolean"))).type, (((Class)file.availableSymbols.get("TsNumber"))).type, (((Class)file.availableSymbols.get("TsString"))).type, (((Class)file.availableSymbols.get("RegExp"))).type, (((Class)file.availableSymbols.get("TsArray"))).type, (((Class)file.availableSymbols.get("TsMap"))).type, (((Class)file.availableSymbols.get("Error"))).type, (((Class)file.availableSymbols.get("Promise"))).type);
        file.arrayTypes = new ClassType[] { (((Class)file.availableSymbols.get("TsArray"))).type, (((Class)file.availableSymbols.get("IterableIterator"))).type, (((Class)file.availableSymbols.get("RegExpExecArray"))).type, (((Class)file.availableSymbols.get("TsString"))).type, (((Class)file.availableSymbols.get("Set"))).type };
    }
    
    public void addProjectFile(String fn, String content)
    {
        var file = TypeScriptParser2.parseFile(content, new SourcePath(this.projectPkg, fn));
        this.setupFile(file);
        this.projectPkg.addFile(file);
    }
    
    public void processWorkspace()
    {
        for (var pkg : Arrays.stream(this.workspace.packages.values().toArray(Package[]::new)).filter(x -> x.definitionOnly).toArray(Package[]::new)) {
            // sets method's parentInterface property
            new FillParent().visitPackage(pkg);
            FillAttributesFromTrivia.processPackage(pkg);
            new ResolveGenericTypeIdentifiers().visitPackage(pkg);
            new ResolveUnresolvedTypes().visitPackage(pkg);
        }
        
        new FillParent().visitPackage(this.projectPkg);
        if (this.hooks != null)
            this.hooks.afterStage("FillParent");
        
        FillAttributesFromTrivia.processPackage(this.projectPkg);
        if (this.hooks != null)
            this.hooks.afterStage("FillAttributesFromTrivia");
        
        ResolveImports.processWorkspace(this.workspace);
        if (this.hooks != null)
            this.hooks.afterStage("ResolveImports");
        
        var transforms = new ArrayList<ITransformer>();
        transforms.add(new ResolveGenericTypeIdentifiers());
        transforms.add(new ConvertToMethodCall());
        transforms.add(new ResolveUnresolvedTypes());
        transforms.add(new ResolveIdentifiers());
        transforms.add(new InstanceOfImplicitCast());
        transforms.add(new DetectMethodCalls());
        transforms.add(new InferTypes());
        transforms.add(new CollectInheritanceInfo());
        transforms.add(new FillMutabilityInfo());
        transforms.add(new LambdaCaptureCollector());
        for (var trans : transforms) {
            trans.visitPackage(this.projectPkg);
            if (this.hooks != null)
                this.hooks.afterStage(trans.getName());
        }
    }
}