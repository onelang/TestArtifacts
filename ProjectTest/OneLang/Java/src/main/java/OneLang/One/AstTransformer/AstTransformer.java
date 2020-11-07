import java.util.Arrays;
import java.util.ArrayList;

public class AstTransformer implements ITransformer {
    public ErrorManager errorMan;
    public SourceFile currentFile;
    public IInterface currentInterface;
    public IMethodBase currentMethod;
    public Statement currentStatement;
    
    String name;
    public String getName() { return this.name; }
    public void setName(String value) { this.name = value; }
    
    public AstTransformer(String name)
    {
        this.setName(name);
        this.errorMan = new ErrorManager();
        this.currentFile = null;
        this.currentInterface = null;
        this.currentMethod = null;
        this.currentStatement = null;
    }
    
    protected void visitAttributesAndTrivia(IHasAttributesAndTrivia node) {
        
    }
    
    protected IType visitType(IType type) {
        if (type instanceof ClassType || type instanceof InterfaceType || type instanceof UnresolvedType) {
            var type2 = ((IHasTypeArguments)type);
            type2.setTypeArguments(Arrays.stream(type2.getTypeArguments()).map(x -> this.visitType(x) != null ? this.visitType(x) : x).toArray(IType[]::new));
        }
        else if (type instanceof LambdaType) {
            for (var mp : ((LambdaType)type).parameters)
                this.visitMethodParameter(mp);
            ((LambdaType)type).returnType = this.visitType(((LambdaType)type).returnType) != null ? this.visitType(((LambdaType)type).returnType) : ((LambdaType)type).returnType;
        }
        return null;
    }
    
    protected Expression visitIdentifier(Identifier id) {
        return null;
    }
    
    protected IVariable visitVariable(IVariable variable) {
        if (variable.getType() != null)
            variable.setType(this.visitType(variable.getType()) != null ? this.visitType(variable.getType()) : variable.getType());
        return null;
    }
    
    protected IVariableWithInitializer visitVariableWithInitializer(IVariableWithInitializer variable) {
        this.visitVariable(variable);
        if (variable.getInitializer() != null)
            variable.setInitializer(this.visitExpression(variable.getInitializer()) != null ? this.visitExpression(variable.getInitializer()) : variable.getInitializer());
        return null;
    }
    
    protected VariableDeclaration visitVariableDeclaration(VariableDeclaration stmt) {
        this.visitVariableWithInitializer(stmt);
        return null;
    }
    
    protected Statement visitUnknownStatement(Statement stmt) {
        this.errorMan.throw_("Unknown statement type");
        return null;
    }
    
