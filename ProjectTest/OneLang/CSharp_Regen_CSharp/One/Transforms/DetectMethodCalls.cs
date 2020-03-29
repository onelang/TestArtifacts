using One;
using One.Ast;

namespace One.Transforms
{
    public class DetectMethodCalls : AstTransformer {
        public DetectMethodCalls(): base("DetectMethodCalls")
        {
            
        }
        
        protected override Expression visitExpression(Expression expr) {
            base.visitExpression(expr);
            if (expr is UnresolvedCallExpression && ((UnresolvedCallExpression)expr).func is PropertyAccessExpression)
                return new UnresolvedMethodCallExpression(((PropertyAccessExpression)((UnresolvedCallExpression)expr).func).object_, ((PropertyAccessExpression)((UnresolvedCallExpression)expr).func).propertyName, ((UnresolvedCallExpression)expr).typeArgs, ((UnresolvedCallExpression)expr).args);
            return null;
        }
    }
}