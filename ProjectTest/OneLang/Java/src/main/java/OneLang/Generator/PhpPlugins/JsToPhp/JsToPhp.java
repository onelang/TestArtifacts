import java.util.Set;
import java.util.HashSet;
import java.util.Arrays;
import java.util.regex.Pattern;

public class JsToPhp implements IGeneratorPlugin {
    public Set<String> unhandledMethods;
    public PhpGenerator main;
    
    public JsToPhp(PhpGenerator main)
    {
        this.main = main;
        this.unhandledMethods = new HashSet<String>();
    }
    
    public String convertMethod(Class cls, Expression obj, Method method, Expression[] args)
    {
        if (cls.getName() == "TsArray") {
            var objR = this.main.expr(obj);
            var argsR = Arrays.stream(args).map(x -> this.main.expr(x)).toArray(String[]::new);
            if (method.name == "includes")
                return "in_array(" + argsR[0] + ", " + objR + ")";
            else if (method.name == "set")
                return objR + "[" + argsR[0] + "] = " + argsR[1];
            else if (method.name == "get")
                return objR + "[" + argsR[0] + "]";
            else if (method.name == "join")
                return "implode(" + argsR[0] + ", " + objR + ")";
            else if (method.name == "map")
                return "array_map(" + argsR[0] + ", " + objR + ")";
            else if (method.name == "push")
                return objR + "[] = " + argsR[0];
            else if (method.name == "pop")
                return "array_pop(" + objR + ")";
            else if (method.name == "filter")
                return "array_values(array_filter(" + objR + ", " + argsR[0] + "))";
            else if (method.name == "every")
                return "\\OneLang\\ArrayHelper::every(" + objR + ", " + argsR[0] + ")";
            else if (method.name == "some")
                return "\\OneLang\\ArrayHelper::some(" + objR + ", " + argsR[0] + ")";
            else if (method.name == "concat")
                return "array_merge(" + objR + ", " + argsR[0] + ")";
            else if (method.name == "shift")
                return "array_shift(" + objR + ")";
            else if (method.name == "find")
                return "\\OneLang\\ArrayHelper::find(" + objR + ", " + argsR[0] + ")";
        }
        else if (cls.getName() == "TsString") {
            var objR = this.main.expr(obj);
            var argsR = Arrays.stream(args).map(x -> this.main.expr(x)).toArray(String[]::new);
            if (method.name == "split") {
                if (args[0] instanceof RegexLiteral) {
                    var pattern = (((RegexLiteral)args[0])).pattern;
                    var modPattern = "/" + pattern.replaceAll(Pattern.quote("/"), "\\/") + "/";
                    return "preg_split(" + JSON.stringify(modPattern) + ", " + objR + ")";
                }
                
                return "explode(" + argsR[0] + ", " + objR + ")";
            }
            else if (method.name == "replace") {
                if (args[0] instanceof RegexLiteral)
                    return "preg_replace(" + JSON.stringify("/" + (((RegexLiteral)args[0])).pattern + "/") + ", " + argsR[1] + ", " + objR + ")";
                
                return argsR[0] + ".replace(" + objR + ", " + argsR[1] + ")";
            }
            else if (method.name == "includes")
                return "strpos(" + objR + ", " + argsR[0] + ") !== false";
            else if (method.name == "startsWith") {
                if (argsR.length > 1)
                    return "substr_compare(" + objR + ", " + argsR[0] + ", " + argsR[1] + ", strlen(" + argsR[0] + ")) === 0";
                else
                    return "substr_compare(" + objR + ", " + argsR[0] + ", 0, strlen(" + argsR[0] + ")) === 0";
            }
            else if (method.name == "endsWith") {
                if (argsR.length > 1)
                    return "substr_compare(" + objR + ", " + argsR[0] + ", " + argsR[1] + " - strlen(" + argsR[0] + "), strlen(" + argsR[0] + ")) === 0";
                else
                    return "substr_compare(" + objR + ", " + argsR[0] + ", strlen(" + objR + ") - strlen(" + argsR[0] + "), strlen(" + argsR[0] + ")) === 0";
            }
            else if (method.name == "indexOf")
                return "strpos(" + objR + ", " + argsR[0] + ", " + argsR[1] + ")";
            else if (method.name == "lastIndexOf")
                return "strrpos(" + objR + ", " + argsR[0] + ", " + argsR[1] + " - strlen(" + objR + "))";
            else if (method.name == "substr") {
                if (argsR.length > 1)
                    return "substr(" + objR + ", " + argsR[0] + ", " + argsR[1] + ")";
                else
                    return "substr(" + objR + ", " + argsR[0] + ")";
            }
            else if (method.name == "substring")
                return "substr(" + objR + ", " + argsR[0] + ", " + argsR[1] + " - (" + argsR[0] + "))";
            else if (method.name == "repeat")
                return "str_repeat(" + objR + ", " + argsR[0] + ")";
            else if (method.name == "toUpperCase")
                return "strtoupper(" + objR + ")";
            else if (method.name == "toLowerCase")
                return "strtolower(" + objR + ")";
            else if (method.name == "get")
                return objR + "[" + argsR[0] + "]";
            else if (method.name == "charCodeAt")
                return "ord(" + objR + "[" + argsR[0] + "])";
        }
        else if (cls.getName() == "TsMap") {
            var objR = this.main.expr(obj);
            var argsR = Arrays.stream(args).map(x -> this.main.expr(x)).toArray(String[]::new);
            if (method.name == "set")
                return objR + "[" + argsR[0] + "] = " + argsR[1];
            else if (method.name == "get")
                return "@" + objR + "[" + argsR[0] + "] ?? null";
            else if (method.name == "hasKey")
                return "array_key_exists(" + argsR[0] + ", " + objR + ")";
        }
        else if (cls.getName() == "Object") {
            var argsR = Arrays.stream(args).map(x -> this.main.expr(x)).toArray(String[]::new);
            if (method.name == "keys")
                return "array_keys(" + argsR[0] + ")";
            else if (method.name == "values")
                return "array_values(" + argsR[0] + ")";
        }
        else if (cls.getName() == "ArrayHelper") {
            var argsR = Arrays.stream(args).map(x -> this.main.expr(x)).toArray(String[]::new);
            if (method.name == "sortBy")
                return "\\OneLang\\ArrayHelper::sortBy(" + argsR[0] + ", " + argsR[1] + ")";
            else if (method.name == "removeLastN")
                return "array_splice(" + argsR[0] + ", -" + argsR[1] + ")";
        }
        else if (cls.getName() == "Math") {
            var argsR = Arrays.stream(args).map(x -> this.main.expr(x)).toArray(String[]::new);
            if (method.name == "floor")
                return "floor(" + argsR[0] + ")";
        }
        else if (cls.getName() == "JSON") {
            var argsR = Arrays.stream(args).map(x -> this.main.expr(x)).toArray(String[]::new);
            if (method.name == "stringify")
                return "json_encode(" + argsR[0] + ", JSON_UNESCAPED_SLASHES)";
        }
        else if (cls.getName() == "RegExpExecArray") {
            var objR = this.main.expr(obj);
            var argsR = Arrays.stream(args).map(x -> this.main.expr(x)).toArray(String[]::new);
            return objR + "[" + argsR[0] + "]";
        }
        else
            return null;
        
        var methodName = cls.getName() + "." + method.name;
        if (!this.unhandledMethods.contains(methodName)) {
            console.error("[JsToPython] Method was not handled: " + cls.getName() + "." + method.name);
            this.unhandledMethods.add(methodName);
        }
        //debugger;
        return null;
    }
    
