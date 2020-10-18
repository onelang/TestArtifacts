public interface ITransformer {
    String getName();
    void setName(String value);
    
    void visitPackage(Package pkg);
}