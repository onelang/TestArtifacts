public class Operator {
    public String text;
    public Integer precedence;
    public Boolean isBinary;
    public Boolean isRightAssoc;
    public Boolean isPostfix;
    
    public Operator(String text, Integer precedence, Boolean isBinary, Boolean isRightAssoc, Boolean isPostfix)
    {
        this.text = text;
        this.precedence = precedence;
        this.isBinary = isBinary;
        this.isRightAssoc = isRightAssoc;
        this.isPostfix = isPostfix;
    }
}