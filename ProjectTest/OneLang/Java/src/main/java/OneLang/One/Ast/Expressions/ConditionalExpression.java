public class ConditionalExpression extends Expression {
    public Expression condition;
    public Expression whenTrue;
    public Expression whenFalse;
    
    public ConditionalExpression(Expression condition, Expression whenTrue, Expression whenFalse)
    {
        super();
        this.condition = condition;
        this.whenTrue = whenTrue;
        this.whenFalse = whenFalse;
    }
}