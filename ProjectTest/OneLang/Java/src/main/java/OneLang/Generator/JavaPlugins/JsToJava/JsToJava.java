import java.util.Set;
import java.util.HashSet;
import java.util.Arrays;

public class JsToJava implements IGeneratorPlugin {
    public Set<String> unhandledMethods;
    public JavaGenerator main;
    
    public JsToJava(JavaGenerator main)
    {
        this.main = main;
        this.unhandledMethods = new HashSet<String>();
    }
    
    public Boolean isArray(Expression arrayExpr)
    {
        // TODO: InstanceMethodCallExpression is a hack, we should introduce real stream handling
        return arrayExpr instanceof VariableReference && !((VariableReference)arrayExpr).getVariable().getMutability().mutated || arrayExpr instanceof StaticMethodCallExpression || arrayExpr instanceof InstanceMethodCallExpression;
    }
    
    public String arrayStream(Expression arrayExpr)
    {
        var isArray = this.isArray(arrayExpr);
        var objR = this.main.expr(arrayExpr);
        if (isArray)
            this.main.imports.add("java.util.Arrays");
        return isArray ? "Arrays.stream(" + objR + ")" : objR + ".stream()";
    }
    
    public String toArray(IType arrayType, Integer typeArgIdx)
    {
        var type = (((ClassType)arrayType)).getTypeArguments()[typeArgIdx];
        return "toArray(" + this.main.type(type) + "[]::new)";
    }
    
    public String toArray(IType arrayType) {
        return this.toArray(arrayType, 0);
    }
    
    public String convertMethod(Class cls, Expression obj, Method method, Expression[] args, IType returnType)
    {
        var objR = this.main.expr(obj);
        var argsR = Arrays.stream(args).map(x -> this.main.expr(x)).toArray(String[]::new);
        if (cls.getName() == "TsArray") {
            if (method.name == "includes")
                return this.arrayStream(obj) + ".anyMatch(" + argsR[0] + "::equals)";
            else if (method.name == "set") {
                if (this.isArray(obj))
                    return objR + "[" + argsR[0] + "] = " + argsR[1];
                else
                    return objR + ".set(" + argsR[0] + ", " + argsR[1] + ")";
            }
            else if (method.name == "get")
                return this.isArray(obj) ? objR + "[" + argsR[0] + "]" : objR + ".get(" + argsR[0] + ")";
            else if (method.name == "join") {
                this.main.imports.add("java.util.stream.Collectors");
                return this.arrayStream(obj) + ".collect(Collectors.joining(" + argsR[0] + "))";
            }
            else if (method.name == "map")
                //if (returnType.repr() !== "C:TsArray<C:TsString>") debugger;
                return this.arrayStream(obj) + ".map(" + argsR[0] + ")." + this.toArray(returnType);
            else if (method.name == "push")
                return objR + ".add(" + argsR[0] + ")";
            else if (method.name == "pop")
                return objR + ".remove(" + objR + ".size() - 1)";
            else if (method.name == "filter")
                return this.arrayStream(obj) + ".filter(" + argsR[0] + ")." + this.toArray(returnType);
            else if (method.name == "every")
                return "StdArrayHelper.allMatch(" + objR + ", " + argsR[0] + ")";
            else if (method.name == "some")
                return this.arrayStream(obj) + ".anyMatch(" + argsR[0] + ")";
            else if (method.name == "concat") {
                this.main.imports.add("java.util.stream.Stream");
                return "Stream.of(" + objR + ", " + argsR[0] + ").flatMap(Stream::of)." + this.toArray(obj.getType());
            }
            else if (method.name == "shift")
                return objR + ".remove(0)";
            else if (method.name == "find")
                return this.arrayStream(obj) + ".filter(" + argsR[0] + ").findFirst().orElse(null)";
        }
        else if (cls.getName() == "TsString") {
            if (method.name == "replace") {
                if (args[0] instanceof RegexLiteral) {
                    this.main.imports.add("java.util.regex.Pattern");
                    return objR + ".replaceAll(Pattern.quote(" + JSON.stringify((((RegexLiteral)args[0])).pattern) + "), " + argsR[1] + ")";
                }
                
                return argsR[0] + ".replace(" + objR + ", " + argsR[1] + ")";
            }
            else if (method.name == "charCodeAt")
                return "(int)" + objR + ".charAt(" + argsR[0] + ")";
            else if (method.name == "includes")
                return objR + ".contains(" + argsR[0] + ")";
            else if (method.name == "get")
                return objR + ".substring(" + argsR[0] + ", " + argsR[0] + " + 1)";
            else if (method.name == "substr")
                return argsR.length == 1 ? objR + ".substring(" + argsR[0] + ")" : objR + ".substring(" + argsR[0] + ", " + argsR[0] + " + " + argsR[1] + ")";
            else if (method.name == "substring")
                return objR + ".substring(" + argsR[0] + ", " + argsR[1] + ")";
            
            if (method.name == "split" && args[0] instanceof RegexLiteral) {
                var pattern = (((RegexLiteral)args[0])).pattern;
                return objR + ".split(" + JSON.stringify(pattern) + ")";
            }
        }
        else if (cls.getName() == "TsMap" || cls.getName() == "Map") {
            if (method.name == "set")
                return objR + ".put(" + argsR[0] + ", " + argsR[1] + ")";
            else if (method.name == "get")
                return objR + ".get(" + argsR[0] + ")";
            else if (method.name == "hasKey" || method.name == "has")
                return objR + ".containsKey(" + argsR[0] + ")";
            else if (method.name == "delete")
                return objR + ".remove(" + argsR[0] + ")";
            else if (method.name == "values")
                return objR + ".values()." + this.toArray(obj.getType(), 1);
        }
        else if (cls.getName() == "Object") {
            if (method.name == "keys")
                return argsR[0] + ".keySet().toArray(String[]::new)";
            else if (method.name == "values")
                return argsR[0] + ".values()." + this.toArray(args[0].getType());
        }
        else if (cls.getName() == "Set") {
            if (method.name == "values")
                return objR + "." + this.toArray(obj.getType());
            else if (method.name == "has")
                return objR + ".contains(" + argsR[0] + ")";
            else if (method.name == "add")
                return objR + ".add(" + argsR[0] + ")";
        }
        else if (cls.getName() == "ArrayHelper") { }
        else if (cls.getName() == "Array") {
            if (method.name == "from")
                return argsR[0];
        }
        else if (cls.getName() == "Promise") {
            if (method.name == "resolve")
                return argsR[0];
        }
        else if (cls.getName() == "RegExpExecArray") {
            if (method.name == "get")
                return objR + "[" + argsR[0] + "]";
        }
        else
            return null;
        
        var methodName = cls.getName() + "." + method.name;
        if (!this.unhandledMethods.contains(methodName)) {
            console.error("[JsToJava] Method was not handled: " + cls.getName() + "." + method.name);
            this.unhandledMethods.add(methodName);
        }
        //debugger;
        return null;
    }
    
