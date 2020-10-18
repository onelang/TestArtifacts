public class AwaitExpression extends Expression {
    public Expression expr;
    
    public AwaitExpression(Expression expr)
    {
        super();
        this.expr = expr;
    }
}