public class ArrayLiteral extends Expression {
    public Expression[] items;
    
    public ArrayLiteral(Expression[] items)
    {
        super();
        this.items = items;
    }
}