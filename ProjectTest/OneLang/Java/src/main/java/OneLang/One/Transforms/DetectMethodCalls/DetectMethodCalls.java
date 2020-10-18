public class DetectMethodCalls extends AstTransformer {
    public DetectMethodCalls()
    {
        super("DetectMethodCalls");
        
    }
    
    protected Expression visitExpression(Expression expr)
    {
        super.visitExpression(expr);
        if (expr instanceof UnresolvedCallExpression && ((UnresolvedCallExpression)expr).func instanceof PropertyAccessExpression)
            return new UnresolvedMethodCallExpression(((PropertyAccessExpression)((UnresolvedCallExpression)expr).func).object, ((PropertyAccessExpression)((UnresolvedCallExpression)expr).func).propertyName, ((UnresolvedCallExpression)expr).typeArgs, ((UnresolvedCallExpression)expr).args);
        return null;
    }
}