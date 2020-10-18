public interface IParser {
    NodeManager getNodeManager();
    void setNodeManager(NodeManager value);
    
    SourceFile parse();
}