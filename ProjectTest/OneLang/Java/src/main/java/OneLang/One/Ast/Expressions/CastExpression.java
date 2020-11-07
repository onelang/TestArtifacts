public class CastExpression extends Expression {
    public IType newType;
    public Expression expression;
    public InstanceOfExpression instanceOfCast;
    
    public CastExpression(IType newType, Expression expression)
    {
        super();
        this.newType = newType;
        this.expression = expression;
        this.instanceOfCast = null;
    }
}