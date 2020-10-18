public class MapLiteral extends Expression {
    public MapLiteralItem[] items;
    
    public MapLiteral(MapLiteralItem[] items)
    {
        super();
        this.items = items;
    }
}