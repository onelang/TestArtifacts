using System.Collections.Generic;
using System.Linq;
using System.Threading.Tasks;
using System;
using One.Transforms.InferTypesPlugins.Helpers;
using One.Ast;

namespace One.Transforms.InferTypesPlugins
{
    public class ArrayAndMapLiteralTypeInfer : InferTypesPlugin {
        public ArrayAndMapLiteralTypeInfer(): base("ArrayAndMapLiteralTypeInfer")
        {
            
        }
        
        protected Type_ inferArrayOrMapItemType(Expression[] items, Type_ expectedType, bool isMap) {
            var itemTypes = new List<Type_>();
            foreach (var item in items) {
                if (!itemTypes.some((Type_ t) => { return Type_.equals(t, item.getType()); }))
                    itemTypes.push(item.getType());
            }
            
            var literalType = isMap ? this.main.currentFile.literalTypes.map : this.main.currentFile.literalTypes.array;
            
            Type_ itemType = null;
            if (itemTypes.length() == 0) {
                if (expectedType == null) {
                    this.errorMan.warn($"Could not determine the type of an empty {(isMap ? "MapLiteral" : "ArrayLiteral")}, using AnyType instead");
                    itemType = AnyType.instance;
                }
                else if (expectedType is ClassType && ((ClassType)expectedType).decl == literalType.decl)
                    itemType = ((ClassType)expectedType).typeArguments.get(0);
                else
                    itemType = AnyType.instance;
            }
            else if (itemTypes.length() == 1)
                itemType = itemTypes.get(0);
            else if (!(expectedType is AnyType)) {
                this.errorMan.warn($"Could not determine the type of {(isMap ? "a MapLiteral" : "an ArrayLiteral")}! Multiple types were found: {itemTypes.map((Type_ x) => { return x.repr(); }).join(", ")}, using AnyType instead");
                itemType = AnyType.instance;
            }
            return itemType;
        }
        
        public override bool canDetectType(Expression expr) {
            return expr is ArrayLiteral || expr is MapLiteral;
        }
        
        public override bool detectType(Expression expr) {
            // make this work: `<{ [name: string]: SomeObject }> {}`
            if (expr.parentNode is CastExpression)
                expr.setExpectedType(((CastExpression)expr.parentNode).newType);
            else if (expr.parentNode is BinaryExpression && ((BinaryExpression)expr.parentNode).operator_ == "=" && ((BinaryExpression)expr.parentNode).right == expr)
                expr.setExpectedType(((BinaryExpression)expr.parentNode).left.actualType);
            else if (expr.parentNode is ConditionalExpression && (((ConditionalExpression)expr.parentNode).whenTrue == expr || ((ConditionalExpression)expr.parentNode).whenFalse == expr))
                expr.setExpectedType(((ConditionalExpression)expr.parentNode).whenTrue == expr ? ((ConditionalExpression)expr.parentNode).whenFalse.actualType : ((ConditionalExpression)expr.parentNode).whenTrue.actualType);
            
            if (expr is ArrayLiteral) {
                var itemType = this.inferArrayOrMapItemType(((ArrayLiteral)expr).items, ((ArrayLiteral)expr).expectedType, false);
                ((ArrayLiteral)expr).setActualType(new ClassType(this.main.currentFile.literalTypes.array.decl, new Type_[] { itemType }));
            }
            else if (expr is MapLiteral) {
                var itemType = this.inferArrayOrMapItemType(((MapLiteral)expr).items.map((MapLiteralItem x) => { return x.value; }), ((MapLiteral)expr).expectedType, true);
                ((MapLiteral)expr).setActualType(new ClassType(this.main.currentFile.literalTypes.map.decl, new Type_[] { itemType }));
            }
            
            return true;
        }
    }
}