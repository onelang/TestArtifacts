using One.Ast;

namespace One.Ast
{
    public class AstHelper {
        static public IInterface[] collectAllBaseInterfaces(IInterface intf) {
            var result = new Set<IInterface>();
            var toBeProcessed = new[] { intf };
            
            while (toBeProcessed.length() > 0) {
                var curr = toBeProcessed.pop();
                result.add(curr);
                
                if (curr is Class && ((Class)curr).baseClass != null)
                    toBeProcessed.push((((ClassType)((Class)curr).baseClass)).decl);
                
                foreach (var baseIntf in curr.baseInterfaces)
                    toBeProcessed.push((((InterfaceType)baseIntf)).decl);
            }
            
            return Array.from(result.values());
        }
    }
}