import java.util.List;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.stream.Collectors;

public class InferTypes extends AstTransformer {
    protected InferTypesStage stage;
    public List<InferTypesPlugin> plugins;
    public Integer contextInfoIdx = 0;
    
    public InferTypes()
    {
        super("InferTypes");
        this.plugins = new ArrayList<InferTypesPlugin>();
        this.addPlugin(new BasicTypeInfer());
        this.addPlugin(new ArrayAndMapLiteralTypeInfer());
        this.addPlugin(new ResolveFieldAndPropertyAccess());
        this.addPlugin(new ResolveMethodCalls());
        this.addPlugin(new LambdaResolver());
        this.addPlugin(new InferReturnType());
        this.addPlugin(new ResolveEnumMemberAccess());
        this.addPlugin(new TypeScriptNullCoalesce());
        this.addPlugin(new InferForeachVarType());
        this.addPlugin(new ResolveFuncCalls());
        this.addPlugin(new NullabilityCheckWithNot());
        this.addPlugin(new ResolveNewCalls());
        this.addPlugin(new ResolveElementAccess());
    }
    
    public void processLambda(Lambda lambda) {
        super.visitMethodBase(lambda);
    }
    
    public void processMethodBase(IMethodBase method) {
        super.visitMethodBase(method);
    }
    
    public void processBlock(Block block) {
        super.visitBlock(block);
    }
    
    public void processVariable(IVariable variable) {
        super.visitVariable(variable);
    }
    
    public void processStatement(Statement stmt) {
        super.visitStatement(stmt);
    }
    
    public void processExpression(Expression expr) {
        super.visitExpression(expr);
    }
    
    public void addPlugin(InferTypesPlugin plugin) {
        plugin.main = this;
        plugin.errorMan = this.errorMan;
        this.plugins.add(plugin);
    }
    
    protected IVariableWithInitializer visitVariableWithInitializer(IVariableWithInitializer variable) {
        if (variable.getType() != null && variable.getInitializer() != null)
            variable.getInitializer().setExpectedType(variable.getType());
        
        super.visitVariableWithInitializer(variable);
        
        if (variable.getType() == null && variable.getInitializer() != null)
            variable.setType(variable.getInitializer().getType());
        
        return null;
    }
    
    protected Expression runTransformRound(Expression expr) {
        if (expr.actualType != null)
            return null;
        
        this.errorMan.currentNode = expr;
        
        var transformers = this.plugins.stream().filter(x -> x.canTransform(expr)).toArray(InferTypesPlugin[]::new);
        if (transformers.length > 1)
            this.errorMan.throw_("Multiple transformers found: " + Arrays.stream(Arrays.stream(transformers).map(x -> x.name).toArray(String[]::new)).collect(Collectors.joining(", ")));
        if (transformers.length != 1)
            return null;
        
        var plugin = transformers[0];
        this.contextInfoIdx++;
        this.errorMan.lastContextInfo = "[" + this.contextInfoIdx + "] running transform plugin \"" + plugin.name + "\"";
        try {
            var newExpr = plugin.transform(expr);
            // expression changed, restart the type infering process on the new expression
            if (newExpr != null)
                newExpr.parentNode = expr.parentNode;
            return newExpr;
        } catch (Exception e)  {
            this.errorMan.currentNode = expr;
            this.errorMan.throw_("Error while running type transformation phase: " + e);
            return null;
        }
    }
    
    protected Boolean detectType(Expression expr) {
        for (var plugin : this.plugins) {
            if (!plugin.canDetectType(expr))
                continue;
            this.contextInfoIdx++;
            this.errorMan.lastContextInfo = "[" + this.contextInfoIdx + "] running type detection plugin \"" + plugin.name + "\"";
            this.errorMan.currentNode = expr;
            try {
                if (plugin.detectType(expr))
                    return true;
            } catch (Exception e)  {
                this.errorMan.throw_("Error while running type detection phase: " + e);
            }
        }
        return false;
    }
    
    protected Expression visitExpression(Expression expr) {
        Expression transformedExpr = null;
        while (true) {
            var newExpr = this.runTransformRound(transformedExpr != null ? transformedExpr : expr);
            if (newExpr == null || newExpr == transformedExpr)
                break;
            transformedExpr = newExpr;
        }
        // if the plugin did not handle the expression, we use the default visit method
        var expr2 = transformedExpr != null ? transformedExpr : super.visitExpression(expr) != null ? super.visitExpression(expr) : expr;
        
        if (expr2.actualType != null)
            return expr2;
        
        var detectSuccess = this.detectType(expr2);
        
        if (expr2.actualType == null) {
            if (detectSuccess)
                this.errorMan.throw_("Type detection failed, although plugin tried to handle it");
            else
                this.errorMan.throw_("Type detection failed: none of the plugins could resolve the type");
        }
        
        return expr2;
    }
    
    protected Statement visitStatement(Statement stmt) {
        this.currentStatement = stmt;
        
        for (var plugin : this.plugins) {
            if (plugin.handleStatement(stmt))
                return null;
        }
        
        return super.visitStatement(stmt);
    }
    
    protected void visitField(Field field) {
        if (this.stage != InferTypesStage.Fields)
            return;
        super.visitField(field);
    }
    
    protected void visitProperty(Property prop) {
        if (this.stage != InferTypesStage.Properties)
            return;
        
        for (var plugin : this.plugins) {
            if (plugin.handleProperty(prop))
                return;
        }
        
        super.visitProperty(prop);
    }
    
    protected void visitMethodBase(IMethodBase method) {
        if (this.stage != InferTypesStage.Methods)
            return;
        
        for (var plugin : this.plugins) {
            if (plugin.handleMethod(method))
                return;
        }
        
        super.visitMethodBase(method);
    }
    
    protected Lambda visitLambda(Lambda lambda) {
        if (lambda.actualType != null)
            return null;
        
        for (var plugin : this.plugins) {
            if (plugin.handleLambda(lambda))
                return lambda;
        }
        
        return super.visitLambda(lambda);
    }
    
    public Expression runPluginsOn(Expression expr) {
        return this.visitExpression(expr);
    }
    
    protected void visitClass(Class cls) {
        if (cls.getAttributes().get("external").equals("true"))
            return;
        super.visitClass(cls);
    }
    
    public void visitPackage(Package pkg) {
        for (var stage : new ArrayList<>(List.of(InferTypesStage.Fields, InferTypesStage.Properties, InferTypesStage.Methods))) {
            this.stage = stage;
            super.visitPackage(pkg);
        }
    }
}