    protected Statement visitStatement(Statement stmt) {
        this.currentStatement = stmt;
        this.visitAttributesAndTrivia(stmt);
        if (stmt instanceof ReturnStatement) {
            if (((ReturnStatement)stmt).expression != null)
                ((ReturnStatement)stmt).expression = this.visitExpression(((ReturnStatement)stmt).expression) != null ? this.visitExpression(((ReturnStatement)stmt).expression) : ((ReturnStatement)stmt).expression;
        }
        else if (stmt instanceof ExpressionStatement)
            ((ExpressionStatement)stmt).expression = this.visitExpression(((ExpressionStatement)stmt).expression) != null ? this.visitExpression(((ExpressionStatement)stmt).expression) : ((ExpressionStatement)stmt).expression;
        else if (stmt instanceof IfStatement) {
            ((IfStatement)stmt).condition = this.visitExpression(((IfStatement)stmt).condition) != null ? this.visitExpression(((IfStatement)stmt).condition) : ((IfStatement)stmt).condition;
            ((IfStatement)stmt).then = this.visitBlock(((IfStatement)stmt).then) != null ? this.visitBlock(((IfStatement)stmt).then) : ((IfStatement)stmt).then;
            if (((IfStatement)stmt).else_ != null)
                ((IfStatement)stmt).else_ = this.visitBlock(((IfStatement)stmt).else_) != null ? this.visitBlock(((IfStatement)stmt).else_) : ((IfStatement)stmt).else_;
        }
        else if (stmt instanceof ThrowStatement)
            ((ThrowStatement)stmt).expression = this.visitExpression(((ThrowStatement)stmt).expression) != null ? this.visitExpression(((ThrowStatement)stmt).expression) : ((ThrowStatement)stmt).expression;
        else if (stmt instanceof VariableDeclaration)
            return this.visitVariableDeclaration(((VariableDeclaration)stmt));
        else if (stmt instanceof WhileStatement) {
            ((WhileStatement)stmt).condition = this.visitExpression(((WhileStatement)stmt).condition) != null ? this.visitExpression(((WhileStatement)stmt).condition) : ((WhileStatement)stmt).condition;
            ((WhileStatement)stmt).body = this.visitBlock(((WhileStatement)stmt).body) != null ? this.visitBlock(((WhileStatement)stmt).body) : ((WhileStatement)stmt).body;
        }
        else if (stmt instanceof DoStatement) {
            ((DoStatement)stmt).condition = this.visitExpression(((DoStatement)stmt).condition) != null ? this.visitExpression(((DoStatement)stmt).condition) : ((DoStatement)stmt).condition;
            ((DoStatement)stmt).body = this.visitBlock(((DoStatement)stmt).body) != null ? this.visitBlock(((DoStatement)stmt).body) : ((DoStatement)stmt).body;
        }
        else if (stmt instanceof ForStatement) {
            if (((ForStatement)stmt).itemVar != null)
                this.visitVariableWithInitializer(((ForStatement)stmt).itemVar);
            ((ForStatement)stmt).condition = this.visitExpression(((ForStatement)stmt).condition) != null ? this.visitExpression(((ForStatement)stmt).condition) : ((ForStatement)stmt).condition;
            ((ForStatement)stmt).incrementor = this.visitExpression(((ForStatement)stmt).incrementor) != null ? this.visitExpression(((ForStatement)stmt).incrementor) : ((ForStatement)stmt).incrementor;
            ((ForStatement)stmt).body = this.visitBlock(((ForStatement)stmt).body) != null ? this.visitBlock(((ForStatement)stmt).body) : ((ForStatement)stmt).body;
        }
        else if (stmt instanceof ForeachStatement) {
            this.visitVariable(((ForeachStatement)stmt).itemVar);
            ((ForeachStatement)stmt).items = this.visitExpression(((ForeachStatement)stmt).items) != null ? this.visitExpression(((ForeachStatement)stmt).items) : ((ForeachStatement)stmt).items;
            ((ForeachStatement)stmt).body = this.visitBlock(((ForeachStatement)stmt).body) != null ? this.visitBlock(((ForeachStatement)stmt).body) : ((ForeachStatement)stmt).body;
        }
        else if (stmt instanceof TryStatement) {
            ((TryStatement)stmt).tryBody = this.visitBlock(((TryStatement)stmt).tryBody) != null ? this.visitBlock(((TryStatement)stmt).tryBody) : ((TryStatement)stmt).tryBody;
            if (((TryStatement)stmt).catchBody != null) {
                this.visitVariable(((TryStatement)stmt).catchVar);
                ((TryStatement)stmt).catchBody = this.visitBlock(((TryStatement)stmt).catchBody) != null ? this.visitBlock(((TryStatement)stmt).catchBody) : ((TryStatement)stmt).catchBody;
            }
            if (((TryStatement)stmt).finallyBody != null)
                ((TryStatement)stmt).finallyBody = this.visitBlock(((TryStatement)stmt).finallyBody) != null ? this.visitBlock(((TryStatement)stmt).finallyBody) : ((TryStatement)stmt).finallyBody;
        }
        else if (stmt instanceof BreakStatement) { }
        else if (stmt instanceof UnsetStatement)
            ((UnsetStatement)stmt).expression = this.visitExpression(((UnsetStatement)stmt).expression) != null ? this.visitExpression(((UnsetStatement)stmt).expression) : ((UnsetStatement)stmt).expression;
        else if (stmt instanceof ContinueStatement) { }
        else
            return this.visitUnknownStatement(stmt);
        return null;
    }
    
    protected Block visitBlock(Block block) {
        block.statements = new ArrayList<>(Arrays.asList(block.statements.stream().map(x -> this.visitStatement(x) != null ? this.visitStatement(x) : x).toArray(Statement[]::new)));
        return null;
    }
    
