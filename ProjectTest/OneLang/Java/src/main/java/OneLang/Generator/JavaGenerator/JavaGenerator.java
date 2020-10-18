import java.util.Set;
import java.util.List;
import java.util.HashSet;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.regex.Pattern;

public class JavaGenerator implements IGenerator {
    public Set<String> imports;
    public IInterface currentClass;
    public String[] reservedWords;
    public String[] fieldToMethodHack;
    public List<IGeneratorPlugin> plugins;
    
    public JavaGenerator()
    {
        this.imports = new HashSet<String>();
        this.reservedWords = new String[] { "class", "interface", "throws", "package", "throw", "boolean" };
        this.fieldToMethodHack = new String[0];
        this.plugins = new ArrayList<IGeneratorPlugin>();
        this.plugins.add(new JsToJava(this));
    }
    
    public String getLangName()
    {
        return "Java";
    }
    
    public String getExtension()
    {
        return "java";
    }
    
    public String name_(String name)
    {
        if (Arrays.stream(this.reservedWords).anyMatch(name::equals))
            name += "_";
        if (Arrays.stream(this.fieldToMethodHack).anyMatch(name::equals))
            name += "()";
        var nameParts = name.split("-");
        for (Integer i = 1; i < nameParts.length; i++)
            nameParts[i] = nameParts[i].substring(0, 0 + 1).toUpperCase() + nameParts[i].substring(1);
        name = Arrays.stream(nameParts).collect(Collectors.joining(""));
        if (name == "_")
            name = "unused";
        return name;
    }
    
    public String leading(Statement item)
    {
        var result = "";
        if (item.getLeadingTrivia() != null && item.getLeadingTrivia().length() > 0)
            result += item.getLeadingTrivia();
        //if (item.attributes !== null)
        //    result += Object.keys(item.attributes).map(x => `// @${x} ${item.attributes[x]}\n`).join("");
        return result;
    }
    
    public String preArr(String prefix, String[] value)
    {
        return value.length > 0 ? prefix + Arrays.stream(value).collect(Collectors.joining(", ")) : "";
    }
    
    public String preIf(String prefix, Boolean condition)
    {
        return condition ? prefix : "";
    }
    
    public String pre(String prefix, String value)
    {
        return value != null ? prefix + value : "";
    }
    
    public String typeArgs(String[] args)
    {
        return args != null && args.length > 0 ? "<" + Arrays.stream(args).collect(Collectors.joining(", ")) + ">" : "";
    }
    
    public String typeArgs2(IType[] args)
    {
        return this.typeArgs(Arrays.stream(args).map(x -> this.type(x)).toArray(String[]::new));
    }
    
    public String type(IType t, Boolean mutates, Boolean isNew)
    {
        if (t instanceof ClassType) {
            var typeArgs = this.typeArgs(Arrays.stream(((ClassType)t).getTypeArguments()).map(x -> this.type(x)).toArray(String[]::new));
            if (((ClassType)t).decl.getName() == "TsString")
                return "String";
            else if (((ClassType)t).decl.getName() == "TsBoolean")
                return "Boolean";
            else if (((ClassType)t).decl.getName() == "TsNumber")
                return "Integer";
            else if (((ClassType)t).decl.getName() == "TsArray") {
                var realType = isNew ? "ArrayList" : "List";
                if (mutates) {
                    this.imports.add("java.util." + realType);
                    return realType + "<" + this.type(((ClassType)t).getTypeArguments()[0]) + ">";
                }
                else
                    return this.type(((ClassType)t).getTypeArguments()[0]) + "[]";
            }
            else if (((ClassType)t).decl.getName() == "Map") {
                var realType = isNew ? "HashMap" : "Map";
                this.imports.add("java.util." + realType);
                return realType + "<" + this.type(((ClassType)t).getTypeArguments()[0]) + ", " + this.type(((ClassType)t).getTypeArguments()[1]) + ">";
            }
            else if (((ClassType)t).decl.getName() == "Set") {
                var realType = isNew ? "HashSet" : "Set";
                this.imports.add("java.util." + realType);
                return realType + "<" + this.type(((ClassType)t).getTypeArguments()[0]) + ">";
            }
            else if (((ClassType)t).decl.getName() == "Promise")
                return ((ClassType)t).getTypeArguments()[0] instanceof VoidType ? "void" : this.type(((ClassType)t).getTypeArguments()[0]);
            else if (((ClassType)t).decl.getName() == "Object")
                //this.imports.add("System");
                return "Object";
            else if (((ClassType)t).decl.getName() == "TsMap") {
                var realType = isNew ? "HashMap" : "Map";
                this.imports.add("java.util." + realType);
                return realType + "<String, " + this.type(((ClassType)t).getTypeArguments()[0]) + ">";
            }
            return this.name_(((ClassType)t).decl.getName()) + typeArgs;
        }
        else if (t instanceof InterfaceType)
            return this.name_(((InterfaceType)t).decl.getName()) + this.typeArgs(Arrays.stream(((InterfaceType)t).getTypeArguments()).map(x -> this.type(x)).toArray(String[]::new));
        else if (t instanceof VoidType)
            return "void";
        else if (t instanceof EnumType)
            return this.name_(((EnumType)t).decl.getName());
        else if (t instanceof AnyType)
            return "Object";
        else if (t instanceof NullType)
            return "null";
        else if (t instanceof GenericsType)
            return ((GenericsType)t).typeVarName;
        else if (t instanceof LambdaType) {
            var isFunc = !(((LambdaType)t).returnType instanceof VoidType);
            var paramTypes = Arrays.asList(Arrays.stream(((LambdaType)t).parameters).map(x -> this.type(x.getType())).toArray(String[]::new));
            if (isFunc)
                paramTypes.add(this.type(((LambdaType)t).returnType));
            this.imports.add("java.util.function." + (isFunc ? "Function" : "Consumer"));
            return (isFunc ? "Function" : "Consumer") + "<" + paramTypes.stream().collect(Collectors.joining(", ")) + ">";
        }
        else if (t == null)
            return "/* TODO */ object";
        else
            return "/* MISSING */";
    }
    
