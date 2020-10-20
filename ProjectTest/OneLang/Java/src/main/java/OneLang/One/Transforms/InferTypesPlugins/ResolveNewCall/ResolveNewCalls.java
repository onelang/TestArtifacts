public class ResolveNewCalls extends InferTypesPlugin {
    public ResolveNewCalls()
    {
        super("ResolveNewCalls");
        
    }
    
    public Boolean canTransform(Expression expr) {
        return expr instanceof NewExpression;
    }
    
    public Expression transform(Expression expr) {
        var newExpr = ((NewExpression)expr);
        for (Integer i = 0; i < newExpr.args.length; i++) {
            newExpr.args[i].setExpectedType(newExpr.cls.decl.constructor_.getParameters()[i].getType());
            newExpr.args[i] = this.main.runPluginsOn(newExpr.args[i]) != null ? this.main.runPluginsOn(newExpr.args[i]) : newExpr.args[i];
        }
        expr.setActualType(newExpr.cls);
        return expr;
    }
}