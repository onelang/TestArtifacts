using One;
using One.Ast;
using System.Collections.Generic;

namespace One
{
    public class Workspace {
        public Dictionary<string, Package> packages;
        public ErrorManager errorManager;
        
        public Workspace()
        {
            this.packages = new Dictionary<string, Package> {};
            this.errorManager = new ErrorManager();
        }
        
        public void addPackage(Package pkg) {
            this.packages.set(pkg.name, pkg);
        }
        
        public Package getPackage(string name) {
            var pkg = this.packages.get(name);
            if (pkg == null)
                throw new Error($"Package was not found: \"{name}\"");
            return pkg;
        }
    }
}