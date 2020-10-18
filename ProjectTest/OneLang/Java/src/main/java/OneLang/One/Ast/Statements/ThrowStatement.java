public class ThrowStatement extends Statement {
    public Expression expression;
    
    public ThrowStatement(Expression expression)
    {
        super();
        this.expression = expression;
    }
}