    protected TemplateString visitTemplateString(TemplateString expr) {
        for (Integer i = 0; i < expr.parts.length; i++) {
            var part = expr.parts[i];
            if (!part.isLiteral)
                part.expression = this.visitExpression(part.expression) != null ? this.visitExpression(part.expression) : part.expression;
        }
        return null;
    }
    
    protected Expression visitUnknownExpression(Expression expr) {
        this.errorMan.throw_("Unknown expression type");
        return null;
    }
    
    protected Lambda visitLambda(Lambda lambda) {
        this.visitMethodBase(lambda);
        return null;
    }
    
    protected VariableReference visitVariableReference(VariableReference varRef) {
        return null;
    }
    
    protected Expression visitExpression(Expression expr) {
        if (expr instanceof BinaryExpression) {
            ((BinaryExpression)expr).left = this.visitExpression(((BinaryExpression)expr).left) != null ? this.visitExpression(((BinaryExpression)expr).left) : ((BinaryExpression)expr).left;
            ((BinaryExpression)expr).right = this.visitExpression(((BinaryExpression)expr).right) != null ? this.visitExpression(((BinaryExpression)expr).right) : ((BinaryExpression)expr).right;
        }
        else if (expr instanceof NullCoalesceExpression) {
            ((NullCoalesceExpression)expr).defaultExpr = this.visitExpression(((NullCoalesceExpression)expr).defaultExpr) != null ? this.visitExpression(((NullCoalesceExpression)expr).defaultExpr) : ((NullCoalesceExpression)expr).defaultExpr;
            ((NullCoalesceExpression)expr).exprIfNull = this.visitExpression(((NullCoalesceExpression)expr).exprIfNull) != null ? this.visitExpression(((NullCoalesceExpression)expr).exprIfNull) : ((NullCoalesceExpression)expr).exprIfNull;
        }
        else if (expr instanceof UnresolvedCallExpression) {
            ((UnresolvedCallExpression)expr).func = this.visitExpression(((UnresolvedCallExpression)expr).func) != null ? this.visitExpression(((UnresolvedCallExpression)expr).func) : ((UnresolvedCallExpression)expr).func;
            ((UnresolvedCallExpression)expr).typeArgs = Arrays.stream(((UnresolvedCallExpression)expr).typeArgs).map(x -> this.visitType(x) != null ? this.visitType(x) : x).toArray(IType[]::new);
            ((UnresolvedCallExpression)expr).args = Arrays.stream(((UnresolvedCallExpression)expr).args).map(x -> this.visitExpression(x) != null ? this.visitExpression(x) : x).toArray(Expression[]::new);
        }
        else if (expr instanceof UnresolvedMethodCallExpression) {
            ((UnresolvedMethodCallExpression)expr).object = this.visitExpression(((UnresolvedMethodCallExpression)expr).object) != null ? this.visitExpression(((UnresolvedMethodCallExpression)expr).object) : ((UnresolvedMethodCallExpression)expr).object;
            ((UnresolvedMethodCallExpression)expr).typeArgs = Arrays.stream(((UnresolvedMethodCallExpression)expr).typeArgs).map(x -> this.visitType(x) != null ? this.visitType(x) : x).toArray(IType[]::new);
            ((UnresolvedMethodCallExpression)expr).args = Arrays.stream(((UnresolvedMethodCallExpression)expr).args).map(x -> this.visitExpression(x) != null ? this.visitExpression(x) : x).toArray(Expression[]::new);
        }
        else if (expr instanceof ConditionalExpression) {
            ((ConditionalExpression)expr).condition = this.visitExpression(((ConditionalExpression)expr).condition) != null ? this.visitExpression(((ConditionalExpression)expr).condition) : ((ConditionalExpression)expr).condition;
            ((ConditionalExpression)expr).whenTrue = this.visitExpression(((ConditionalExpression)expr).whenTrue) != null ? this.visitExpression(((ConditionalExpression)expr).whenTrue) : ((ConditionalExpression)expr).whenTrue;
            ((ConditionalExpression)expr).whenFalse = this.visitExpression(((ConditionalExpression)expr).whenFalse) != null ? this.visitExpression(((ConditionalExpression)expr).whenFalse) : ((ConditionalExpression)expr).whenFalse;
        }
        else if (expr instanceof Identifier)
            return this.visitIdentifier(((Identifier)expr));
        else if (expr instanceof UnresolvedNewExpression) {
            this.visitType(((UnresolvedNewExpression)expr).cls);
            ((UnresolvedNewExpression)expr).args = Arrays.stream(((UnresolvedNewExpression)expr).args).map(x -> this.visitExpression(x) != null ? this.visitExpression(x) : x).toArray(Expression[]::new);
        }
        else if (expr instanceof NewExpression) {
            this.visitType(((NewExpression)expr).cls);
            ((NewExpression)expr).args = Arrays.stream(((NewExpression)expr).args).map(x -> this.visitExpression(x) != null ? this.visitExpression(x) : x).toArray(Expression[]::new);
        }
        else if (expr instanceof TemplateString)
            return this.visitTemplateString(((TemplateString)expr));
        else if (expr instanceof ParenthesizedExpression)
            ((ParenthesizedExpression)expr).expression = this.visitExpression(((ParenthesizedExpression)expr).expression) != null ? this.visitExpression(((ParenthesizedExpression)expr).expression) : ((ParenthesizedExpression)expr).expression;
        else if (expr instanceof UnaryExpression)
            ((UnaryExpression)expr).operand = this.visitExpression(((UnaryExpression)expr).operand) != null ? this.visitExpression(((UnaryExpression)expr).operand) : ((UnaryExpression)expr).operand;
        else if (expr instanceof PropertyAccessExpression)
            ((PropertyAccessExpression)expr).object = this.visitExpression(((PropertyAccessExpression)expr).object) != null ? this.visitExpression(((PropertyAccessExpression)expr).object) : ((PropertyAccessExpression)expr).object;
        else if (expr instanceof ElementAccessExpression) {
            ((ElementAccessExpression)expr).object = this.visitExpression(((ElementAccessExpression)expr).object) != null ? this.visitExpression(((ElementAccessExpression)expr).object) : ((ElementAccessExpression)expr).object;
            ((ElementAccessExpression)expr).elementExpr = this.visitExpression(((ElementAccessExpression)expr).elementExpr) != null ? this.visitExpression(((ElementAccessExpression)expr).elementExpr) : ((ElementAccessExpression)expr).elementExpr;
        }
        else if (expr instanceof ArrayLiteral)
            ((ArrayLiteral)expr).items = Arrays.stream(((ArrayLiteral)expr).items).map(x -> this.visitExpression(x) != null ? this.visitExpression(x) : x).toArray(Expression[]::new);
        else if (expr instanceof MapLiteral)
            for (var item : ((MapLiteral)expr).items)
                item.value = this.visitExpression(item.value) != null ? this.visitExpression(item.value) : item.value;
        else if (expr instanceof StringLiteral) { }
        else if (expr instanceof BooleanLiteral) { }
        else if (expr instanceof NumericLiteral) { }
        else if (expr instanceof NullLiteral) { }
        else if (expr instanceof RegexLiteral) { }
        else if (expr instanceof CastExpression) {
            ((CastExpression)expr).newType = this.visitType(((CastExpression)expr).newType) != null ? this.visitType(((CastExpression)expr).newType) : ((CastExpression)expr).newType;
            ((CastExpression)expr).expression = this.visitExpression(((CastExpression)expr).expression) != null ? this.visitExpression(((CastExpression)expr).expression) : ((CastExpression)expr).expression;
        }
        else if (expr instanceof InstanceOfExpression) {
            ((InstanceOfExpression)expr).expr = this.visitExpression(((InstanceOfExpression)expr).expr) != null ? this.visitExpression(((InstanceOfExpression)expr).expr) : ((InstanceOfExpression)expr).expr;
            ((InstanceOfExpression)expr).checkType = this.visitType(((InstanceOfExpression)expr).checkType) != null ? this.visitType(((InstanceOfExpression)expr).checkType) : ((InstanceOfExpression)expr).checkType;
        }
        else if (expr instanceof AwaitExpression)
            ((AwaitExpression)expr).expr = this.visitExpression(((AwaitExpression)expr).expr) != null ? this.visitExpression(((AwaitExpression)expr).expr) : ((AwaitExpression)expr).expr;
        else if (expr instanceof Lambda)
            return this.visitLambda(((Lambda)expr));
        else if (expr instanceof ClassReference) { }
        else if (expr instanceof EnumReference) { }
        else if (expr instanceof ThisReference) { }
        else if (expr instanceof StaticThisReference) { }
        else if (expr instanceof MethodParameterReference)
            return this.visitVariableReference(((MethodParameterReference)expr));
        else if (expr instanceof VariableDeclarationReference)
            return this.visitVariableReference(((VariableDeclarationReference)expr));
        else if (expr instanceof ForVariableReference)
            return this.visitVariableReference(((ForVariableReference)expr));
        else if (expr instanceof ForeachVariableReference)
            return this.visitVariableReference(((ForeachVariableReference)expr));
        else if (expr instanceof CatchVariableReference)
            return this.visitVariableReference(((CatchVariableReference)expr));
        else if (expr instanceof GlobalFunctionReference) { }
        else if (expr instanceof SuperReference) { }
        else if (expr instanceof InstanceFieldReference) {
            ((InstanceFieldReference)expr).object = this.visitExpression(((InstanceFieldReference)expr).object) != null ? this.visitExpression(((InstanceFieldReference)expr).object) : ((InstanceFieldReference)expr).object;
            return this.visitVariableReference(((InstanceFieldReference)expr));
        }
        else if (expr instanceof InstancePropertyReference) {
            ((InstancePropertyReference)expr).object = this.visitExpression(((InstancePropertyReference)expr).object) != null ? this.visitExpression(((InstancePropertyReference)expr).object) : ((InstancePropertyReference)expr).object;
            return this.visitVariableReference(((InstancePropertyReference)expr));
        }
        else if (expr instanceof StaticFieldReference)
            return this.visitVariableReference(((StaticFieldReference)expr));
        else if (expr instanceof StaticPropertyReference)
            return this.visitVariableReference(((StaticPropertyReference)expr));
        else if (expr instanceof EnumMemberReference) { }
        else if (expr instanceof StaticMethodCallExpression) {
            ((StaticMethodCallExpression)expr).setTypeArgs(Arrays.stream(((StaticMethodCallExpression)expr).getTypeArgs()).map(x -> this.visitType(x) != null ? this.visitType(x) : x).toArray(IType[]::new));
            ((StaticMethodCallExpression)expr).setArgs(Arrays.stream(((StaticMethodCallExpression)expr).getArgs()).map(x -> this.visitExpression(x) != null ? this.visitExpression(x) : x).toArray(Expression[]::new));
        }
        else if (expr instanceof GlobalFunctionCallExpression)
            ((GlobalFunctionCallExpression)expr).args = Arrays.stream(((GlobalFunctionCallExpression)expr).args).map(x -> this.visitExpression(x) != null ? this.visitExpression(x) : x).toArray(Expression[]::new);
        else if (expr instanceof InstanceMethodCallExpression) {
            ((InstanceMethodCallExpression)expr).object = this.visitExpression(((InstanceMethodCallExpression)expr).object) != null ? this.visitExpression(((InstanceMethodCallExpression)expr).object) : ((InstanceMethodCallExpression)expr).object;
            ((InstanceMethodCallExpression)expr).setTypeArgs(Arrays.stream(((InstanceMethodCallExpression)expr).getTypeArgs()).map(x -> this.visitType(x) != null ? this.visitType(x) : x).toArray(IType[]::new));
            ((InstanceMethodCallExpression)expr).setArgs(Arrays.stream(((InstanceMethodCallExpression)expr).getArgs()).map(x -> this.visitExpression(x) != null ? this.visitExpression(x) : x).toArray(Expression[]::new));
        }
        else if (expr instanceof LambdaCallExpression)
            ((LambdaCallExpression)expr).args = Arrays.stream(((LambdaCallExpression)expr).args).map(x -> this.visitExpression(x) != null ? this.visitExpression(x) : x).toArray(Expression[]::new);
        else
            return this.visitUnknownExpression(expr);
        return null;
    }
    
