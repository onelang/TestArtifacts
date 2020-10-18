using One;
using One.Ast;
using Utils;

namespace Utils
{
    public class StatementDebugger : AstTransformer {
        public string stmtFilterRegex;
        
        public StatementDebugger(string stmtFilterRegex): base("StatementDebugger")
        {
            this.stmtFilterRegex = stmtFilterRegex;
        }
        
        public override Expression visitExpression(Expression expr)
        {
            // optimization: no need to process these...
            return null;
        }
        
        protected override void visitField(Field field)
        {
            //if (field.name === "type" && field.parentInterface.name === "Interface") debugger;
            base.visitField(field);
        }
        
        public override Statement visitStatement(Statement stmt)
        {
            var stmtRepr = TSOverviewGenerator.preview.stmt(stmt);
            // if (new RegExp(this.stmtFilterRegex).test(stmtRepr))
            //     debugger;
            return base.visitStatement(stmt);
        }
    }
}