    public String type(IType t, Boolean mutates) {
        return this.type(t, mutates, false);
    }
    
    public String type(IType t) {
        return this.type(t, true, false);
    }
    
    public Boolean isTsArray(IType type)
    {
        return type instanceof ClassType && ((ClassType)type).decl.getName() == "TsArray";
    }
    
    public String vis(Visibility v)
    {
        return v == Visibility.Private ? "private" : v == Visibility.Protected ? "protected" : v == Visibility.Public ? "public" : "/* TODO: not set */public";
    }
    
    public String varType(IVariable v, IHasAttributesAndTrivia attr)
    {
        String type;
        if (attr != null && attr.getAttributes() != null && attr.getAttributes().containsKey("java-type"))
            type = attr.getAttributes().get("java-type");
        else if (v.getType() instanceof ClassType && ((ClassType)v.getType()).decl.getName() == "TsArray") {
            if (v.getMutability().mutated) {
                this.imports.add("java.util.List");
                type = "List<" + this.type(((ClassType)v.getType()).getTypeArguments()[0]) + ">";
            }
            else
                type = this.type(((ClassType)v.getType()).getTypeArguments()[0]) + "[]";
        }
        else
            type = this.type(v.getType());
        return type;
    }
    
    public String varWoInit(IVariable v, IHasAttributesAndTrivia attr)
    {
        return this.varType(v, attr) + " " + this.name_(v.getName());
    }
    
    public String var(IVariableWithInitializer v, IHasAttributesAndTrivia attrs)
    {
        return this.varWoInit(v, attrs) + (v.getInitializer() != null ? " = " + this.expr(v.getInitializer()) : "");
    }
    
    public String exprCall(IType[] typeArgs, Expression[] args)
    {
        return this.typeArgs2(typeArgs) + "(" + Arrays.stream(Arrays.stream(args).map(x -> this.expr(x)).toArray(String[]::new)).collect(Collectors.joining(", ")) + ")";
    }
    
    public String mutateArg(Expression arg, Boolean shouldBeMutable)
    {
        if (this.isTsArray(arg.actualType)) {
            var itemType = (((ClassType)arg.actualType)).getTypeArguments()[0];
            if (arg instanceof ArrayLiteral && !shouldBeMutable)
                return ((ArrayLiteral)arg).items.length == 0 && !this.isTsArray(itemType) ? "new " + this.type(itemType) + "[0]" : "new " + this.type(itemType) + "[] { " + Arrays.stream(Arrays.stream(((ArrayLiteral)arg).items).map(x -> this.expr(x)).toArray(String[]::new)).collect(Collectors.joining(", ")) + " }";
            
            var currentlyMutable = shouldBeMutable;
            if (arg instanceof VariableReference)
                currentlyMutable = ((VariableReference)arg).getVariable().getMutability().mutated;
            else if (arg instanceof InstanceMethodCallExpression || arg instanceof StaticMethodCallExpression)
                currentlyMutable = false;
            
            if (currentlyMutable && !shouldBeMutable)
                return this.expr(arg) + ".toArray(" + this.type(itemType) + "[]::new)";
            else if (!currentlyMutable && shouldBeMutable) {
                this.imports.add("java.util.Arrays");
                return "Arrays.asList(" + this.expr(arg) + ")";
            }
        }
        return this.expr(arg);
    }
    
    public String mutatedExpr(Expression expr, Expression toWhere)
    {
        if (toWhere instanceof VariableReference) {
            var v = ((VariableReference)toWhere).getVariable();
            if (this.isTsArray(v.getType()))
                return this.mutateArg(expr, v.getMutability().mutated);
        }
        return this.expr(expr);
    }
    
