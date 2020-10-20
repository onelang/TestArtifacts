public class NumericLiteral extends Expression {
    public String valueAsText;
    
    public NumericLiteral(String valueAsText)
    {
        super();
        this.valueAsText = valueAsText;
    }
    
    public IExpression copy() {
        return new NumericLiteral(this.valueAsText);
    }
}