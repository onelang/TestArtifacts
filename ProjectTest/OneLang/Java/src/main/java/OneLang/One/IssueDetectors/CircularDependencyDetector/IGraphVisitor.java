public interface IGraphVisitor<TNode> {
    void processNode(TNode node);
}