    public String callParams(Expression[] args, MethodParameter[] params)
    {
        var argReprs = new ArrayList<String>();
        for (Integer i = 0; i < args.length; i++)
            argReprs.add(this.isTsArray(params[i].getType()) ? this.mutateArg(args[i], params[i].getMutability().mutated) : this.expr(args[i]));
        return "(" + argReprs.stream().collect(Collectors.joining(", ")) + ")";
    }
    
    public String methodCall(IMethodCallExpression expr)
    {
        return this.name_(expr.getMethod().name) + this.typeArgs2(expr.getTypeArgs()) + this.callParams(expr.getArgs(), expr.getMethod().getParameters());
    }
    
    public String inferExprNameForType(IType type)
    {
        if (type instanceof ClassType && StdArrayHelper.allMatch(((ClassType)type).getTypeArguments(), (x, unused) -> x instanceof ClassType)) {
            var fullName = Arrays.stream(Arrays.stream(((ClassType)type).getTypeArguments()).map(x -> (((ClassType)x)).decl.getName()).toArray(String[]::new)).collect(Collectors.joining("")) + ((ClassType)type).decl.getName();
            return NameUtils.shortName(fullName);
        }
        return null;
    }
    
    public Boolean isSetExpr(VariableReference varRef)
    {
        return varRef.parentNode instanceof BinaryExpression && ((BinaryExpression)varRef.parentNode).left == varRef && List.of("=", "+=", "-=").stream().anyMatch(((BinaryExpression)varRef.parentNode).operator::equals);
    }
    
