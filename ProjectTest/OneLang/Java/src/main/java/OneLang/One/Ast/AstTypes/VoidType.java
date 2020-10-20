public class VoidType implements IPrimitiveType {
    public static VoidType instance;
    
    static {
        VoidType.instance = new VoidType();
    }
    
    public String repr() {
        return "Void";
    }
}