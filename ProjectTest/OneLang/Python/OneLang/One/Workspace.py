from OneLangStdLib import *
import OneLang.One.ErrorManager as errorMan
import OneLang.One.Ast.Types as types

class Workspace:
    def __init__(self):
        self.packages = {}
        self.error_manager = errorMan.ErrorManager()
    
    def add_package(self, pkg):
        self.packages[pkg.name] = pkg
    
    def get_package(self, name):
        pkg = self.packages.get(name)
        if pkg == None:
            raise Error(f'''Package was not found: "{name}"''')
        return pkg