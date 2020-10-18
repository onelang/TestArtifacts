public class PropertyAccessExpression extends Expression {
    public Expression object;
    public String propertyName;
    
    public PropertyAccessExpression(Expression object, String propertyName)
    {
        super();
        this.object = object;
        this.propertyName = propertyName;
    }
}