    public String expr(IExpression expr)
    {
        if (expr instanceof InstanceMethodCallExpression && ((InstanceMethodCallExpression)expr).object.actualType instanceof ClassType)
            return this.convertMethod(((ClassType)((InstanceMethodCallExpression)expr).object.actualType).decl, ((InstanceMethodCallExpression)expr).object, ((InstanceMethodCallExpression)expr).getMethod(), ((InstanceMethodCallExpression)expr).getArgs(), ((InstanceMethodCallExpression)expr).actualType);
        else if (expr instanceof InstancePropertyReference && ((InstancePropertyReference)expr).object.actualType instanceof ClassType) {
            if (((InstancePropertyReference)expr).property.parentClass.getName() == "TsString" && ((InstancePropertyReference)expr).property.getName() == "length")
                return this.main.expr(((InstancePropertyReference)expr).object) + ".length()";
            if (((InstancePropertyReference)expr).property.parentClass.getName() == "TsArray" && ((InstancePropertyReference)expr).property.getName() == "length")
                return this.main.expr(((InstancePropertyReference)expr).object) + "." + (this.isArray(((InstancePropertyReference)expr).object) ? "length" : "size()");
        }
        else if (expr instanceof InstanceFieldReference && ((InstanceFieldReference)expr).object.actualType instanceof ClassType) {
            if (((InstanceFieldReference)expr).field.parentInterface.getName() == "RegExpExecArray" && ((InstanceFieldReference)expr).field.getName() == "length")
                return this.main.expr(((InstanceFieldReference)expr).object) + ".length";
        }
        else if (expr instanceof StaticMethodCallExpression && ((StaticMethodCallExpression)expr).getMethod().parentInterface instanceof Class)
            return this.convertMethod(((Class)((StaticMethodCallExpression)expr).getMethod().parentInterface), null, ((StaticMethodCallExpression)expr).getMethod(), ((StaticMethodCallExpression)expr).getArgs(), ((StaticMethodCallExpression)expr).actualType);
        return null;
    }
    
    public String stmt(Statement stmt)
    {
        return null;
    }
}