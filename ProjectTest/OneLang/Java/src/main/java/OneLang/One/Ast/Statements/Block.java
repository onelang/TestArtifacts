import java.util.List;
import java.util.Arrays;
import java.util.ArrayList;

public class Block {
    public List<Statement> statements;
    
    public Block(Statement[] statements)
    {
        this.statements = new ArrayList<>(Arrays.asList(statements));
    }
}