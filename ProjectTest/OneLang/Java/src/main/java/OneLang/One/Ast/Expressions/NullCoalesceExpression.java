public class NullCoalesceExpression extends Expression {
    public Expression defaultExpr;
    public Expression exprIfNull;
    
    public NullCoalesceExpression(Expression defaultExpr, Expression exprIfNull)
    {
        super();
        this.defaultExpr = defaultExpr;
        this.exprIfNull = exprIfNull;
    }
}