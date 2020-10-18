public class RegexLiteral extends Expression {
    public String pattern;
    public Boolean caseInsensitive;
    public Boolean global;
    
    public RegexLiteral(String pattern, Boolean caseInsensitive, Boolean global)
    {
        super();
        this.pattern = pattern;
        this.caseInsensitive = caseInsensitive;
        this.global = global;
    }
}