    public String expr(IExpression expr)
    {
        for (var plugin : this.plugins) {
            var result = plugin.expr(expr);
            if (result != null)
                return result;
        }
        
        var res = "UNKNOWN-EXPR";
        if (expr instanceof NewExpression)
            res = "new " + this.type(((NewExpression)expr).cls, true, true) + this.callParams(((NewExpression)expr).args, ((NewExpression)expr).cls.decl.constructor_ != null ? ((NewExpression)expr).cls.decl.constructor_.getParameters() : new MethodParameter[0]);
        else if (expr instanceof UnresolvedNewExpression)
            res = "/* TODO: UnresolvedNewExpression */ new " + this.type(((UnresolvedNewExpression)expr).cls) + "(" + Arrays.stream(Arrays.stream(((UnresolvedNewExpression)expr).args).map(x -> this.expr(x)).toArray(String[]::new)).collect(Collectors.joining(", ")) + ")";
        else if (expr instanceof Identifier)
            res = "/* TODO: Identifier */ " + ((Identifier)expr).text;
        else if (expr instanceof PropertyAccessExpression)
            res = "/* TODO: PropertyAccessExpression */ " + this.expr(((PropertyAccessExpression)expr).object) + "." + ((PropertyAccessExpression)expr).propertyName;
        else if (expr instanceof UnresolvedCallExpression)
            res = "/* TODO: UnresolvedCallExpression */ " + this.expr(((UnresolvedCallExpression)expr).func) + this.exprCall(((UnresolvedCallExpression)expr).typeArgs, ((UnresolvedCallExpression)expr).args);
        else if (expr instanceof UnresolvedMethodCallExpression)
            res = "/* TODO: UnresolvedMethodCallExpression */ " + this.expr(((UnresolvedMethodCallExpression)expr).object) + "." + ((UnresolvedMethodCallExpression)expr).methodName + this.exprCall(((UnresolvedMethodCallExpression)expr).typeArgs, ((UnresolvedMethodCallExpression)expr).args);
        else if (expr instanceof InstanceMethodCallExpression)
            res = this.expr(((InstanceMethodCallExpression)expr).object) + "." + this.methodCall(((InstanceMethodCallExpression)expr));
        else if (expr instanceof StaticMethodCallExpression)
            res = this.name_(((StaticMethodCallExpression)expr).getMethod().parentInterface.getName()) + "." + this.methodCall(((StaticMethodCallExpression)expr));
        else if (expr instanceof GlobalFunctionCallExpression)
            res = "Global." + this.name_(((GlobalFunctionCallExpression)expr).func.getName()) + this.exprCall(new IType[0], ((GlobalFunctionCallExpression)expr).args);
        else if (expr instanceof LambdaCallExpression)
            res = this.expr(((LambdaCallExpression)expr).method) + ".apply(" + Arrays.stream(Arrays.stream(((LambdaCallExpression)expr).args).map(x -> this.expr(x)).toArray(String[]::new)).collect(Collectors.joining(", ")) + ")";
        else if (expr instanceof BooleanLiteral)
            res = (((BooleanLiteral)expr).boolValue ? "true" : "false");
        else if (expr instanceof StringLiteral)
            res = JSON.stringify(((StringLiteral)expr).stringValue);
        else if (expr instanceof NumericLiteral)
            res = ((NumericLiteral)expr).valueAsText;
        else if (expr instanceof CharacterLiteral)
            res = "'" + ((CharacterLiteral)expr).charValue + "'";
        else if (expr instanceof ElementAccessExpression)
            res = this.expr(((ElementAccessExpression)expr).object) + ".get(" + this.expr(((ElementAccessExpression)expr).elementExpr) + ")";
        else if (expr instanceof TemplateString) {
            var parts = new ArrayList<String>();
            for (var part : ((TemplateString)expr).parts) {
                if (part.isLiteral) {
                    var lit = "";
                    for (Integer i = 0; i < part.literalText.length(); i++) {
                        var chr = part.literalText.substring(i, i + 1);
                        if (chr == "\n")
                            lit += "\\n";
                        else if (chr == "\r")
                            lit += "\\r";
                        else if (chr == "\t")
                            lit += "\\t";
                        else if (chr == "\\")
                            lit += "\\\\";
                        else if (chr == "\"")
                            lit += "\\\"";
                        else {
                            var chrCode = (int)chr.charAt(0);
                            if (32 <= chrCode && chrCode <= 126)
                                lit += chr;
                            else
                                throw new Error("invalid char in template string (code=" + chrCode + ")");
                        }
                    }
                    parts.add("\"" + lit + "\"");
                }
                else {
                    var repr = this.expr(part.expression);
                    parts.add(part.expression instanceof ConditionalExpression ? "(" + repr + ")" : repr);
                }
            }
            res = parts.stream().collect(Collectors.joining(" + "));
        }
        else if (expr instanceof BinaryExpression) {
            var modifies = List.of("=", "+=", "-=").stream().anyMatch(((BinaryExpression)expr).operator::equals);
            if (modifies && ((BinaryExpression)expr).left instanceof InstanceFieldReference && this.useGetterSetter(((InstanceFieldReference)((BinaryExpression)expr).left)))
                res = this.expr(((InstanceFieldReference)((BinaryExpression)expr).left).object) + ".set" + this.ucFirst(((InstanceFieldReference)((BinaryExpression)expr).left).field.getName()) + "(" + this.mutatedExpr(((BinaryExpression)expr).right, ((BinaryExpression)expr).operator == "=" ? ((InstanceFieldReference)((BinaryExpression)expr).left) : null) + ")";
            else
                res = this.expr(((BinaryExpression)expr).left) + " " + ((BinaryExpression)expr).operator + " " + this.mutatedExpr(((BinaryExpression)expr).right, ((BinaryExpression)expr).operator == "=" ? ((BinaryExpression)expr).left : null);
        }
        else if (expr instanceof ArrayLiteral) {
            if (((ArrayLiteral)expr).items.length == 0)
                res = "new " + this.type(((ArrayLiteral)expr).actualType, true, true) + "()";
            else {
                this.imports.add("java.util.List");
                res = "List.of(" + Arrays.stream(Arrays.stream(((ArrayLiteral)expr).items).map(x -> this.expr(x)).toArray(String[]::new)).collect(Collectors.joining(", ")) + ")";
            }
        }
        else if (expr instanceof CastExpression)
            res = "((" + this.type(((CastExpression)expr).newType) + ")" + this.expr(((CastExpression)expr).expression) + ")";
        else if (expr instanceof ConditionalExpression)
            res = this.expr(((ConditionalExpression)expr).condition) + " ? " + this.expr(((ConditionalExpression)expr).whenTrue) + " : " + this.mutatedExpr(((ConditionalExpression)expr).whenFalse, ((ConditionalExpression)expr).whenTrue);
        else if (expr instanceof InstanceOfExpression)
            res = this.expr(((InstanceOfExpression)expr).expr) + " instanceof " + this.type(((InstanceOfExpression)expr).checkType);
        else if (expr instanceof ParenthesizedExpression)
            res = "(" + this.expr(((ParenthesizedExpression)expr).expression) + ")";
        else if (expr instanceof RegexLiteral)
            res = "new RegExp(" + JSON.stringify(((RegexLiteral)expr).pattern) + ")";
        else if (expr instanceof Lambda) {
            String body;
            if (((Lambda)expr).getBody().statements.size() == 1 && ((Lambda)expr).getBody().statements.get(0) instanceof ReturnStatement)
                body = this.expr((((ReturnStatement)((Lambda)expr).getBody().statements.get(0))).expression);
            else
                body = "{ " + this.rawBlock(((Lambda)expr).getBody()) + " }";
            
            var params = Arrays.stream(((Lambda)expr).getParameters()).map(x -> this.name_(x.getName())).toArray(String[]::new);
            
            res = (params.length == 1 ? params[0] : "(" + Arrays.stream(params).collect(Collectors.joining(", ")) + ")") + " -> " + body;
        }
        else if (expr instanceof UnaryExpression && ((UnaryExpression)expr).unaryType == UnaryType.Prefix)
            res = ((UnaryExpression)expr).operator + this.expr(((UnaryExpression)expr).operand);
        else if (expr instanceof UnaryExpression && ((UnaryExpression)expr).unaryType == UnaryType.Postfix)
            res = this.expr(((UnaryExpression)expr).operand) + ((UnaryExpression)expr).operator;
        else if (expr instanceof MapLiteral) {
            if (((MapLiteral)expr).items.length > 10)
                throw new Error("MapLiteral is only supported with maximum of 10 items");
            if (((MapLiteral)expr).items.length == 0)
                res = "new " + this.type(((MapLiteral)expr).actualType, true, true) + "()";
            else {
                this.imports.add("java.util.Map");
                var repr = Arrays.stream(Arrays.stream(((MapLiteral)expr).items).map(item -> JSON.stringify(item.key) + ", " + this.expr(item.value)).toArray(String[]::new)).collect(Collectors.joining(", "));
                res = "Map.of(" + repr + ")";
            }
        }
        else if (expr instanceof NullLiteral)
            res = "null";
        else if (expr instanceof AwaitExpression)
            res = this.expr(((AwaitExpression)expr).expr);
        else if (expr instanceof ThisReference)
            res = "this";
        else if (expr instanceof StaticThisReference)
            res = this.currentClass.getName();
        else if (expr instanceof EnumReference)
            res = this.name_(((EnumReference)expr).decl.getName());
        else if (expr instanceof ClassReference)
            res = this.name_(((ClassReference)expr).decl.getName());
        else if (expr instanceof MethodParameterReference)
            res = this.name_(((MethodParameterReference)expr).decl.getName());
        else if (expr instanceof VariableDeclarationReference)
            res = this.name_(((VariableDeclarationReference)expr).decl.getName());
        else if (expr instanceof ForVariableReference)
            res = this.name_(((ForVariableReference)expr).decl.getName());
        else if (expr instanceof ForeachVariableReference)
            res = this.name_(((ForeachVariableReference)expr).decl.getName());
        else if (expr instanceof CatchVariableReference)
            res = this.name_(((CatchVariableReference)expr).decl.getName());
        else if (expr instanceof GlobalFunctionReference)
            res = this.name_(((GlobalFunctionReference)expr).decl.getName());
        else if (expr instanceof SuperReference)
            res = "super";
        else if (expr instanceof StaticFieldReference)
            res = this.name_(((StaticFieldReference)expr).decl.parentInterface.getName()) + "." + this.name_(((StaticFieldReference)expr).decl.getName());
        else if (expr instanceof StaticPropertyReference)
            res = this.name_(((StaticPropertyReference)expr).decl.parentClass.getName()) + "." + this.name_(((StaticPropertyReference)expr).decl.getName());
        else if (expr instanceof InstanceFieldReference) {
            // TODO: unified handling of field -> property conversion?
            if (this.useGetterSetter(((InstanceFieldReference)expr)))
                res = this.expr(((InstanceFieldReference)expr).object) + ".get" + this.ucFirst(((InstanceFieldReference)expr).field.getName()) + "()";
            else
                res = this.expr(((InstanceFieldReference)expr).object) + "." + this.name_(((InstanceFieldReference)expr).field.getName());
        }
        else if (expr instanceof InstancePropertyReference)
            res = this.expr(((InstancePropertyReference)expr).object) + "." + (this.isSetExpr(((InstancePropertyReference)expr)) ? "set" : "get") + this.ucFirst(((InstancePropertyReference)expr).property.getName()) + "()";
        else if (expr instanceof EnumMemberReference)
            res = this.name_(((EnumMemberReference)expr).decl.parentEnum.getName()) + "." + this.name_(((EnumMemberReference)expr).decl.name);
        else if (expr instanceof NullCoalesceExpression)
            res = this.expr(((NullCoalesceExpression)expr).defaultExpr) + " != null ? " + this.expr(((NullCoalesceExpression)expr).defaultExpr) + " : " + this.mutatedExpr(((NullCoalesceExpression)expr).exprIfNull, ((NullCoalesceExpression)expr).defaultExpr);
        else { }
        return res;
    }
    