    public String expr(IExpression expr)
    {
        if (expr instanceof InstanceMethodCallExpression && ((InstanceMethodCallExpression)expr).object.actualType instanceof ClassType)
            return this.convertMethod(((ClassType)((InstanceMethodCallExpression)expr).object.actualType).decl, ((InstanceMethodCallExpression)expr).object, ((InstanceMethodCallExpression)expr).getMethod(), ((InstanceMethodCallExpression)expr).getArgs());
        else if (expr instanceof InstancePropertyReference && ((InstancePropertyReference)expr).object.actualType instanceof ClassType) {
            if (((InstancePropertyReference)expr).property.parentClass.getName() == "TsString" && ((InstancePropertyReference)expr).property.getName() == "length")
                return "strlen(" + this.main.expr(((InstancePropertyReference)expr).object) + ")";
            if (((InstancePropertyReference)expr).property.parentClass.getName() == "TsArray" && ((InstancePropertyReference)expr).property.getName() == "length")
                return "count(" + this.main.expr(((InstancePropertyReference)expr).object) + ")";
        }
        else if (expr instanceof InstanceFieldReference && ((InstanceFieldReference)expr).object.actualType instanceof ClassType) {
            if (((InstanceFieldReference)expr).field.parentInterface.getName() == "RegExpExecArray" && ((InstanceFieldReference)expr).field.getName() == "length")
                return "count(" + this.main.expr(((InstanceFieldReference)expr).object) + ")";
        }
        else if (expr instanceof StaticMethodCallExpression && ((StaticMethodCallExpression)expr).getMethod().parentInterface instanceof Class)
            return this.convertMethod(((Class)((StaticMethodCallExpression)expr).getMethod().parentInterface), null, ((StaticMethodCallExpression)expr).getMethod(), ((StaticMethodCallExpression)expr).getArgs());
        return null;
    }
    
    public String stmt(Statement stmt)
    {
        return null;
    }
}