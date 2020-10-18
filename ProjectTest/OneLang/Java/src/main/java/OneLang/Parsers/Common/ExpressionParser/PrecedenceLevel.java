public class PrecedenceLevel {
    public String name;
    public String[] operators;
    public Boolean binary;
    
    public PrecedenceLevel(String name, String[] operators, Boolean binary)
    {
        this.name = name;
        this.operators = operators;
        this.binary = binary;
    }
}