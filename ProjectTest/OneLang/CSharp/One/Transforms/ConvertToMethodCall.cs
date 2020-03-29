using System.Collections.Generic;
using System.Linq;
using System.Threading.Tasks;
using One.Ast;
using One;

namespace One.Transforms
{
    public class ConvertToMethodCall : AstTransformer {
        public ConvertToMethodCall(): base("ConvertToMethodCall")
        {
            
        }
        
        protected override Expression visitExpression(Expression expr) {
            var origExpr = expr;
            
            expr = base.visitExpression(expr) ?? expr;
            
            if (expr is BinaryExpression && ((BinaryExpression)expr).operator_ == "in")
                expr = new UnresolvedCallExpression(new PropertyAccessExpression(((BinaryExpression)expr).right, "hasKey"), new Type_[0], new Expression[] { ((BinaryExpression)expr).left });
            
            expr.parentNode = origExpr.parentNode;
            return expr;
        }
    }
}