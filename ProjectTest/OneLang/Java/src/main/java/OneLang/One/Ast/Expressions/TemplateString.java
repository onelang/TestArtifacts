public class TemplateString extends Expression {
    public TemplateStringPart[] parts;
    
    public TemplateString(TemplateStringPart[] parts)
    {
        super();
        this.parts = parts;
    }
}