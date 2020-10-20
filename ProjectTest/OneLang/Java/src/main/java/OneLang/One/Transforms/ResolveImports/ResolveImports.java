import java.util.Arrays;

public class ResolveImports {
    public static void processWorkspace(Workspace ws) {
        for (var pkg : ws.packages.values().toArray(Package[]::new))
            for (var file : pkg.files.values().toArray(SourceFile[]::new))
                for (var imp : file.imports) {
                    var impPkg = ws.getPackage(imp.exportScope.packageName);
                    var scope = impPkg.getExportedScope(imp.exportScope.scopeName);
                    imp.imports = imp.importAll ? scope.getAllExports() : Arrays.stream(imp.imports).map(x -> x instanceof UnresolvedImport ? scope.getExport(((UnresolvedImport)x).getName()) : x).toArray(IImportable[]::new);
                    file.addAvailableSymbols(imp.imports);
                }
    }
}