public interface ITransformer {
    String getName();
    void setName(String value);
    
    void visitFiles(SourceFile[] files);
}