public class UnsetStatement extends Statement {
    public Expression expression;
    
    public UnsetStatement(Expression expression)
    {
        super();
        this.expression = expression;
    }
}