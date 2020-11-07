public class ResolveIdentifiers extends AstTransformer {
    public SymbolLookup symbolLookup;
    
    public ResolveIdentifiers()
    {
        super("ResolveIdentifiers");
        this.symbolLookup = new SymbolLookup();
    }
    
    protected Expression visitIdentifier(Identifier id) {
        super.visitIdentifier(id);
        var symbol = this.symbolLookup.getSymbol(id.text);
        if (symbol == null) {
            this.errorMan.throw_("Identifier '" + id.text + "' was not found in available symbols");
            return null;
        }
        
        Reference ref = null;
        if (symbol instanceof Class && id.text.equals("this")) {
            var withinStaticMethod = this.currentMethod instanceof Method && ((Method)this.currentMethod).getIsStatic();
            ref = withinStaticMethod ? ((Reference)new StaticThisReference(((Class)symbol))) : new ThisReference(((Class)symbol));
        }
        else if (symbol instanceof Class && id.text.equals("super"))
            ref = new SuperReference(((Class)symbol));
        else
            ref = symbol.createReference();
        ref.parentNode = id.parentNode;
        return ref;
    }
    
    protected Statement visitStatement(Statement stmt) {
        if (stmt instanceof ForStatement) {
            this.symbolLookup.pushContext("For");
            if (((ForStatement)stmt).itemVar != null)
                this.symbolLookup.addSymbol(((ForStatement)stmt).itemVar.getName(), ((ForStatement)stmt).itemVar);
            super.visitStatement(((ForStatement)stmt));
            this.symbolLookup.popContext();
        }
        else if (stmt instanceof ForeachStatement) {
            this.symbolLookup.pushContext("Foreach");
            this.symbolLookup.addSymbol(((ForeachStatement)stmt).itemVar.getName(), ((ForeachStatement)stmt).itemVar);
            super.visitStatement(((ForeachStatement)stmt));
            this.symbolLookup.popContext();
        }
        else if (stmt instanceof TryStatement) {
            this.symbolLookup.pushContext("Try");
            this.visitBlock(((TryStatement)stmt).tryBody);
            if (((TryStatement)stmt).catchBody != null) {
                this.symbolLookup.addSymbol(((TryStatement)stmt).catchVar.getName(), ((TryStatement)stmt).catchVar);
                this.visitBlock(((TryStatement)stmt).catchBody);
                this.symbolLookup.popContext();
            }
            if (((TryStatement)stmt).finallyBody != null)
                this.visitBlock(((TryStatement)stmt).finallyBody);
        }
        else
            return super.visitStatement(stmt);
        return null;
    }
    
    protected Lambda visitLambda(Lambda lambda) {
        this.symbolLookup.pushContext("Lambda");
        for (var param : lambda.getParameters())
            this.symbolLookup.addSymbol(param.getName(), param);
        super.visitBlock(lambda.getBody());
        // directly process method's body without opening a new scope again
        this.symbolLookup.popContext();
        return null;
    }
    
    protected Block visitBlock(Block block) {
        this.symbolLookup.pushContext("block");
        super.visitBlock(block);
        this.symbolLookup.popContext();
        return null;
    }
    
    protected VariableDeclaration visitVariableDeclaration(VariableDeclaration stmt) {
        this.symbolLookup.addSymbol(stmt.getName(), stmt);
        return super.visitVariableDeclaration(stmt);
    }
    
    protected void visitMethodBase(IMethodBase method) {
        this.symbolLookup.pushContext(method instanceof Method ? "Method: " + ((Method)method).name : method instanceof Constructor ? "constructor" : "???");
        
        for (var param : method.getParameters()) {
            this.symbolLookup.addSymbol(param.getName(), param);
            if (param.getInitializer() != null)
                this.visitExpression(param.getInitializer());
        }
        
        if (method.getBody() != null)
            super.visitBlock(method.getBody());
        // directly process method's body without opening a new scope again
        
        this.symbolLookup.popContext();
    }
    
    protected void visitClass(Class cls) {
        this.symbolLookup.pushContext("Class: " + cls.getName());
        this.symbolLookup.addSymbol("this", cls);
        if (cls.baseClass instanceof ClassType)
            this.symbolLookup.addSymbol("super", ((ClassType)cls.baseClass).decl);
        super.visitClass(cls);
        this.symbolLookup.popContext();
    }
    
    public void visitFile(SourceFile sourceFile) {
        this.errorMan.resetContext(this);
        this.symbolLookup.pushContext("File: " + sourceFile.sourcePath.toString());
        
        for (var symbol : sourceFile.availableSymbols.values().toArray(IImportable[]::new)) {
            if (symbol instanceof Class)
                this.symbolLookup.addSymbol(((Class)symbol).getName(), ((Class)symbol));
            else if (symbol instanceof Interface) { }
            else if (symbol instanceof Enum)
                this.symbolLookup.addSymbol(((Enum)symbol).getName(), ((Enum)symbol));
            else if (symbol instanceof GlobalFunction)
                this.symbolLookup.addSymbol(((GlobalFunction)symbol).getName(), ((GlobalFunction)symbol));
            else { }
        }
        
        super.visitFile(sourceFile);
        
        this.symbolLookup.popContext();
        this.errorMan.resetContext();
    }
}