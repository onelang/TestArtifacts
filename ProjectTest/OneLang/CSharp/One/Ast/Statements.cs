using System.Collections.Generic;
using One.Ast;

namespace One.Ast
{
    public class Statement : IHasAttributesAndTrivia, IAstNode {
        public string leadingTrivia { get; set; }
        public Dictionary<string, string> attributes { get; set; }
        public Block parentBlock;
    }
    
    public class IfStatement : Statement {
        public Expression condition;
        public Block then;
        public Block else_;
        
        public IfStatement(Expression condition, Block then, Block else_): base() {
            this.condition = condition;
            this.then = then;
            this.else_ = else_;
        }
    }
    
    public class ReturnStatement : Statement {
        public Expression expression;
        
        public ReturnStatement(Expression expression): base() {
            this.expression = expression;
        }
    }
    
    public class ThrowStatement : Statement {
        public Expression expression;
        
        public ThrowStatement(Expression expression): base() {
            this.expression = expression;
        }
    }
    
    public class ExpressionStatement : Statement {
        public Expression expression;
        
        public ExpressionStatement(Expression expression): base() {
            this.expression = expression;
        }
    }
    
    public class BreakStatement : Statement {
        
    }
    
    public class ContinueStatement : Statement {
        
    }
    
    public class UnsetStatement : Statement {
        public Expression expression;
        
        public UnsetStatement(Expression expression): base() {
            this.expression = expression;
        }
    }
    
    public class VariableDeclaration : Statement, IVariableWithInitializer, IReferencable {
        public string name { get; set; }
        public Type_ type { get; set; }
        public Expression initializer { get; set; }
        public bool isMutable;
        public VariableDeclarationReference[] references;
        
        public VariableDeclaration(string name, Type_ type, Expression initializer): base() {
            this.references = new VariableDeclarationReference[0];
            this.name = name;
            this.type = type;
            this.initializer = initializer;
        }
        
        public Reference createReference() {
            return new VariableDeclarationReference(this);
        }
    }
    
    public class WhileStatement : Statement {
        public Expression condition;
        public Block body;
        
        public WhileStatement(Expression condition, Block body): base() {
            this.condition = condition;
            this.body = body;
        }
    }
    
    public class DoStatement : Statement {
        public Expression condition;
        public Block body;
        
        public DoStatement(Expression condition, Block body): base() {
            this.condition = condition;
            this.body = body;
        }
    }
    
    public class ForeachVariable : IVariable, IReferencable {
        public string name { get; set; }
        public Type_ type { get; set; }
        public ForeachVariableReference[] references;
        
        public ForeachVariable(string name) {
            this.references = new ForeachVariableReference[0];
            this.name = name;
        }
        
        public Reference createReference() {
            return new ForeachVariableReference(this);
        }
    }
    
    public class ForeachStatement : Statement {
        public ForeachVariable itemVar;
        public Expression items;
        public Block body;
        
        public ForeachStatement(ForeachVariable itemVar, Expression items, Block body): base() {
            this.itemVar = itemVar;
            this.items = items;
            this.body = body;
        }
    }
    
    public class ForVariable : IVariableWithInitializer, IReferencable {
        public string name { get; set; }
        public Type_ type { get; set; }
        public Expression initializer { get; set; }
        public ForVariableReference[] references;
        
        public ForVariable(string name, Type_ type, Expression initializer) {
            this.references = new ForVariableReference[0];
            this.name = name;
            this.type = type;
            this.initializer = initializer;
        }
        
        public Reference createReference() {
            return new ForVariableReference(this);
        }
    }
    
    public class ForStatement : Statement {
        public ForVariable itemVar;
        public Expression condition;
        public Expression incrementor;
        public Block body;
        
        public ForStatement(ForVariable itemVar, Expression condition, Expression incrementor, Block body): base() {
            this.itemVar = itemVar;
            this.condition = condition;
            this.incrementor = incrementor;
            this.body = body;
        }
    }
    
    public class CatchVariable : IVariable, IReferencable {
        public string name { get; set; }
        public Type_ type { get; set; }
        public CatchVariableReference[] references;
        
        public CatchVariable(string name, Type_ type) {
            this.references = new CatchVariableReference[0];
            this.name = name;
            this.type = type;
        }
        
        public Reference createReference() {
            return new CatchVariableReference(this);
        }
    }
    
    public class TryStatement : Statement {
        public Block tryBody;
        public CatchVariable catchVar;
        public Block catchBody;
        public Block finallyBody;
        
        public TryStatement(Block tryBody, CatchVariable catchVar, Block catchBody, Block finallyBody): base() {
            this.tryBody = tryBody;
            this.catchVar = catchVar;
            this.catchBody = catchBody;
            this.finallyBody = finallyBody;
            if (this.catchBody == null && this.finallyBody == null)
                throw new Error("try without catch and finally is not allowed");
        }
    }
}