import java.util.Map;
import java.util.HashMap;

public class GraphCycleDetector<TNode> {
    public Map<TNode, Boolean> nodeIsInPath;
    public IGraphVisitor<TNode> visitor;
    
    public GraphCycleDetector(IGraphVisitor<TNode> visitor)
    {
        this.visitor = visitor;
        this.nodeIsInPath = null;
    }
    
    public void findCycles(TNode[] nodes)
    {
        this.nodeIsInPath = new HashMap<TNode, Boolean>();
        for (var node : nodes)
            this.visitNode(node);
    }
    
    public Boolean visitNode(TNode node)
    {
        if (!this.nodeIsInPath.containsKey(node)) {
            // untouched node
            this.nodeIsInPath.put(node, true);
            this.visitor.processNode(node);
            this.nodeIsInPath.put(node, false);
            return false;
        }
        else
            // true = node used in current path = cycle
            // false = node was already scanned previously (not a cycle)
            return this.nodeIsInPath.get(node);
    }
}