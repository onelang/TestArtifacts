using System.Collections.Generic;
using System.Linq;
using System.Threading.Tasks;
using System;
using One.Transforms.InferTypesPlugins.Helpers;
using One.Ast;

namespace One.Transforms.InferTypesPlugins
{
    public class InferForeachVarType : InferTypesPlugin {
        public InferForeachVarType(): base("InferForeachVarType")
        {
            
        }
        
        public override bool handleStatement(Statement stmt) {
            if (stmt is ForeachStatement) {
                ((ForeachStatement)stmt).items = this.main.runPluginsOn(((ForeachStatement)stmt).items) ?? ((ForeachStatement)stmt).items;
                var arrayType = ((ForeachStatement)stmt).items.getType();
                var found = false;
                if (arrayType is ClassType || arrayType is InterfaceType) {
                    var intfType = ((IInterfaceType)arrayType);
                    var isArrayType = this.main.currentFile.arrayTypes.some((ClassType x) => { return x.decl == intfType.getDecl(); });
                    if (isArrayType && intfType.typeArguments.length() > 0) {
                        ((ForeachStatement)stmt).itemVar.type = intfType.typeArguments.get(0);
                        found = true;
                    }
                }
                
                if (!found && !(arrayType is AnyType))
                    this.errorMan.throw_($"Expected array as Foreach items variable, but got {arrayType.repr()}");
                
                this.main.processBlock(((ForeachStatement)stmt).body);
                return true;
            }
            return false;
        }
    }
}