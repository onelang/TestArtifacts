import java.util.Map;

public class Statement implements IHasAttributesAndTrivia, IAstNode {
    String leadingTrivia = null;
    public String getLeadingTrivia() { return this.leadingTrivia; }
    public void setLeadingTrivia(String value) { this.leadingTrivia = value; }
    
    Map<String, String> attributes = null;
    public Map<String, String> getAttributes() { return this.attributes; }
    public void setAttributes(Map<String, String> value) { this.attributes = value; }
    
    public Statement()
    {
        
    }
}