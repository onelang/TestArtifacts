import java.util.Arrays;

public class InferForeachVarType extends InferTypesPlugin {
    public InferForeachVarType()
    {
        super("InferForeachVarType");
        
    }
    
    public Boolean handleStatement(Statement stmt)
    {
        if (stmt instanceof ForeachStatement) {
            ((ForeachStatement)stmt).items = this.main.runPluginsOn(((ForeachStatement)stmt).items) != null ? this.main.runPluginsOn(((ForeachStatement)stmt).items) : ((ForeachStatement)stmt).items;
            var arrayType = ((ForeachStatement)stmt).items.getType();
            var found = false;
            if (arrayType instanceof ClassType || arrayType instanceof InterfaceType) {
                var intfType = ((IInterfaceType)arrayType);
                var isArrayType = Arrays.stream(this.main.currentFile.arrayTypes).anyMatch(x -> x.decl == intfType.getDecl());
                if (isArrayType && intfType.getTypeArguments().length > 0) {
                    ((ForeachStatement)stmt).itemVar.setType(intfType.getTypeArguments()[0]);
                    found = true;
                }
            }
            
            if (!found && !(arrayType instanceof AnyType))
                this.errorMan.throw_("Expected array as Foreach items variable, but got " + arrayType.repr());
            
            this.main.processBlock(((ForeachStatement)stmt).body);
            return true;
        }
        return false;
    }
}