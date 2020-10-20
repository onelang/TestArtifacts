public class StatementDebugger extends AstTransformer {
    public String stmtFilterRegex;
    
    public StatementDebugger(String stmtFilterRegex)
    {
        super("StatementDebugger");
        this.stmtFilterRegex = stmtFilterRegex;
    }
    
    public Expression visitExpression(Expression expr) {
        // optimization: no need to process these...
        return null;
    }
    
    protected void visitField(Field field) {
        //if (field.name === "type" && field.parentInterface.name === "Interface") debugger;
        super.visitField(field);
    }
    
    public Statement visitStatement(Statement stmt) {
        var stmtRepr = TSOverviewGenerator.preview.stmt(stmt);
        // if (new RegExp(this.stmtFilterRegex).test(stmtRepr))
        //     debugger;
        return super.visitStatement(stmt);
    }
}