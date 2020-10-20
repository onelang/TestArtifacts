import java.util.List;
import java.util.ArrayList;

public class NodeManager {
    public List<Object> nodes;
    public Reader reader;
    
    public NodeManager(Reader reader)
    {
        this.reader = reader;
        this.nodes = new ArrayList<Object>();
    }
    
    public void addNode(Object node, Integer start) {
        this.nodes.add(node);
    }
}