    public Boolean useGetterSetter(InstanceFieldReference fieldRef)
    {
        return fieldRef.object.actualType instanceof InterfaceType || (fieldRef.field.interfaceDeclarations != null && fieldRef.field.interfaceDeclarations.length > 0);
    }
    
    public String block(Block block, Boolean allowOneLiner)
    {
        var stmtLen = block.statements.size();
        return stmtLen == 0 ? " { }" : allowOneLiner && stmtLen == 1 && !(block.statements.get(0) instanceof IfStatement) && !(block.statements.get(0) instanceof VariableDeclaration) ? "\n" + this.pad(this.rawBlock(block)) : " {\n" + this.pad(this.rawBlock(block)) + "\n}";
    }
    
    public String block(Block block) {
        return this.block(block, true);
    }
    
    public String stmtDefault(Statement stmt)
    {
        var res = "UNKNOWN-STATEMENT";
        if (stmt instanceof BreakStatement)
            res = "break;";
        else if (stmt instanceof ReturnStatement)
            res = ((ReturnStatement)stmt).expression == null ? "return;" : "return " + this.mutateArg(((ReturnStatement)stmt).expression, false) + ";";
        else if (stmt instanceof UnsetStatement)
            res = "/* unset " + this.expr(((UnsetStatement)stmt).expression) + "; */";
        else if (stmt instanceof ThrowStatement)
            res = "throw " + this.expr(((ThrowStatement)stmt).expression) + ";";
        else if (stmt instanceof ExpressionStatement)
            res = this.expr(((ExpressionStatement)stmt).expression) + ";";
        else if (stmt instanceof VariableDeclaration) {
            if (((VariableDeclaration)stmt).getInitializer() instanceof NullLiteral)
                res = this.type(((VariableDeclaration)stmt).getType(), ((VariableDeclaration)stmt).getMutability().mutated) + " " + this.name_(((VariableDeclaration)stmt).getName()) + " = null;";
            else if (((VariableDeclaration)stmt).getInitializer() != null)
                res = "var " + this.name_(((VariableDeclaration)stmt).getName()) + " = " + this.mutateArg(((VariableDeclaration)stmt).getInitializer(), ((VariableDeclaration)stmt).getMutability().mutated) + ";";
            else
                res = this.type(((VariableDeclaration)stmt).getType()) + " " + this.name_(((VariableDeclaration)stmt).getName()) + ";";
        }
        else if (stmt instanceof ForeachStatement)
            res = "for (var " + this.name_(((ForeachStatement)stmt).itemVar.getName()) + " : " + this.expr(((ForeachStatement)stmt).items) + ")" + this.block(((ForeachStatement)stmt).body);
        else if (stmt instanceof IfStatement) {
            var elseIf = ((IfStatement)stmt).else_ != null && ((IfStatement)stmt).else_.statements.size() == 1 && ((IfStatement)stmt).else_.statements.get(0) instanceof IfStatement;
            res = "if (" + this.expr(((IfStatement)stmt).condition) + ")" + this.block(((IfStatement)stmt).then);
            res += (elseIf ? "\nelse " + this.stmt(((IfStatement)stmt).else_.statements.get(0)) : "") + (!elseIf && ((IfStatement)stmt).else_ != null ? "\nelse" + this.block(((IfStatement)stmt).else_) : "");
        }
        else if (stmt instanceof WhileStatement)
            res = "while (" + this.expr(((WhileStatement)stmt).condition) + ")" + this.block(((WhileStatement)stmt).body);
        else if (stmt instanceof ForStatement)
            res = "for (" + (((ForStatement)stmt).itemVar != null ? this.var(((ForStatement)stmt).itemVar, null) : "") + "; " + this.expr(((ForStatement)stmt).condition) + "; " + this.expr(((ForStatement)stmt).incrementor) + ")" + this.block(((ForStatement)stmt).body);
        else if (stmt instanceof DoStatement)
            res = "do" + this.block(((DoStatement)stmt).body) + " while (" + this.expr(((DoStatement)stmt).condition) + ");";
        else if (stmt instanceof TryStatement) {
            res = "try" + this.block(((TryStatement)stmt).tryBody, false);
            if (((TryStatement)stmt).catchBody != null)
                //this.imports.add("System");
                res += " catch (Exception " + this.name_(((TryStatement)stmt).catchVar.getName()) + ") " + this.block(((TryStatement)stmt).catchBody, false);
            if (((TryStatement)stmt).finallyBody != null)
                res += "finally" + this.block(((TryStatement)stmt).finallyBody);
        }
        else if (stmt instanceof ContinueStatement)
            res = "continue;";
        else { }
        return res;
    }
    
