import java.util.Map;
import java.util.LinkedHashMap;

public class FillAttributesFromTrivia extends AstTransformer {
    public FillAttributesFromTrivia()
    {
        super("FillAttributesFromTrivia");
        
    }
    
    protected void visitAttributesAndTrivia(IHasAttributesAndTrivia node) {
        node.setAttributes(FillAttributesFromTrivia.processTrivia(node.getLeadingTrivia()));
    }
    
    protected Expression visitExpression(Expression expr) {
        return null;
    }
    
    public static Map<String, String> processTrivia(String trivia) {
        var result = new LinkedHashMap<String, String>();
        if (trivia != null && !trivia.equals("")) {
            var regex = new RegExp("(?:\\n|^)\\s*(?://|#|/\\*\\*?)\\s*@([a-z0-9_.-]+) ?((?!\\n|\\*/|$).+)?");
            while (true) {
                var match = regex.exec(trivia);
                if (match == null)
                    break;
                if (result.containsKey(match[1]))
                    // @php $result[$match[1]] .= "\n" . $match[2];
                    // @python result[match[1]] += "\n" + match[2]
                    // @csharp result[match[1]] += "\n" + match[2];
                    // @java result.put(match[1], result.get(match[1]) + "\n" + match[2]);
                    result.put(match[1], result.get(match[1]) + "\n" + match[2]);
                else
                    result.put(match[1], match[2] != null ? match[2] : "true");
            }
        }
        return result;
    }
}