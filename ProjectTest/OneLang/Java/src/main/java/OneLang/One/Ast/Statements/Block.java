import java.util.List;
import java.util.Arrays;

public class Block {
    public List<Statement> statements;
    
    public Block(Statement[] statements)
    {
        this.statements = Arrays.asList(statements);
    }
}