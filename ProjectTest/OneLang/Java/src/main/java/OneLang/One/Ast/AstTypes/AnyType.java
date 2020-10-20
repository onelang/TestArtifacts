public class AnyType implements IPrimitiveType {
    public static AnyType instance;
    
    static {
        AnyType.instance = new AnyType();
    }
    
    public String repr() {
        return "Any";
    }
}