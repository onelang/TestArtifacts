using System.Collections.Generic;
using System.Linq;
using System.Threading.Tasks;
using System;
using One.Transforms.InferTypesPlugins.Helpers;
using One.Ast;

namespace One.Transforms.InferTypesPlugins
{
    public class TypeScriptNullCoalesce : InferTypesPlugin {
        public TypeScriptNullCoalesce(): base("TypeScriptNullCoalesce")
        {
            
        }
        
        public override bool canTransform(Expression expr) {
            return expr is BinaryExpression && ((BinaryExpression)expr).operator_ == "||";
        }
        
        public override Expression transform(Expression expr) {
            if (expr is BinaryExpression && ((BinaryExpression)expr).operator_ == "||") {
                var litTypes = this.main.currentFile.literalTypes;
                
                ((BinaryExpression)expr).left = this.main.runPluginsOn(((BinaryExpression)expr).left) ?? ((BinaryExpression)expr).left;
                var leftType = ((BinaryExpression)expr).left.getType();
                
                if (((BinaryExpression)expr).right is ArrayLiteral && ((ArrayLiteral)((BinaryExpression)expr).right).items.length() == 0) {
                    if (leftType is ClassType && ((ClassType)leftType).decl == litTypes.array.decl) {
                        ((ArrayLiteral)((BinaryExpression)expr).right).setActualType(((ClassType)leftType));
                        return new NullCoalesceExpression(((BinaryExpression)expr).left, ((ArrayLiteral)((BinaryExpression)expr).right));
                    }
                }
                
                if (((BinaryExpression)expr).right is MapLiteral && ((MapLiteral)((BinaryExpression)expr).right).items.length() == 0) {
                    if (leftType is ClassType && ((ClassType)leftType).decl == litTypes.map.decl) {
                        ((MapLiteral)((BinaryExpression)expr).right).setActualType(((ClassType)leftType));
                        return new NullCoalesceExpression(((BinaryExpression)expr).left, ((MapLiteral)((BinaryExpression)expr).right));
                    }
                }
                
                ((BinaryExpression)expr).right = this.main.runPluginsOn(((BinaryExpression)expr).right) ?? ((BinaryExpression)expr).right;
                var rightType = ((BinaryExpression)expr).right.getType();
                
                if (((BinaryExpression)expr).right is NullLiteral)
                    // something-which-can-be-undefined || null
                    return ((BinaryExpression)expr).left;
                else if (Type_.isAssignableTo(rightType, leftType) && !Type_.equals(rightType, this.main.currentFile.literalTypes.boolean))
                    return new NullCoalesceExpression(((BinaryExpression)expr).left, ((BinaryExpression)expr).right);
            }
            return null;
        }
    }
}