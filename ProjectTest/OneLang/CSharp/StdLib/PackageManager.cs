using System.Collections.Generic;
using System.Linq;
using System.Threading.Tasks;
using System;
using Utils;

namespace StdLib
{
    public enum PackageType { Interface, Implementation }
    
    public interface PackageSource {
        Task<PackageBundle> getPackageBundle(PackageId[] ids, bool cachedOnly);
        
        Task<PackageBundle> getAllCached();
    }
    
    public class PackageId {
        public PackageType type;
        public string name;
        public string version;
        
        public PackageId(PackageType type, string name, string version)
        {
            this.type = type;
            this.name = name;
            this.version = version;
        }
    }
    
    public class PackageContent {
        public PackageId id;
        public Dictionary<string, string> files;
        
        public PackageContent(PackageId id, Dictionary<string, string> files, bool fromCache)
        {
            this.id = id;
            this.files = files;
        }
    }
    
    public class PackageBundle {
        public PackageContent[] packages;
        
        public PackageBundle(PackageContent[] packages)
        {
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
    
    public class InterfaceDependency {
        public string name;
        public double minver;
    }
    
    public class InterfaceYaml {
        public int fileVersion;
        public string vendor;
        public string name;
        public double version;
        public string definitionFile;
        public InterfaceDependency[] dependencies;
    }
    
    public class InterfacePackage {
        public InterfaceYaml interfaceYaml;
        public string definition;
        public PackageContent content;
        
        public InterfacePackage(PackageContent content)
        {
            this.content = content;
            this.interfaceYaml = Yaml.load<InterfaceYaml>(content.files.get("interface.yaml"));
            this.definition = content.files.get(this.interfaceYaml.definitionFile);
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
        public string[] nativeIncludes;
        public string nativeIncludeDir;
    }
    
    public class ImplPackageYaml {
        public int fileVersion;
        public string vendor;
        public string name;
        public string description;
        public string version;
        public string[] includes;
        public ImplPkgImplementation[] implements;
    }
    
    public class ImplementationPackage {
        public ImplPackageYaml implementationYaml;
        public List<ImplPkgImplementation> implementations;
        public PackageContent content;
        
        public ImplementationPackage(PackageContent content)
        {
            this.content = content;
            this.implementations = new List<ImplPkgImplementation>();
            this.implementationYaml = Yaml.load<ImplPackageYaml>(content.files.get("package.yaml"));
            this.implementations = new List<ImplPkgImplementation>();
            foreach (var impl in this.implementationYaml.implements ?? new ImplPkgImplementation[0])
                this.implementations.push(impl);
            foreach (var include in this.implementationYaml.includes ?? new string[0]) {
                var included = Yaml.load<ImplPackageYaml>(content.files.get(include));
                foreach (var impl in included.implements)
                    this.implementations.push(impl);
            }
        }
    }
    
    public class PackageManager {
        public List<InterfacePackage> interfacesPkgs;
        public List<ImplementationPackage> implementationPkgs;
        public PackageSource source;
        
        public PackageManager(PackageSource source)
        {
            this.source = source;
            this.interfacesPkgs = new List<InterfacePackage>();
            this.implementationPkgs = new List<ImplementationPackage>();
        }
        
        public async Task loadAllCached() {
            var allPackages = await this.source.getAllCached();
            
            foreach (var content in allPackages.packages.filter((PackageContent x) => { return x.id.type == PackageType.Interface; }))
                this.interfacesPkgs.push(new InterfacePackage(content));
            
            foreach (var content in allPackages.packages.filter((PackageContent x) => { return x.id.type == PackageType.Implementation; }))
                this.implementationPkgs.push(new ImplementationPackage(content));
        }
        
        public ImplPkgImplementation[] getLangImpls(string langName) {
            var allImpls = new List<ImplPkgImplementation>();
            foreach (var pkg in this.implementationPkgs)
                foreach (var impl in pkg.implementations)
                    allImpls.push(impl);
            return allImpls.filter((ImplPkgImplementation x) => { return x.language == langName; });
        }
        
        public string getInterfaceDefinitions() {
            return this.interfacesPkgs.map((InterfacePackage x) => { return x.definition; }).join("\n");
        }
        
        public PackageNativeImpl[] getLangNativeImpls(string langName) {
            var result = new List<PackageNativeImpl>();
            foreach (var pkg in this.implementationPkgs)
                foreach (var pkgImpl in pkg.implementations.filter((ImplPkgImplementation x) => { return x.language == langName; })) {
                    var fileNamePaths = new Dictionary<string, string> {};
                    foreach (var fileName in pkgImpl.nativeIncludes ?? new string[0])
                        fileNamePaths.set(fileName, $"native/{fileName}");
                    
                    var incDir = pkgImpl.nativeIncludeDir;
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
            return result.ToArray();
        }
    }
}