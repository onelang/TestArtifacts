public class ParenthesizedExpression extends Expression {
    public Expression expression;
    
    public ParenthesizedExpression(Expression expression)
    {
        super();
        this.expression = expression;
    }
}