    public String stmt(Statement stmt)
    {
        String res = null;
        
        if (stmt.getAttributes() != null && stmt.getAttributes().containsKey("java-import"))
            this.imports.add(stmt.getAttributes().get("java-import"));
        
        if (stmt.getAttributes() != null && stmt.getAttributes().containsKey("java"))
            res = stmt.getAttributes().get("java");
        else {
            for (var plugin : this.plugins) {
                res = plugin.stmt(stmt);
                if (res != null)
                    break;
            }
            
            if (res == null)
                res = this.stmtDefault(stmt);
        }
        
        return this.leading(stmt) + res;
    }
    
    public String stmts(Statement[] stmts)
    {
        return Arrays.stream(Arrays.stream(stmts).map(stmt -> this.stmt(stmt)).toArray(String[]::new)).collect(Collectors.joining("\n"));
    }
    
    public String rawBlock(Block block)
    {
        return this.stmts(block.statements.toArray(Statement[]::new));
    }
    
    public String overloadMethodGen(String prefix, Method method, MethodParameter[] params, String body)
    {
        var methods = new ArrayList<String>();
        methods.add(prefix + "(" + Arrays.stream(Arrays.stream(params).map(p -> this.varWoInit(p, null)).toArray(String[]::new)).collect(Collectors.joining(", ")) + ")" + body);
        
        for (Integer paramLen = params.length - 1; paramLen >= 0; paramLen--) {
            if (params[paramLen].getInitializer() == null)
                break;
            
            var methodParams = new ArrayList<String>();
            var methodArgs = new ArrayList<String>();
            for (Integer i = 0; i < params.length; i++) {
                var p = params[i];
                if (i < paramLen)
                    methodParams.add(this.varWoInit(p, null));
                methodArgs.add(i >= paramLen ? this.expr(p.getInitializer()) : p.getName());
            }
            
            var baseName = (method != null && method.getIsStatic() ? this.currentClass.getName() : "this") + (method != null ? "." + method.name : "");
            methods.add(prefix + "(" + methodParams.stream().collect(Collectors.joining(", ")) + ") {\n" + this.pad((method != null && !(method.returns instanceof VoidType) ? "return " : "") + baseName + "(" + methodArgs.stream().collect(Collectors.joining(", ")) + ");") + "\n}");
        }
        
        return methods.stream().collect(Collectors.joining("\n\n"));
    }
    
