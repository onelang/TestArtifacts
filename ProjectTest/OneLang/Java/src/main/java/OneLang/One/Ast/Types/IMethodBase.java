public interface IMethodBase extends IAstNode {
    MethodParameter[] getParameters();
    void setParameters(MethodParameter[] value);
    
    Block getBody();
    void setBody(Block value);
    
    Boolean getThrows();
    void setThrows(Boolean value);
}