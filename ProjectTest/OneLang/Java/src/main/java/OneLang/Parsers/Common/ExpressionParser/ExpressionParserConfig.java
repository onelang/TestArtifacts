import java.util.Map;

public class ExpressionParserConfig {
    public String[] unary;
    public PrecedenceLevel[] precedenceLevels;
    public String[] rightAssoc;
    public Map<String, String> aliases;
    public String[] propertyAccessOps;
}