    public String method(Method method, Boolean isCls)
    {
        // TODO: final
        var prefix = (isCls ? this.vis(method.getVisibility()) + " " : "") + this.preIf("static ", method.getIsStatic()) + this.preIf("/* throws */ ", method.getThrows()) + (method.typeArguments.length > 0 ? "<" + Arrays.stream(method.typeArguments).collect(Collectors.joining(", ")) + "> " : "") + this.type(method.returns, false) + " " + this.name_(method.name);
        return this.overloadMethodGen(prefix, method, method.getParameters(), method.getBody() == null ? ";" : "\n{\n" + this.pad(this.stmts(method.getBody().statements.toArray(Statement[]::new))) + "\n}");
    }
    
    public String class_(Class cls)
    {
        this.currentClass = cls;
        var resList = new ArrayList<String>();
        
        var staticConstructorStmts = new ArrayList<Statement>();
        var complexFieldInits = new ArrayList<Statement>();
        var fieldReprs = new ArrayList<String>();
        var propReprs = new ArrayList<String>();
        for (var field : cls.getFields()) {
            var isInitializerComplex = field.getInitializer() != null && !(field.getInitializer() instanceof StringLiteral) && !(field.getInitializer() instanceof BooleanLiteral) && !(field.getInitializer() instanceof NumericLiteral);
            
            var prefix = this.vis(field.getVisibility()) + " " + this.preIf("static ", field.getIsStatic());
            if (field.interfaceDeclarations.length > 0) {
                var varType = this.varType(field, field);
                var name = this.name_(field.getName());
                var pname = this.ucFirst(field.getName());
                propReprs.add(varType + " " + name + ";\n" + prefix + varType + " get" + pname + "() { return this." + name + "; }\n" + prefix + "void set" + pname + "(" + varType + " value) { this." + name + " = value; }");
            }
            else if (isInitializerComplex) {
                if (field.getIsStatic())
                    staticConstructorStmts.add(new ExpressionStatement(new BinaryExpression(new StaticFieldReference(field), "=", field.getInitializer())));
                else
                    complexFieldInits.add(new ExpressionStatement(new BinaryExpression(new InstanceFieldReference(new ThisReference(cls), field), "=", field.getInitializer())));
                
                fieldReprs.add(prefix + this.varWoInit(field, field) + ";");
            }
            else
                fieldReprs.add(prefix + this.var(field, field) + ";");
        }
        resList.add(fieldReprs.stream().collect(Collectors.joining("\n")));
        resList.add(propReprs.stream().collect(Collectors.joining("\n\n")));
        
        for (var prop : cls.properties) {
            var prefix = this.vis(prop.getVisibility()) + " " + this.preIf("static ", prop.getIsStatic());
            if (prop.getter != null)
                resList.add(prefix + this.type(prop.getType()) + " get" + this.ucFirst(prop.getName()) + "()" + this.block(prop.getter, false));
            
            if (prop.setter != null)
                resList.add(prefix + "void set" + this.ucFirst(prop.getName()) + "(" + this.type(prop.getType()) + " value)" + this.block(prop.setter, false));
        }
        
        if (staticConstructorStmts.size() > 0)
            resList.add("static {\n" + this.pad(this.stmts(staticConstructorStmts.toArray(Statement[]::new))) + "\n}");
        
        if (cls.constructor_ != null) {
            var constrFieldInits = new ArrayList<Statement>();
            for (var field : Arrays.stream(cls.getFields()).filter(x -> x.constructorParam != null).toArray(Field[]::new)) {
                var fieldRef = new InstanceFieldReference(new ThisReference(cls), field);
                var mpRef = new MethodParameterReference(field.constructorParam);
                // TODO: decide what to do with "after-TypeEngine" transformations
                mpRef.setActualType(field.getType(), false, false);
                constrFieldInits.add(new ExpressionStatement(new BinaryExpression(fieldRef, "=", mpRef)));
            }
            
            var superCall = cls.constructor_.superCallArgs != null ? "super(" + Arrays.stream(Arrays.stream(cls.constructor_.superCallArgs).map(x -> this.expr(x)).toArray(String[]::new)).collect(Collectors.joining(", ")) + ");\n" : "";
            
            // TODO: super calls
            resList.add(this.overloadMethodGen("public " + this.preIf("/* throws */ ", cls.constructor_.getThrows()) + this.name_(cls.getName()), null, cls.constructor_.getParameters(), "\n{\n" + this.pad(superCall + this.stmts(Stream.of(Stream.of(constrFieldInits, complexFieldInits).flatMap(Stream::of).toArray(Statement[]::new), cls.constructor_.getBody().statements).flatMap(Stream::of).toArray(Statement[]::new))) + "\n}"));
        }
        else if (complexFieldInits.size() > 0)
            resList.add("public " + this.name_(cls.getName()) + "()\n{\n" + this.pad(this.stmts(complexFieldInits.toArray(Statement[]::new))) + "\n}");
        
        var methods = new ArrayList<String>();
        for (var method : cls.getMethods()) {
            if (method.getBody() == null)
                continue;
            // declaration only
            methods.add(this.method(method, true));
        }
        resList.add(methods.stream().collect(Collectors.joining("\n\n")));
        return this.pad(Arrays.stream(resList.stream().filter(x -> x != "").toArray(String[]::new)).collect(Collectors.joining("\n\n")));
    }
    
