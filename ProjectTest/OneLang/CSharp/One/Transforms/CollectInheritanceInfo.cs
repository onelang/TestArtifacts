using System.Collections.Generic;
using System.Linq;
using System.Threading.Tasks;
using One.Ast;

namespace One.Transforms
{
    public class CollectInheritanceInfo {
        public void visitClass(Class cls) {
            var allBaseIIntfs = cls.getAllBaseInterfaces();
            var intfs = allBaseIIntfs.map((IInterface x) => { return x is Interface ? ((Interface)x) : null; }).filter((Interface x) => { return x != null; });
            var clses = allBaseIIntfs.map((IInterface x) => { return x is Class ? ((Class)x) : null; }).filter((Class x) => { return x != null && x != cls; });
            
            foreach (var field in cls.fields)
                field.interfaceDeclarations = intfs.map((Interface x) => { return x.fields.find((Field f) => { return f.name == field.name; }); }).filter((Field x) => { return x != null; });
            
            foreach (var method in cls.methods) {
                method.interfaceDeclarations = intfs.map((Interface x) => { return x.methods.find((Method m) => { return m.name == method.name; }); }).filter((Method x) => { return x != null; });
                method.overrides = clses.map((Class x) => { return x.methods.find((Method m) => { return m.name == method.name; }); }).find((Method x) => { return x != null; });
                if (method.overrides != null)
                    method.overrides.overriddenBy.push(method);
            }
        }
        
        public void visitPackage(Package pkg) {
            foreach (var file in Object.values(pkg.files))
                foreach (var cls in file.classes)
                    this.visitClass(cls);
        }
    }
}