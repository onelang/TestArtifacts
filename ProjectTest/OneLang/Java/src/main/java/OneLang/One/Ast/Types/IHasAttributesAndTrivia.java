import java.util.Map;

public interface IHasAttributesAndTrivia {
    String getLeadingTrivia();
    void setLeadingTrivia(String value);
    
    Map<String, String> getAttributes();
    void setAttributes(Map<String, String> value);
}