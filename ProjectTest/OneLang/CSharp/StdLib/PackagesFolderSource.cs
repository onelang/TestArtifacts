using System.Collections.Generic;
using System.Linq;
using System.Threading.Tasks;
using System;
using StdLib;
using Utils;

namespace StdLib
{
    public class PackagesFolderSource : PackageSource {
        public string packagesDir;
        
        public PackagesFolderSource(string packagesDir = "packages")
        {
            this.packagesDir = packagesDir;
        }
        
        public Task<PackageBundle> getPackageBundle(PackageId[] ids, bool cachedOnly) {
            throw new Error("Method not implemented.");
        }
        
        public async Task<PackageBundle> getAllCached() {
            var fs = ((Fs)await Global.import("fs"));
            var glob = ((Glob)await Global.import("glob"));
            var path = ((Path)await Global.import("path"));
            
            var packages = new Dictionary<string, PackageContent> {};
            var allFiles = glob.sync($"{this.packagesDir}/**/*", new Dictionary<string, bool> { ["nodir"] = true });
            foreach (var fn in allFiles) {
                var pathParts = path.relative(this.packagesDir, fn).split(new RegExp("\\/"));
                // [0]=implementations/interfaces, [1]=package-name, [2:]=path
                var type = pathParts.shift();
                var pkgDir = pathParts.shift();
                if (type != "implementations" && type != "interfaces")
                    continue;
                // skip e.g. bundle.json
                var pkgIdStr = $"{type}/{pkgDir}";
                var pkg = packages.get(pkgIdStr);
                if (pkg == null) {
                    var pkgDirParts = pkgDir.split(new RegExp("-"));
                    var version = pkgDirParts.pop().replace(new RegExp("^v"), "");
                    var pkgType = type == "implementations" ? PackageType.Implementation : PackageType.Interface;
                    var pkgId = new PackageId(pkgType, pkgDirParts.join("-"), version);
                    pkg = new PackageContent(pkgId, new Dictionary<string, string> {}, true);
                    packages.set(pkgIdStr, pkg);
                }
                pkg.files.set(pathParts.join("/"), fs.readFileSync(fn, "utf-8"));
            }
            return new PackageBundle(Object.values(packages));
        }
    }
}