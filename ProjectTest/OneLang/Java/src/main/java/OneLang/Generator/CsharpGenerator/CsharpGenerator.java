import java.util.Set;
import java.util.Map;
import java.util.HashMap;
import java.util.Arrays;
import java.util.stream.Collectors;
import java.util.ArrayList;
import java.util.stream.Stream;
import java.util.HashSet;
import java.util.List;

public class CsharpGenerator implements IGenerator {
    public Set<String> usings;
    public IInterface currentClass;
    public String[] reservedWords;
    public String[] fieldToMethodHack;
    public Map<String, Integer> instanceOfIds;
    
    public CsharpGenerator()
    {
        this.reservedWords = new String[] { "object", "else", "operator", "class", "enum", "void", "string", "implicit", "Type", "Enum", "params", "using", "throw", "ref", "base", "virtual", "interface", "int", "const" };
        this.fieldToMethodHack = new String[] { "length" };
        this.instanceOfIds = new HashMap<String, Integer>();
    }
    
    public String getLangName()
    {
        return "CSharp";
    }
    
    public String getExtension()
    {
        return "cs";
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
    
    public String type(IType t, Boolean mutates)
    {
        if (t instanceof ClassType) {
            var typeArgs = this.typeArgs(Arrays.stream(((ClassType)t).getTypeArguments()).map(x -> this.type(x)).toArray(String[]::new));
            if (((ClassType)t).decl.getName() == "TsString")
                return "string";
            else if (((ClassType)t).decl.getName() == "TsBoolean")
                return "bool";
            else if (((ClassType)t).decl.getName() == "TsNumber")
                return "int";
            else if (((ClassType)t).decl.getName() == "TsArray") {
                if (mutates) {
                    this.usings.add("System.Collections.Generic");
                    return "List<" + this.type(((ClassType)t).getTypeArguments()[0]) + ">";
                }
                else
                    return this.type(((ClassType)t).getTypeArguments()[0]) + "[]";
            }
            else if (((ClassType)t).decl.getName() == "Promise") {
                this.usings.add("System.Threading.Tasks");
                return ((ClassType)t).getTypeArguments()[0] instanceof VoidType ? "Task" : "Task" + typeArgs;
            }
            else if (((ClassType)t).decl.getName() == "Object") {
                this.usings.add("System");
                return "object";
            }
            else if (((ClassType)t).decl.getName() == "TsMap") {
                this.usings.add("System.Collections.Generic");
                return "Dictionary<string, " + this.type(((ClassType)t).getTypeArguments()[0]) + ">";
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
            return "object";
        else if (t instanceof NullType)
            return "null";
        else if (t instanceof GenericsType)
            return ((GenericsType)t).typeVarName;
        else if (t instanceof LambdaType) {
            var isFunc = !(((LambdaType)t).returnType instanceof VoidType);
            var paramTypes = Arrays.asList(Arrays.stream(((LambdaType)t).parameters).map(x -> this.type(x.getType())).toArray(String[]::new));
            if (isFunc)
                paramTypes.add(this.type(((LambdaType)t).returnType));
            this.usings.add("System");
            return (isFunc ? "Func" : "Action") + "<" + paramTypes.stream().collect(Collectors.joining(", ")) + ">";
        }
        else if (t == null)
            return "/* TODO */ object";
        else
            return "/* MISSING */";
    }
    
    public String type(IType t) {
        return this.type(t, true);
    }
    
    public Boolean isTsArray(IType type)
    {
        return type instanceof ClassType && ((ClassType)type).decl.getName() == "TsArray";
    }
    
    public String vis(Visibility v)
    {
        return v == Visibility.Private ? "private" : v == Visibility.Protected ? "protected" : v == Visibility.Public ? "public" : "/* TODO: not set */public";
    }
    
    public String varWoInit(IVariable v, IHasAttributesAndTrivia attr)
    {
        String type;
        if (attr != null && attr.getAttributes() != null && attr.getAttributes().containsKey("csharp-type"))
            type = attr.getAttributes().get("csharp-type");
        else if (v.getType() instanceof ClassType && ((ClassType)v.getType()).decl.getName() == "TsArray") {
            if (v.getMutability().mutated) {
                this.usings.add("System.Collections.Generic");
                type = "List<" + this.type(((ClassType)v.getType()).getTypeArguments()[0]) + ">";
            }
            else
                type = this.type(((ClassType)v.getType()).getTypeArguments()[0]) + "[]";
        }
        else
            type = this.type(v.getType());
        return type + " " + this.name_(v.getName());
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
            if (arg instanceof ArrayLiteral && !shouldBeMutable) {
                var itemType = (((ClassType)((ArrayLiteral)arg).actualType)).getTypeArguments()[0];
                return ((ArrayLiteral)arg).items.length == 0 && !this.isTsArray(itemType) ? "new " + this.type(itemType) + "[0]" : "new " + this.type(itemType) + "[] { " + Arrays.stream(Arrays.stream(((ArrayLiteral)arg).items).map(x -> this.expr(x)).toArray(String[]::new)).collect(Collectors.joining(", ")) + " }";
            }
            
            var currentlyMutable = shouldBeMutable;
            if (arg instanceof VariableReference)
                currentlyMutable = ((VariableReference)arg).getVariable().getMutability().mutated;
            else if (arg instanceof InstanceMethodCallExpression || arg instanceof StaticMethodCallExpression)
                currentlyMutable = false;
            
            if (currentlyMutable && !shouldBeMutable)
                return this.expr(arg) + ".ToArray()";
            else if (!currentlyMutable && shouldBeMutable) {
                this.usings.add("System.Linq");
                return this.expr(arg) + ".ToList()";
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
    
    public String expr(IExpression expr)
    {
        var res = "UNKNOWN-EXPR";
        if (expr instanceof NewExpression)
            res = "new " + this.type(((NewExpression)expr).cls) + this.callParams(((NewExpression)expr).args, ((NewExpression)expr).cls.decl.constructor_ != null ? ((NewExpression)expr).cls.decl.constructor_.getParameters() : new MethodParameter[0]);
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
            res = this.expr(((LambdaCallExpression)expr).method) + "(" + Arrays.stream(Arrays.stream(((LambdaCallExpression)expr).args).map(x -> this.expr(x)).toArray(String[]::new)).collect(Collectors.joining(", ")) + ")";
        else if (expr instanceof BooleanLiteral)
            res = (((BooleanLiteral)expr).boolValue ? "true" : "false");
        else if (expr instanceof StringLiteral)
            res = JSON.stringify(((StringLiteral)expr).stringValue);
        else if (expr instanceof NumericLiteral)
            res = ((NumericLiteral)expr).valueAsText;
        else if (expr instanceof CharacterLiteral)
            res = "'" + ((CharacterLiteral)expr).charValue + "'";
        else if (expr instanceof ElementAccessExpression)
            res = this.expr(((ElementAccessExpression)expr).object) + "[" + this.expr(((ElementAccessExpression)expr).elementExpr) + "]";
        else if (expr instanceof TemplateString) {
            var parts = new ArrayList<String>();
            for (var part : ((TemplateString)expr).parts) {
                // parts.push(part.literalText.replace(new RegExp("\\n"), $"\\n").replace(new RegExp("\\r"), $"\\r").replace(new RegExp("\\t"), $"\\t").replace(new RegExp("{"), "{{").replace(new RegExp("}"), "}}").replace(new RegExp("\""), $"\\\""));
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
                        else if (chr == "{")
                            lit += "{{";
                        else if (chr == "}")
                            lit += "}}";
                        else {
                            var chrCode = (int)chr.charAt(0);
                            if (32 <= chrCode && chrCode <= 126)
                                lit += chr;
                            else
                                throw new Error("invalid char in template string (code=" + chrCode + ")");
                        }
                    }
                    parts.add(lit);
                }
                else {
                    var repr = this.expr(part.expression);
                    parts.add(part.expression instanceof ConditionalExpression ? "{(" + repr + ")}" : "{" + repr + "}");
                }
            }
            res = "$\"" + parts.stream().collect(Collectors.joining("")) + "\"";
        }
        else if (expr instanceof BinaryExpression)
            res = this.expr(((BinaryExpression)expr).left) + " " + ((BinaryExpression)expr).operator + " " + this.mutatedExpr(((BinaryExpression)expr).right, ((BinaryExpression)expr).operator == "=" ? ((BinaryExpression)expr).left : null);
        else if (expr instanceof ArrayLiteral) {
            if (((ArrayLiteral)expr).items.length == 0)
                res = "new " + this.type(((ArrayLiteral)expr).actualType) + "()";
            else
                res = "new " + this.type(((ArrayLiteral)expr).actualType) + " { " + Arrays.stream(Arrays.stream(((ArrayLiteral)expr).items).map(x -> this.expr(x)).toArray(String[]::new)).collect(Collectors.joining(", ")) + " }";
        }
        else if (expr instanceof CastExpression) {
            if (((CastExpression)expr).instanceOfCast != null && ((CastExpression)expr).instanceOfCast.alias != null)
                res = this.name_(((CastExpression)expr).instanceOfCast.alias);
            else
                res = "((" + this.type(((CastExpression)expr).newType) + ")" + this.expr(((CastExpression)expr).expression) + ")";
        }
        else if (expr instanceof ConditionalExpression)
            res = this.expr(((ConditionalExpression)expr).condition) + " ? " + this.expr(((ConditionalExpression)expr).whenTrue) + " : " + this.mutatedExpr(((ConditionalExpression)expr).whenFalse, ((ConditionalExpression)expr).whenTrue);
        else if (expr instanceof InstanceOfExpression) {
            if (((InstanceOfExpression)expr).implicitCasts != null && ((InstanceOfExpression)expr).implicitCasts.size() > 0) {
                var aliasPrefix = this.inferExprNameForType(((InstanceOfExpression)expr).checkType);
                if (aliasPrefix == null)
                    aliasPrefix = ((InstanceOfExpression)expr).expr instanceof VariableReference ? ((VariableReference)((InstanceOfExpression)expr).expr).getVariable().getName() : "obj";
                var id = this.instanceOfIds.containsKey(aliasPrefix) ? this.instanceOfIds.get(aliasPrefix) : 1;
                this.instanceOfIds.put(aliasPrefix, id + 1);
                ((InstanceOfExpression)expr).alias = aliasPrefix + (id == 1 ? "" : id);
            }
            res = this.expr(((InstanceOfExpression)expr).expr) + " is " + this.type(((InstanceOfExpression)expr).checkType) + (((InstanceOfExpression)expr).alias != null ? " " + this.name_(((InstanceOfExpression)expr).alias) : "");
        }
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
            
            res = (params.length == 1 ? params[0] : "(" + Arrays.stream(params).collect(Collectors.joining(", ")) + ")") + " => " + body;
        }
        else if (expr instanceof UnaryExpression && ((UnaryExpression)expr).unaryType == UnaryType.Prefix)
            res = ((UnaryExpression)expr).operator + this.expr(((UnaryExpression)expr).operand);
        else if (expr instanceof UnaryExpression && ((UnaryExpression)expr).unaryType == UnaryType.Postfix)
            res = this.expr(((UnaryExpression)expr).operand) + ((UnaryExpression)expr).operator;
        else if (expr instanceof MapLiteral) {
            var repr = Arrays.stream(Arrays.stream(((MapLiteral)expr).items).map(item -> "[" + JSON.stringify(item.key) + "] = " + this.expr(item.value)).toArray(String[]::new)).collect(Collectors.joining(",\n"));
            res = "new " + this.type(((MapLiteral)expr).actualType) + " " + (repr == "" ? "{}" : repr.contains("\n") ? "{\n" + this.pad(repr) + "\n}" : "{ " + repr + " }");
        }
        else if (expr instanceof NullLiteral)
            res = "null";
        else if (expr instanceof AwaitExpression)
            res = "await " + this.expr(((AwaitExpression)expr).expr);
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
            res = "base";
        else if (expr instanceof StaticFieldReference)
            res = this.name_(((StaticFieldReference)expr).decl.parentInterface.getName()) + "." + this.name_(((StaticFieldReference)expr).decl.getName());
        else if (expr instanceof StaticPropertyReference)
            res = this.name_(((StaticPropertyReference)expr).decl.parentClass.getName()) + "." + this.name_(((StaticPropertyReference)expr).decl.getName());
        else if (expr instanceof InstanceFieldReference)
            res = this.expr(((InstanceFieldReference)expr).object) + "." + this.name_(((InstanceFieldReference)expr).field.getName());
        else if (expr instanceof InstancePropertyReference)
            res = this.expr(((InstancePropertyReference)expr).object) + "." + this.name_(((InstancePropertyReference)expr).property.getName());
        else if (expr instanceof EnumMemberReference)
            res = this.name_(((EnumMemberReference)expr).decl.parentEnum.getName()) + "." + this.name_(((EnumMemberReference)expr).decl.name);
        else if (expr instanceof NullCoalesceExpression)
            res = this.expr(((NullCoalesceExpression)expr).defaultExpr) + " ?? " + this.mutatedExpr(((NullCoalesceExpression)expr).exprIfNull, ((NullCoalesceExpression)expr).defaultExpr);
        else { }
        return res;
    }
    
    public String block(Block block, Boolean allowOneLiner)
    {
        var stmtLen = block.statements.size();
        return stmtLen == 0 ? " { }" : allowOneLiner && stmtLen == 1 && !(block.statements.get(0) instanceof IfStatement) ? "\n" + this.pad(this.rawBlock(block)) : " {\n" + this.pad(this.rawBlock(block)) + "\n}";
    }
    
    public String block(Block block) {
        return this.block(block, true);
    }
    
    public String stmt(Statement stmt)
    {
        var res = "UNKNOWN-STATEMENT";
        if (stmt.getAttributes() != null && stmt.getAttributes().containsKey("csharp"))
            res = stmt.getAttributes().get("csharp");
        else if (stmt instanceof BreakStatement)
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
            res = "foreach (var " + this.name_(((ForeachStatement)stmt).itemVar.getName()) + " in " + this.expr(((ForeachStatement)stmt).items) + ")" + this.block(((ForeachStatement)stmt).body);
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
            if (((TryStatement)stmt).catchBody != null) {
                this.usings.add("System");
                res += " catch (Exception " + this.name_(((TryStatement)stmt).catchVar.getName()) + ") " + this.block(((TryStatement)stmt).catchBody, false);
            }
            if (((TryStatement)stmt).finallyBody != null)
                res += "finally" + this.block(((TryStatement)stmt).finallyBody);
        }
        else if (stmt instanceof ContinueStatement)
            res = "continue;";
        else { }
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
    
    public String classLike(IInterface cls)
    {
        this.currentClass = cls;
        var resList = new ArrayList<String>();
        
        var staticConstructorStmts = new ArrayList<Statement>();
        var complexFieldInits = new ArrayList<Statement>();
        if (cls instanceof Class) {
            var fieldReprs = new ArrayList<String>();
            for (var field : ((Class)cls).getFields()) {
                var isInitializerComplex = field.getInitializer() != null && !(field.getInitializer() instanceof StringLiteral) && !(field.getInitializer() instanceof BooleanLiteral) && !(field.getInitializer() instanceof NumericLiteral);
                
                var prefix = this.vis(field.getVisibility()) + " " + this.preIf("static ", field.getIsStatic());
                if (field.interfaceDeclarations.length > 0)
                    fieldReprs.add(prefix + this.varWoInit(field, field) + " { get; set; }");
                else if (isInitializerComplex) {
                    if (field.getIsStatic())
                        staticConstructorStmts.add(new ExpressionStatement(new BinaryExpression(new StaticFieldReference(field), "=", field.getInitializer())));
                    else
                        complexFieldInits.add(new ExpressionStatement(new BinaryExpression(new InstanceFieldReference(new ThisReference(((Class)cls)), field), "=", field.getInitializer())));
                    
                    fieldReprs.add(prefix + this.varWoInit(field, field) + ";");
                }
                else
                    fieldReprs.add(prefix + this.var(field, field) + ";");
            }
            resList.add(fieldReprs.stream().collect(Collectors.joining("\n")));
            
            resList.add(Arrays.stream(Arrays.stream(((Class)cls).properties).map(prop -> this.vis(prop.getVisibility()) + " " + this.preIf("static ", prop.getIsStatic()) + this.varWoInit(prop, prop) + (prop.getter != null ? " {\n    get {\n" + this.pad(this.block(prop.getter)) + "\n    }\n}" : "") + (prop.setter != null ? " {\n    set {\n" + this.pad(this.block(prop.setter)) + "\n    }\n}" : "")).toArray(String[]::new)).collect(Collectors.joining("\n")));
            
            if (staticConstructorStmts.size() > 0)
                resList.add("static " + this.name_(((Class)cls).getName()) + "()\n{\n" + this.pad(this.stmts(staticConstructorStmts.toArray(Statement[]::new))) + "\n}");
            
            if (((Class)cls).constructor_ != null) {
                var constrFieldInits = new ArrayList<Statement>();
                for (var field : Arrays.stream(((Class)cls).getFields()).filter(x -> x.constructorParam != null).toArray(Field[]::new)) {
                    var fieldRef = new InstanceFieldReference(new ThisReference(((Class)cls)), field);
                    var mpRef = new MethodParameterReference(field.constructorParam);
                    // TODO: decide what to do with "after-TypeEngine" transformations
                    mpRef.setActualType(field.getType(), false, false);
                    constrFieldInits.add(new ExpressionStatement(new BinaryExpression(fieldRef, "=", mpRef)));
                }
                
                resList.add("public " + this.preIf("/* throws */ ", ((Class)cls).constructor_.getThrows()) + this.name_(((Class)cls).getName()) + "(" + Arrays.stream(Arrays.stream(((Class)cls).constructor_.getParameters()).map(p -> this.var(p, p)).toArray(String[]::new)).collect(Collectors.joining(", ")) + ")" + (((Class)cls).constructor_.superCallArgs != null ? ": base(" + Arrays.stream(Arrays.stream(((Class)cls).constructor_.superCallArgs).map(x -> this.expr(x)).toArray(String[]::new)).collect(Collectors.joining(", ")) + ")" : "") + "\n{\n" + this.pad(this.stmts(Stream.of(Stream.of(constrFieldInits, complexFieldInits).flatMap(Stream::of).toArray(Statement[]::new), ((Class)cls).constructor_.getBody().statements).flatMap(Stream::of).toArray(Statement[]::new))) + "\n}");
            }
            else if (complexFieldInits.size() > 0)
                resList.add("public " + this.name_(((Class)cls).getName()) + "()\n{\n" + this.pad(this.stmts(complexFieldInits.toArray(Statement[]::new))) + "\n}");
        }
        else if (cls instanceof Interface)
            resList.add(Arrays.stream(Arrays.stream(((Interface)cls).getFields()).map(field -> this.varWoInit(field, field) + " { get; set; }").toArray(String[]::new)).collect(Collectors.joining("\n")));
        
        var methods = new ArrayList<String>();
        for (var method : cls.getMethods()) {
            if (cls instanceof Class && method.getBody() == null)
                continue;
            // declaration only
            methods.add((method.parentInterface instanceof Interface ? "" : this.vis(method.getVisibility()) + " ") + this.preIf("static ", method.getIsStatic()) + this.preIf("virtual ", method.overrides == null && method.overriddenBy.size() > 0) + this.preIf("override ", method.overrides != null) + this.preIf("async ", method.async) + this.preIf("/* throws */ ", method.getThrows()) + this.type(method.returns, false) + " " + this.name_(method.name) + this.typeArgs(method.typeArguments) + "(" + Arrays.stream(Arrays.stream(method.getParameters()).map(p -> this.var(p, null)).toArray(String[]::new)).collect(Collectors.joining(", ")) + ")" + (method.getBody() != null ? "\n{\n" + this.pad(this.stmts(method.getBody().statements.toArray(Statement[]::new))) + "\n}" : ";"));
        }
        resList.add(methods.stream().collect(Collectors.joining("\n\n")));
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
    
    public String genFile(SourceFile sourceFile)
    {
        this.instanceOfIds = new HashMap<String, Integer>();
        this.usings = new HashSet<String>();
        var enums = Arrays.stream(sourceFile.enums).map(enum_ -> "public enum " + this.name_(enum_.getName()) + " { " + Arrays.stream(Arrays.stream(enum_.values).map(x -> this.name_(x.name)).toArray(String[]::new)).collect(Collectors.joining(", ")) + " }").toArray(String[]::new);
        
        var intfs = Arrays.stream(sourceFile.interfaces).map(intf -> "public interface " + this.name_(intf.getName()) + this.typeArgs(intf.getTypeArguments()) + this.preArr(" : ", Arrays.stream(intf.getBaseInterfaces()).map(x -> this.type(x)).toArray(String[]::new)) + " {\n" + this.classLike(intf) + "\n}").toArray(String[]::new);
        
        var classes = new ArrayList<String>();
        for (var cls : sourceFile.classes) {
            var baseClasses = new ArrayList<IType>();
            if (cls.baseClass != null)
                baseClasses.add(cls.baseClass);
            for (var intf : cls.getBaseInterfaces())
                baseClasses.add(intf);
            classes.add("public class " + this.name_(cls.getName()) + this.typeArgs(cls.getTypeArguments()) + this.preArr(" : ", baseClasses.stream().map(x -> this.type(x)).toArray(String[]::new)) + " {\n" + this.classLike(cls) + "\n}");
        }
        
        var main = sourceFile.mainBlock.statements.size() > 0 ? "public class Program\n{\n    static void Main(string[] args)\n    {\n" + this.pad(this.rawBlock(sourceFile.mainBlock)) + "\n    }\n}" : "";
        
        // @java var usingsSet = new HashSet<String>(Arrays.stream(sourceFile.imports).map(x -> this.pathToNs(x.exportScope.scopeName)).filter(x -> x != "").collect(Collectors.toList()));
        var usingsSet = new HashSet<String>(Arrays.stream(sourceFile.imports).map(x -> this.pathToNs(x.exportScope.scopeName)).filter(x -> x != "").collect(Collectors.toList()));
        for (var using : this.usings.toArray(String[]::new))
            usingsSet.add(using);
        
        var usings = new ArrayList<String>();
        for (var using : usingsSet.toArray(String[]::new))
            usings.add("using " + using + ";");
        
        var result = Arrays.stream(List.of(Arrays.stream(enums).collect(Collectors.joining("\n")), Arrays.stream(intfs).collect(Collectors.joining("\n\n")), classes.stream().collect(Collectors.joining("\n\n")), main).stream().filter(x -> x != "").toArray(String[]::new)).collect(Collectors.joining("\n\n"));
        var nl = "\n";
        // Python fix
        result = usings.stream().collect(Collectors.joining(nl)) + "\n\nnamespace " + this.pathToNs(sourceFile.sourcePath.path) + "\n{\n" + this.pad(result) + "\n}";
        return result;
    }
    
    public GeneratedFile[] generate(Package pkg)
    {
        var result = new ArrayList<GeneratedFile>();
        for (var path : pkg.files.keySet().toArray(String[]::new))
            result.add(new GeneratedFile(path, this.genFile(pkg.files.get(path))));
        return result.toArray(GeneratedFile[]::new);
    }
}