    protected void visitMethodParameter(MethodParameter methodParameter) {
        this.visitAttributesAndTrivia(methodParameter);
        this.visitVariableWithInitializer(methodParameter);
    }
    
    protected void visitMethodBase(IMethodBase method) {
        for (var item : method.getParameters())
            this.visitMethodParameter(item);
        
        if (method.getBody() != null)
            method.setBody(this.visitBlock(method.getBody()) != null ? this.visitBlock(method.getBody()) : method.getBody());
    }
    
    protected void visitMethod(Method method) {
        this.currentMethod = method;
        this.visitAttributesAndTrivia(method);
        this.visitMethodBase(method);
        method.returns = this.visitType(method.returns) != null ? this.visitType(method.returns) : method.returns;
        this.currentMethod = null;
    }
    
    protected void visitGlobalFunction(GlobalFunction func) {
        this.visitMethodBase(func);
        func.returns = this.visitType(func.returns) != null ? this.visitType(func.returns) : func.returns;
    }
    
    protected void visitConstructor(Constructor constructor) {
        this.currentMethod = constructor;
        this.visitAttributesAndTrivia(constructor);
        this.visitMethodBase(constructor);
        this.currentMethod = null;
    }
    
    protected void visitField(Field field) {
        this.visitAttributesAndTrivia(field);
        this.visitVariableWithInitializer(field);
    }
    
