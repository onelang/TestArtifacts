import java.util.LinkedHashMap;
import java.util.Arrays;
import java.util.ArrayList;
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
    
    public PackageBundle getPackageBundle(PackageId[] ids, Boolean cachedOnly) {
        throw new Error("Method not implemented.");
    }
    
    public PackageBundle getAllCached() {
        var packages = new LinkedHashMap<String, PackageContent>();
        var allFiles = OneFile.listFiles(this.packagesDir, true);
        for (var fn : allFiles) {
            if (fn.equals("bundle.json"))
                continue;
            // TODO: hack
            var pathParts = new ArrayList<>(Arrays.asList(fn.split("/", -1)));
            // [0]=implementations/interfaces, [1]=package-name, [2:]=path
            var type = pathParts.remove(0);
            var pkgDir = pathParts.remove(0);
            if (!type.equals("implementations") && !type.equals("interfaces"))
                continue;
            // skip e.g. bundle.json
            var pkgIdStr = type + "/" + pkgDir;
            var pkg = packages.get(pkgIdStr);
            if (pkg == null) {
                var pkgDirParts = new ArrayList<>(Arrays.asList(pkgDir.split("-", -1)));
                var version = pkgDirParts.remove(pkgDirParts.size() - 1).replaceAll("^v", "");
                var pkgType = type.equals("implementations") ? PackageType.Implementation : PackageType.Interface;
                var pkgId = new PackageId(pkgType, pkgDirParts.stream().collect(Collectors.joining("-")), version);
                pkg = new PackageContent(pkgId, new LinkedHashMap<String, String>(), true);
                packages.put(pkgIdStr, pkg);
            }
            pkg.files.put(pathParts.stream().collect(Collectors.joining("/")), OneFile.readText(this.packagesDir + "/" + fn));
        }
        return new PackageBundle(packages.values().toArray(PackageContent[]::new));
    }
}