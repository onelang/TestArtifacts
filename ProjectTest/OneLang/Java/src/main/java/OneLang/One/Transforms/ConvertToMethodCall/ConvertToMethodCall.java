public class ConvertToMethodCall extends AstTransformer {
    public ConvertToMethodCall()
    {
        super("ConvertToMethodCall");
        
    }
    
    protected Expression visitExpression(Expression expr)
    {
        var origExpr = expr;
        
        expr = super.visitExpression(expr) != null ? super.visitExpression(expr) : expr;
        
        if (expr instanceof BinaryExpression && ((BinaryExpression)expr).operator == "in")
            expr = new UnresolvedCallExpression(new PropertyAccessExpression(((BinaryExpression)expr).right, "hasKey"), new IType[0], new Expression[] { ((BinaryExpression)expr).left });
        
        expr.parentNode = origExpr.parentNode;
        return expr;
    }
}