    protected void visitProperty(Property prop) {
        this.visitAttributesAndTrivia(prop);
        this.visitVariable(prop);
        if (prop.getter != null)
            prop.getter = this.visitBlock(prop.getter) != null ? this.visitBlock(prop.getter) : prop.getter;
        if (prop.setter != null)
            prop.setter = this.visitBlock(prop.setter) != null ? this.visitBlock(prop.setter) : prop.setter;
    }
    
    protected void visitInterface(Interface intf) {
        this.currentInterface = intf;
        this.visitAttributesAndTrivia(intf);
        intf.setBaseInterfaces(Arrays.stream(intf.getBaseInterfaces()).map(x -> this.visitType(x) != null ? this.visitType(x) : x).toArray(IType[]::new));
        for (var field : intf.getFields())
            this.visitField(field);
        for (var method : intf.getMethods())
            this.visitMethod(method);
        this.currentInterface = null;
    }
    
    protected void visitClass(Class cls) {
        this.currentInterface = cls;
        this.visitAttributesAndTrivia(cls);
        if (cls.constructor_ != null)
            this.visitConstructor(cls.constructor_);
        
        cls.baseClass = this.visitType(cls.baseClass) != null ? this.visitType(cls.baseClass) : cls.baseClass;
        cls.setBaseInterfaces(Arrays.stream(cls.getBaseInterfaces()).map(x -> this.visitType(x) != null ? this.visitType(x) : x).toArray(IType[]::new));
        for (var field : cls.getFields())
            this.visitField(field);
        for (var prop : cls.properties)
            this.visitProperty(prop);
        for (var method : cls.getMethods())
            this.visitMethod(method);
        this.currentInterface = null;
    }
    
