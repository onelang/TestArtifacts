using One.Ast;
using Parsers;
using StdLib;
using One.Transforms;
using One;
using System.Threading.Tasks;
using System.Collections.Generic;

namespace One
{
    public interface CompilerHooks {
        void afterStage(string stageName);
    }
    
    public class Compiler {
        public PackageManager pacMan;
        public Workspace workspace;
        public SourceFile nativeFile;
        public ExportedScope nativeExports;
        public Package projectPkg;
        public CompilerHooks hooks;
        
        public Compiler()
        {
            this.pacMan = null;
            this.workspace = null;
            this.nativeFile = null;
            this.nativeExports = null;
            this.projectPkg = null;
            this.hooks = null;
        }
        
        public async Task init(string packagesDir) {
            this.pacMan = new PackageManager(new PackagesFolderSource(packagesDir));
            await this.pacMan.loadAllCached();
        }
        
        public void setupNativeResolver(string content) {
            this.nativeFile = TypeScriptParser2.parseFile(content);
            this.nativeExports = Package.collectExportsFromFile(this.nativeFile, true);
            new FillParent().visitSourceFile(this.nativeFile);
            FillAttributesFromTrivia.processFile(this.nativeFile);
            new ResolveGenericTypeIdentifiers().visitSourceFile(this.nativeFile);
            new ResolveUnresolvedTypes().visitSourceFile(this.nativeFile);
            new FillMutabilityInfo().visitSourceFile(this.nativeFile);
        }
        
        public void newWorkspace(string pkgName = "@") {
            this.workspace = new Workspace();
            foreach (var intfPkg in this.pacMan.interfacesPkgs) {
                var libName = $"{intfPkg.interfaceYaml.vendor}.{intfPkg.interfaceYaml.name}-v{intfPkg.interfaceYaml.version}";
                this.addInterfacePackage(libName, intfPkg.definition);
            }
            
            this.projectPkg = new Package(pkgName, false);
            this.workspace.addPackage(this.projectPkg);
        }
        
        public void addInterfacePackage(string libName, string definitionFileContent) {
            var libPkg = new Package(libName, true);
            var file = TypeScriptParser2.parseFile(definitionFileContent, new SourcePath(libPkg, Package.INDEX));
            this.setupFile(file);
            libPkg.addFile(file, true);
            this.workspace.addPackage(libPkg);
        }
        
        public void setupFile(SourceFile file) {
            file.addAvailableSymbols(this.nativeExports.getAllExports());
            file.literalTypes = new LiteralTypes((((Class)file.availableSymbols.get("TsBoolean"))).type, (((Class)file.availableSymbols.get("TsNumber"))).type, (((Class)file.availableSymbols.get("TsString"))).type, (((Class)file.availableSymbols.get("RegExp"))).type, (((Class)file.availableSymbols.get("TsArray"))).type, (((Class)file.availableSymbols.get("TsMap"))).type, (((Class)file.availableSymbols.get("Error"))).type, (((Class)file.availableSymbols.get("Promise"))).type);
            file.arrayTypes = new ClassType[] { (((Class)file.availableSymbols.get("TsArray"))).type, (((Class)file.availableSymbols.get("IterableIterator"))).type, (((Class)file.availableSymbols.get("RegExpExecArray"))).type, (((Class)file.availableSymbols.get("TsString"))).type, (((Class)file.availableSymbols.get("Set"))).type };
        }
        
        public void addProjectFile(string fn, string content) {
            var file = TypeScriptParser2.parseFile(content, new SourcePath(this.projectPkg, fn));
            this.setupFile(file);
            this.projectPkg.addFile(file);
        }
        
        public void processWorkspace() {
            foreach (var pkg in Object.values(this.workspace.packages).filter(x => x.definitionOnly)) {
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
            
            var transforms = new List<ITransformer>();
            transforms.push(new ResolveGenericTypeIdentifiers());
            transforms.push(new ConvertToMethodCall());
            transforms.push(new ResolveUnresolvedTypes());
            transforms.push(new ResolveIdentifiers());
            transforms.push(new InstanceOfImplicitCast());
            transforms.push(new DetectMethodCalls());
            transforms.push(new InferTypes());
            transforms.push(new CollectInheritanceInfo());
            transforms.push(new FillMutabilityInfo());
            foreach (var trans in transforms) {
                trans.visitPackage(this.projectPkg);
                if (this.hooks != null)
                    this.hooks.afterStage(trans.name);
            }
        }
    }
}