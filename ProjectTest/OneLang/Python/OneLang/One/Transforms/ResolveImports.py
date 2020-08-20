from OneLangStdLib import *
import OneLang.One.Ast.Types as types

class ResolveImports:
    def __init__(self):
        pass
    
    @classmethod
    def process_workspace(cls, ws):
        for pkg in ws.packages.values():
            for file in pkg.files.values():
                for imp in file.imports:
                    imp_pkg = ws.get_package(imp.export_scope.package_name)
                    scope = imp_pkg.get_exported_scope(imp.export_scope.scope_name)
                    imp.imports = scope.get_all_exports() if imp.import_all else list(map(lambda x: scope.get_export(x.name) if isinstance(x, types.UnresolvedImport) else x, imp.imports))
                    file.add_available_symbols(imp.imports)