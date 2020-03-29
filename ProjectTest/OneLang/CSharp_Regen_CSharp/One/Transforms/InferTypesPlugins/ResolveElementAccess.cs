using One.Transforms.InferTypesPlugins.Helpers;
using One.Ast;
using One.Transforms.InferTypesPlugins;

namespace One.Transforms.InferTypesPlugins
{
    public class ResolveElementAccess : InferTypesPlugin {
        public ResolveElementAccess(): base("ResolveElementAccess")
        {
            
        }
        
        public override bool canTransform(Expression expr) {
            var isSet = expr is BinaryExpression && ((BinaryExpression)expr).left is ElementAccessExpression && ((BinaryExpression)expr).operator_ == "=";
            return expr is ElementAccessExpression || isSet;
        }
        
        public bool isMapOrArrayType(Type_ type) {
            return Type_.isAssignableTo(type, this.main.currentFile.literalTypes.map) || this.main.currentFile.arrayTypes.some((ClassType x) => { return Type_.isAssignableTo(type, x); });
        }
        
        public override Expression transform(Expression expr) {
            if (expr is BinaryExpression && ((BinaryExpression)expr).left is ElementAccessExpression) {
                ((ElementAccessExpression)((BinaryExpression)expr).left).object_ = this.main.runPluginsOn(((ElementAccessExpression)((BinaryExpression)expr).left).object_);
                if (this.isMapOrArrayType(((ElementAccessExpression)((BinaryExpression)expr).left).object_.getType()))
                    return new UnresolvedMethodCallExpression(((ElementAccessExpression)((BinaryExpression)expr).left).object_, "set", new Type_[0], new Expression[] { ((ElementAccessExpression)((BinaryExpression)expr).left).elementExpr, ((BinaryExpression)expr).right });
            }
            else if (expr is ElementAccessExpression) {
                ((ElementAccessExpression)expr).object_ = this.main.runPluginsOn(((ElementAccessExpression)expr).object_);
                if (this.isMapOrArrayType(((ElementAccessExpression)expr).object_.getType()))
                    return new UnresolvedMethodCallExpression(((ElementAccessExpression)expr).object_, "get", new Type_[0], new Expression[] { ((ElementAccessExpression)expr).elementExpr });
                else if (((ElementAccessExpression)expr).elementExpr is StringLiteral)
                    return new PropertyAccessExpression(((ElementAccessExpression)expr).object_, ((StringLiteral)((ElementAccessExpression)expr).elementExpr).stringValue);
            }
            return expr;
        }
    }
}