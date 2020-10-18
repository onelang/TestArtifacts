public class UnaryExpression extends Expression {
    public UnaryType unaryType;
    public String operator;
    public Expression operand;
    
    public UnaryExpression(UnaryType unaryType, String operator, Expression operand)
    {
        super();
        this.unaryType = unaryType;
        this.operator = operator;
        this.operand = operand;
    }
}