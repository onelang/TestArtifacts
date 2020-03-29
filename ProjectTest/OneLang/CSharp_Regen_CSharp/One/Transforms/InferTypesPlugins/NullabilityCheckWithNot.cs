using One.Transforms.InferTypesPlugins.Helpers;
using One.Ast;

namespace One.Transforms.InferTypesPlugins
{
    public class NullabilityCheckWithNot : InferTypesPlugin {
        public NullabilityCheckWithNot(): base("NullabilityCheckWithNot")
        {
            
        }
        
        public override bool canTransform(Expression expr) {
            return expr is UnaryExpression ? ((UnaryExpression)expr).operator_ == "!" : false;
        }
        
        public override Expression transform(Expression expr) {
            var unaryExpr = ((UnaryExpression)expr);
            if (unaryExpr.operator_ == "!") {
                this.main.processExpression(expr);
                var type = unaryExpr.operand.actualType;
                var litTypes = this.main.currentFile.literalTypes;
                if (type is ClassType && ((ClassType)type).decl != litTypes.boolean.decl && ((ClassType)type).decl != litTypes.numeric.decl)
                    return new BinaryExpression(unaryExpr.operand, "==", new NullLiteral());
            }
            
            return null;
        }
    }
}