    public String ucFirst(String str)
    {
        return str.substring(0, 0 + 1).toUpperCase() + str.substring(1);
    }
    
    public String interface_(Interface intf)
    {
        this.currentClass = intf;
        
        var resList = new ArrayList<String>();
        for (var field : intf.getFields()) {
            var varType = this.varType(field, field);
            var name = this.ucFirst(field.getName());
            resList.add(varType + " get" + name + "();\nvoid set" + name + "(" + varType + " value);");
        }
        
        resList.add(Arrays.stream(Arrays.stream(intf.getMethods()).map(method -> this.method(method, false)).toArray(String[]::new)).collect(Collectors.joining("\n")));
        return this.pad(Arrays.stream(resList.stream().filter(x -> x != "").toArray(String[]::new)).collect(Collectors.joining("\n\n")));
    }
    
    public String pad(String str)
    {
        return Arrays.stream(Arrays.stream(str.split("\\n")).map(x -> "    " + x).toArray(String[]::new)).collect(Collectors.joining("\n"));
    }
    
    public String pathToNs(String path)
    {
        // Generator/ExprLang/ExprLangAst.ts -> Generator.ExprLang
        var parts = Arrays.asList(path.split("/"));
        parts.remove(parts.size() - 1);
        return parts.stream().collect(Collectors.joining("."));
    }
    
    public String importsHead()
    {
        var imports = new ArrayList<String>();
        for (var imp : this.imports.toArray(String[]::new))
            imports.add(imp);
        this.imports = new HashSet<String>();
        return imports.size() == 0 ? "" : Arrays.stream(imports.stream().map(x -> "import " + x + ";").toArray(String[]::new)).collect(Collectors.joining("\n")) + "\n\n";
    }
    
    public GeneratedFile[] generate(Package pkg)
    {
        var result = new ArrayList<GeneratedFile>();
        for (var path : pkg.files.keySet().toArray(String[]::new)) {
            var file = pkg.files.get(path);
            var dstDir = "src/main/java/" + pkg.name + "/" + file.sourcePath.path.replaceAll(Pattern.quote("\\.ts$"), "");
            
            for (var enum_ : file.enums)
                result.add(new GeneratedFile(dstDir + "/" + enum_.getName() + ".java", "public enum " + this.name_(enum_.getName()) + " { " + Arrays.stream(Arrays.stream(enum_.values).map(x -> this.name_(x.name)).toArray(String[]::new)).collect(Collectors.joining(", ")) + " }"));
            
            for (var intf : file.interfaces) {
                var res = "public interface " + this.name_(intf.getName()) + this.typeArgs(intf.getTypeArguments()) + this.preArr(" extends ", Arrays.stream(intf.getBaseInterfaces()).map(x -> this.type(x)).toArray(String[]::new)) + " {\n" + this.interface_(intf) + "\n}";
                result.add(new GeneratedFile(dstDir + "/" + intf.getName() + ".java", this.importsHead() + res));
            }
            
            for (var cls : file.classes) {
                var res = "public class " + this.name_(cls.getName()) + this.typeArgs(cls.getTypeArguments()) + (cls.baseClass != null ? " extends " + this.type(cls.baseClass) : "") + this.preArr(" implements ", Arrays.stream(cls.getBaseInterfaces()).map(x -> this.type(x)).toArray(String[]::new)) + " {\n" + this.class_(cls) + "\n}";
                result.add(new GeneratedFile(dstDir + "/" + cls.getName() + ".java", this.importsHead() + res));
            }
        }
        return result.toArray(GeneratedFile[]::new);
    }
}