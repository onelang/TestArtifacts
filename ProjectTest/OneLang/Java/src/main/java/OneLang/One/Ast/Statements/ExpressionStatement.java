public class ExpressionStatement extends Statement {
    public Expression expression;
    
    public ExpressionStatement(Expression expression)
    {
        super();
        this.expression = expression;
    }
}