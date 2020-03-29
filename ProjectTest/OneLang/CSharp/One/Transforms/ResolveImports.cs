using System.Collections.Generic;
using System.Linq;
using System.Threading.Tasks;
using System;
using One.Ast;

namespace One.Transforms
{
    public class ResolveImports {
        public static void processWorkspace(Workspace ws) {
            foreach (var pkg in Object.values(ws.packages))
                foreach (var file in Object.values(pkg.files))
                    foreach (var imp in file.imports) {
                        var impPkg = ws.getPackage(imp.exportScope.packageName);
                        var scope = impPkg.getExportedScope(imp.exportScope.scopeName);
                        imp.imports = imp.importAll ? scope.getAllExports() : imp.imports.map((IImportable x) => { return x is UnresolvedImport ? scope.getExport(((UnresolvedImport)x).name) : x; });
                        file.addAvailableSymbols(imp.imports);
                    }
        }
    }
}