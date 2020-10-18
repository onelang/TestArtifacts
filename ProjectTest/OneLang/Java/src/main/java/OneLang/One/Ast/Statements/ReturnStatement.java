public class ReturnStatement extends Statement {
    public Expression expression;
    
    public ReturnStatement(Expression expression)
    {
        super();
        this.expression = expression;
    }
}