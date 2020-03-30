using System.Collections.Generic;
using One.Ast;

namespace One.Transforms
{
    public class FillAttributesFromTrivia {
        public static Dictionary<string, string> processTrivia(string trivia) {
            var result = new Dictionary<string, string> {};
            if (trivia != null && trivia != "") {
                var regex = new RegExp("(?:\\n|^)\\s*(?:\\/\\/|#|\\/\\*\\*?)\\s*@([a-z0-9_.-]+) ?((?!\\n|\\*\\/|$).+)?");
                while (true) {
                    var match = regex.exec(trivia);
                    if (match == null)
                        break;
                    result.set(match.get(1), match.get(2) ?? "true");
                }
            }
            return result;
        }
        
        private static void process(IHasAttributesAndTrivia[] items) {
            foreach (var item in items)
                item.attributes = FillAttributesFromTrivia.processTrivia(item.leadingTrivia);
        }
        
        private static void processBlock(Block block) {
            if (block == null)
                return;
            FillAttributesFromTrivia.process(block.statements.ToArray());
            foreach (var stmt in block.statements) {
                if (stmt is ForeachStatement forStat)
                    FillAttributesFromTrivia.processBlock(forStat.body);
                else if (stmt is ForStatement forStat2)
                    FillAttributesFromTrivia.processBlock(forStat2.body);
                else if (stmt is IfStatement ifStat) {
                    FillAttributesFromTrivia.processBlock(ifStat.then);
                    FillAttributesFromTrivia.processBlock(ifStat.else_);
                }
            }
        }
        
        private static void processMethod(IMethodBaseWithTrivia method) {
            if (method == null)
                return;
            FillAttributesFromTrivia.process(new IMethodBaseWithTrivia[] { method });
            FillAttributesFromTrivia.processBlock(method.body);
        }
        
        public static void processFile(SourceFile file) {
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
        
        public static void processPackage(Package pkg) {
            foreach (var file in Object.values(pkg.files))
                FillAttributesFromTrivia.processFile(file);
        }
    }
}