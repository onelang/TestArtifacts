public class TemplateStringPart {
    public Boolean isLiteral;
    public String literalText;
    public Expression expression;
    
    public TemplateStringPart(Boolean isLiteral, String literalText, Expression expression)
    {
        this.isLiteral = isLiteral;
        this.literalText = literalText;
        this.expression = expression;
    }
    
    public static TemplateStringPart Literal(String literalText) {
        return new TemplateStringPart(true, literalText, null);
    }
    
    public static TemplateStringPart Expression(Expression expr) {
        return new TemplateStringPart(false, null, expr);
    }
}