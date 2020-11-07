public class MapLiteralItem implements IAstNode {
    public String key;
    public Expression value;
    
    public MapLiteralItem(String key, Expression value)
    {
        this.key = key;
        this.value = value;
    }
}