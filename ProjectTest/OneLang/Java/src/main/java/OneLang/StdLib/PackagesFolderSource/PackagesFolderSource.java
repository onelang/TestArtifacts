import java.util.HashMap;
import java.util.Arrays;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class PackagesFolderSource implements PackageSource {
    public String packagesDir;
    
    public PackagesFolderSource(String packagesDir)
    {
        this.packagesDir = packagesDir;
    }
    
    public PackagesFolderSource() {
        this("packages");
    }
    
    public PackageBundle getPackageBundle(PackageId[] ids, Boolean cachedOnly)
    {
        throw new Error("Method not implemented.");
    }
    
    public PackageBundle getAllCached()
    {
        var packages = new HashMap<String, PackageContent>();
        var allFiles = OneFile.listFiles(this.packagesDir, true);
        for (var fn : allFiles) {
            if (fn == "bundle.json")
                continue;
            // TODO: hack
            var pathParts = Arrays.asList(fn.split("/"));
            // [0]=implementations/interfaces, [1]=package-name, [2:]=path
            var type = pathParts.remove(0);
            var pkgDir = pathParts.remove(0);
            if (type != "implementations" && type != "interfaces")
                continue;
            // skip e.g. bundle.json
            var pkgIdStr = type + "/" + pkgDir;
            var pkg = packages.get(pkgIdStr);
            if (pkg == null) {
                var pkgDirParts = Arrays.asList(pkgDir.split("-"));
                var version = pkgDirParts.remove(pkgDirParts.size() - 1).replaceAll(Pattern.quote("^v"), "");
                var pkgType = type == "implementations" ? PackageType.Implementation : PackageType.Interface;
                var pkgId = new PackageId(pkgType, pkgDirParts.stream().collect(Collectors.joining("-")), version);
                pkg = new PackageContent(pkgId, new HashMap<String, String>(), true);
                packages.put(pkgIdStr, pkg);
            }
            pkg.files.put(pathParts.stream().collect(Collectors.joining("/")), OneFile.readText(this.packagesDir + "/" + fn));
        }
        return new PackageBundle(packages.values().toArray(PackageContent[]::new));
    }
}