    protected void visitEnum(Enum enum_) {
        this.visitAttributesAndTrivia(enum_);
        for (var value : enum_.values)
            this.visitEnumMember(value);
    }
    
    protected void visitEnumMember(EnumMember enumMember) {
        
    }
    
    protected void visitImport(Import imp) {
        this.visitAttributesAndTrivia(imp);
    }
    
    public void visitFile(SourceFile sourceFile) {
        this.errorMan.resetContext(this);
        this.currentFile = sourceFile;
        for (var imp : sourceFile.imports)
            this.visitImport(imp);
        for (var enum_ : sourceFile.enums)
            this.visitEnum(enum_);
        for (var intf : sourceFile.interfaces)
            this.visitInterface(intf);
        for (var cls : sourceFile.classes)
            this.visitClass(cls);
        for (var func : sourceFile.funcs)
            this.visitGlobalFunction(func);
        sourceFile.mainBlock = this.visitBlock(sourceFile.mainBlock) != null ? this.visitBlock(sourceFile.mainBlock) : sourceFile.mainBlock;
        this.currentFile = null;
    }
    
    public void visitFiles(SourceFile[] files) {
        for (var file : files)
            this.visitFile(file);
    }
    
    public void visitPackage(Package pkg) {
        this.visitFiles(pkg.files.values().toArray(SourceFile[]::new));
    }
}