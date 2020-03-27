using System.Collections.Generic;
using System.Threading.Tasks;
using One.Ast;

namespace One.Transforms
{
    public class FillAttributesFromTrivia {
        static public Dictionary<string, string> processTrivia(string trivia) {
            var result = new Dictionary<string, string> {};
            if (trivia != "") {
                var regex = new RegExp("(?:n|^)s*(?://|#)s*@([a-z0-9_.-]+)(?: ([^n]+)|$|n)");
                while (true) {
                    var match = regex.exec(trivia);
                    if (match == null)
                        break;
                    result.set(match.get(0), match.get(1));
                }
            }
            return result;
        }
        
        static private void process(IHasAttributesAndTrivia[] items) {
            foreach (var item in items)
                item.attributes = FillAttributesFromTrivia.processTrivia(item.leadingTrivia);
        }
        
        static private void processBlock(Block block) {
            if (block == null)
                return;
            FillAttributesFromTrivia.process(block.statements);
            foreach (var stmt in block.statements)
                if (stmt is ForeachStatement)
                    FillAttributesFromTrivia.processBlock(((ForeachStatement)stmt).body);
                else if (stmt is ForStatement)
                    FillAttributesFromTrivia.processBlock(((ForStatement)stmt).body);
                else if (stmt is IfStatement) {
                    FillAttributesFromTrivia.processBlock(((IfStatement)stmt).then);
                    FillAttributesFromTrivia.processBlock(((IfStatement)stmt).else_);
                }
        }
        
        static private void processMethod(IMethodBaseWithTrivia method) {
            if (method == null)
                return;
            FillAttributesFromTrivia.process(new[] { method });
            FillAttributesFromTrivia.processBlock(method.body);
        }
        
        static public void processFile(SourceFile file) {
            FillAttributesFromTrivia.process(file.imports);
            FillAttributesFromTrivia.process(file.enums);
            FillAttributesFromTrivia.process(file.interfaces);
            FillAttributesFromTrivia.process(file.classes);
            FillAttributesFromTrivia.processBlock(file.mainBlock);
            
            foreach (var intf in file.interfaces)
                foreach (var method in intf.methods)
                    FillAttributesFromTrivia.processMethod(method);
            
            foreach (var cls in file.classes) {
                FillAttributesFromTrivia.processMethod(cls.constructor_);
                FillAttributesFromTrivia.process(cls.fields);
                FillAttributesFromTrivia.process(cls.properties);
                foreach (var prop in cls.properties) {
                    FillAttributesFromTrivia.processBlock(prop.getter);
                    FillAttributesFromTrivia.processBlock(prop.setter);
                }
                foreach (var method in cls.methods)
                    FillAttributesFromTrivia.processMethod(method);
            }
        }
        
        static public void processPackage(Package pkg) {
            foreach (var file in Object.values(pkg.files))
                FillAttributesFromTrivia.processFile(file);
        }
    }
}