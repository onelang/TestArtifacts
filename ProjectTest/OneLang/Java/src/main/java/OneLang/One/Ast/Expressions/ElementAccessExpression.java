public class ElementAccessExpression extends Expression {
    public Expression object;
    public Expression elementExpr;
    
    public ElementAccessExpression(Expression object, Expression elementExpr)
    {
        super();
        this.object = object;
        this.elementExpr = elementExpr;
    }
}