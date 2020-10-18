import java.util.Arrays;

public class ResolveFuncCalls extends InferTypesPlugin {
    public ResolveFuncCalls()
    {
        super("ResolveFuncCalls");
        
    }
    
    public Boolean canTransform(Expression expr)
    {
        return expr instanceof UnresolvedCallExpression;
    }
    
    public Expression transform(Expression expr)
    {
        var callExpr = ((UnresolvedCallExpression)expr);
        if (callExpr.func instanceof GlobalFunctionReference) {
            var newExpr = new GlobalFunctionCallExpression(((GlobalFunctionReference)callExpr.func).decl, callExpr.args);
            callExpr.args = Arrays.stream(callExpr.args).map(arg -> this.main.runPluginsOn(arg) != null ? this.main.runPluginsOn(arg) : arg).toArray(Expression[]::new);
            newExpr.setActualType(((GlobalFunctionReference)callExpr.func).decl.returns);
            return newExpr;
        }
        else {
            this.main.processExpression(expr);
            if (callExpr.func.actualType instanceof LambdaType) {
                var newExpr = new LambdaCallExpression(callExpr.func, callExpr.args);
                newExpr.setActualType(((LambdaType)callExpr.func.actualType).returnType);
                return newExpr;
            }
            else
                return expr;
        }
    }
}