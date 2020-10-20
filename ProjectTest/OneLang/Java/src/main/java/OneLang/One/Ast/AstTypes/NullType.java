public class NullType implements IPrimitiveType {
    public static NullType instance;
    
    static {
        NullType.instance = new NullType();
    }
    
    public String repr() {
        return "Null";
    }
}