using System.Collections.Generic;
using System.Threading.Tasks;
using System;

namespace StdLib
{
    public enum PackageType { Interface, Implementation }
    
    public interface PackageSource {
        Task<PackageBundle> getPackageBundle(PackageId[] ids, bool cachedOnly);
        
        Task<PackageBundle> getAllCached();
    }
    
    public interface InterfaceYaml {
        int fileversion { get; set; }
        string vendor { get; set; }
        string name { get; set; }
        int version { get; set; }
        string definitionfile { get; set; }
    }
    
    public class PackageId {
        public PackageType type;
        public string name;
        public string version;
        
        public PackageId(PackageType type, string name, string version) {
            this.type = type;
            this.name = name;
            this.version = version;
        }
    }
    
    public class PackageContent {
        public PackageId id;
        public Dictionary<string, string> files;
        
        public PackageContent(PackageId id, Dictionary<string, string> files, bool fromCache) {
            this.id = id;
            this.files = files;
        }
    }
    
    public class PackageBundle {
        public PackageContent[] packages;
        
        public PackageBundle(PackageContent[] packages) {
            this.packages = packages;
        }
    }
    
    public class PackageNativeImpl {
        public string pkgName;
        public string pkgVendor;
        public string pkgVersion;
        public string fileName;
        public string code;
    }
    
    public class InterfacePackage {
        public InterfaceYaml interfaceYaml;
        public string definition;
        public PackageContent content;
        
        public InterfacePackage(PackageContent content) {
            this.content = content;
            this.interfaceYaml = ((InterfaceYaml)YAML.safeLoad(content.files.get("interface.yaml")));
            this.definition = content.files.get(this.interfaceYaml.definitionfile);
        }
    }
    
    public class ImplPkgImplIntf {
        public string name;
        public int minver;
        public int maxver;
    }
    
    public class ImplPkgImplementation {
        public ImplPkgImplIntf interface_;
        public string language;
        public string[] nativeincludes;
        public string nativeincludedir;
    }
    
    public class ImplPackageYaml {
        public int fileversion;
        public string vendor;
        public string name;
        public string description;
        public string version;
        public string[] includes;
        public ImplPkgImplementation[] implements;
    }
    
    public class ImplementationPackage {
        public ImplPackageYaml implementationYaml;
        public ImplPkgImplementation[] implementations;
        public PackageContent content;
        
        public ImplementationPackage(PackageContent content) {
            this.implementations = new ImplPkgImplementation[0];
            this.content = content;
            this.implementationYaml = ((ImplPackageYaml)YAML.safeLoad(content.files.get("package.yaml")));
            this.implementations = new ImplPkgImplementation[0];
            foreach (var impl in this.implementationYaml.implements ?? new ImplPkgImplementation[0])
                this.implementations.push(impl);
            foreach (var include in this.implementationYaml.includes ?? new string[0]) {
                var included = ((ImplPackageYaml)YAML.safeLoad(content.files.get(include)));
                foreach (var impl in included.implements)
                    this.implementations.push(impl);
            }
        }
    }
    
    public class PackageManager {
        public InterfacePackage[] interfacesPkgs;
        public ImplementationPackage[] implementationPkgs;
        public PackageSource source;
        
        public PackageManager(PackageSource source) {
            this.interfacesPkgs = new InterfacePackage[0];
            this.implementationPkgs = new ImplementationPackage[0];
            this.source = source;
        }
        
        public async Task loadAllCached() {
            var allPackages = await this.source.getAllCached();
            
            foreach (var content in allPackages.packages.filter((PackageContent x) => { return x.id.type == PackageType.Interface; }))
                this.interfacesPkgs.push(new InterfacePackage(content));
            
            foreach (var content in allPackages.packages.filter((PackageContent x) => { return x.id.type == PackageType.Implementation; }))
                this.implementationPkgs.push(new ImplementationPackage(content));
        }
        
        public ImplPkgImplementation[] getLangImpls(string langName) {
            var allImpls = new ImplPkgImplementation[0];
            foreach (var pkg in this.implementationPkgs)
                foreach (var impl in pkg.implementations)
                    allImpls.push(impl);
            return allImpls.filter((ImplPkgImplementation x) => { return x.language == langName; });
        }
        
        public string getInterfaceDefinitions() {
            return this.interfacesPkgs.map((InterfacePackage x) => { return x.definition; }).join("\n");
        }
        
        public PackageNativeImpl[] getLangNativeImpls(string langName) {
            var result = new PackageNativeImpl[0];
            foreach (var pkg in this.implementationPkgs)
                foreach (var pkgImpl in pkg.implementations.filter((ImplPkgImplementation x) => { return x.language == langName; })) {
                    var fileNamePaths = new Dictionary<string, string> {};
                    foreach (var fileName in pkgImpl.nativeincludes ?? new string[0])
                        fileNamePaths.set(fileName, $"native/{fileName}");
                    
                    var incDir = pkgImpl.nativeincludedir;
                    if (incDir != null) {
                        if (!incDir.endsWith("/"))
                            incDir += "/";
                        var prefix = $"native/{incDir}";
                        foreach (var fn in Object.keys(pkg.content.files).filter((string x) => { return x.startsWith(prefix); }).map((string x) => { return x.substr(prefix.length()); }))
                            fileNamePaths.set(fn, $"{prefix}{fn}");
                    }
                    
                    foreach (var fileName in Object.keys(fileNamePaths)) {
                        var path = fileNamePaths.get(fileName);
                        var code = pkg.content.files.get(path);
                        if (code == null)
                            throw new Error($"File '{fileName}' was not found for package '{pkg.implementationYaml.name}'");
                        var impl = new PackageNativeImpl();
                        impl.pkgName = pkg.implementationYaml.name;
                        impl.pkgVendor = pkg.implementationYaml.vendor;
                        impl.pkgVersion = pkg.implementationYaml.version;
                        impl.fileName = fileName;
                        impl.code = code;
                        result.push(impl);
                    }
                }
            return result;
        }
    }
}