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
    
    public String convertMethod(Class cls, Expression obj, Method method, Expression[] args) {
        if (cls.getName().equals("TsArray")) {
            var objR = this.main.expr(obj);
            var argsR = Arrays.stream(args).map(x -> this.main.expr(x)).toArray(String[]::new);
            if (method.name.equals("includes"))
                return "in_array(" + argsR[0] + ", " + objR + ")";
            else if (method.name.equals("set"))
                return objR + "[" + argsR[0] + "] = " + argsR[1];
            else if (method.name.equals("get"))
                return objR + "[" + argsR[0] + "]";
            else if (method.name.equals("join"))
                return "implode(" + argsR[0] + ", " + objR + ")";
            else if (method.name.equals("map"))
                return "array_map(" + argsR[0] + ", " + objR + ")";
            else if (method.name.equals("push"))
                return objR + "[] = " + argsR[0];
            else if (method.name.equals("pop"))
                return "array_pop(" + objR + ")";
            else if (method.name.equals("filter"))
                return "array_values(array_filter(" + objR + ", " + argsR[0] + "))";
            else if (method.name.equals("every"))
                return "\\OneLang\\ArrayHelper::every(" + objR + ", " + argsR[0] + ")";
            else if (method.name.equals("some"))
                return "\\OneLang\\ArrayHelper::some(" + objR + ", " + argsR[0] + ")";
            else if (method.name.equals("concat"))
                return "array_merge(" + objR + ", " + argsR[0] + ")";
            else if (method.name.equals("shift"))
                return "array_shift(" + objR + ")";
            else if (method.name.equals("find"))
                return "\\OneLang\\ArrayHelper::find(" + objR + ", " + argsR[0] + ")";
        }
        else if (cls.getName().equals("TsString")) {
            var objR = this.main.expr(obj);
            var argsR = Arrays.stream(args).map(x -> this.main.expr(x)).toArray(String[]::new);
            if (method.name.equals("split")) {
                if (args[0] instanceof RegexLiteral) {
                    var pattern = (((RegexLiteral)args[0])).pattern;
                    var modPattern = "/" + pattern.replaceAll("/", "\\/") + "/";
                    return "preg_split(" + JSON.stringify(modPattern) + ", " + objR + ")";
                }
                
                return "explode(" + argsR[0] + ", " + objR + ")";
            }
            else if (method.name.equals("replace")) {
                if (args[0] instanceof RegexLiteral)
                    return "preg_replace(" + JSON.stringify("/" + (((RegexLiteral)args[0])).pattern + "/") + ", " + argsR[1] + ", " + objR + ")";
                
                return argsR[0] + ".replace(" + objR + ", " + argsR[1] + ")";
            }
            else if (method.name.equals("includes"))
                return "strpos(" + objR + ", " + argsR[0] + ") !== false";
            else if (method.name.equals("startsWith")) {
                if (argsR.length > 1)
                    return "substr_compare(" + objR + ", " + argsR[0] + ", " + argsR[1] + ", strlen(" + argsR[0] + ")) === 0";
                else
                    return "substr_compare(" + objR + ", " + argsR[0] + ", 0, strlen(" + argsR[0] + ")) === 0";
            }
            else if (method.name.equals("endsWith")) {
                if (argsR.length > 1)
                    return "substr_compare(" + objR + ", " + argsR[0] + ", " + argsR[1] + " - strlen(" + argsR[0] + "), strlen(" + argsR[0] + ")) === 0";
                else
                    return "substr_compare(" + objR + ", " + argsR[0] + ", strlen(" + objR + ") - strlen(" + argsR[0] + "), strlen(" + argsR[0] + ")) === 0";
            }
            else if (method.name.equals("indexOf"))
                return "strpos(" + objR + ", " + argsR[0] + ", " + argsR[1] + ")";
            else if (method.name.equals("lastIndexOf"))
                return "strrpos(" + objR + ", " + argsR[0] + ", " + argsR[1] + " - strlen(" + objR + "))";
            else if (method.name.equals("substr")) {
                if (argsR.length > 1)
                    return "substr(" + objR + ", " + argsR[0] + ", " + argsR[1] + ")";
                else
                    return "substr(" + objR + ", " + argsR[0] + ")";
            }
            else if (method.name.equals("substring"))
                return "substr(" + objR + ", " + argsR[0] + ", " + argsR[1] + " - (" + argsR[0] + "))";
            else if (method.name.equals("repeat"))
                return "str_repeat(" + objR + ", " + argsR[0] + ")";
            else if (method.name.equals("toUpperCase"))
                return "strtoupper(" + objR + ")";
            else if (method.name.equals("toLowerCase"))
                return "strtolower(" + objR + ")";
            else if (method.name.equals("get"))
                return objR + "[" + argsR[0] + "]";
            else if (method.name.equals("charCodeAt"))
                return "ord(" + objR + "[" + argsR[0] + "])";
        }
        else if (cls.getName().equals("TsMap")) {
            var objR = this.main.expr(obj);
            var argsR = Arrays.stream(args).map(x -> this.main.expr(x)).toArray(String[]::new);
            if (method.name.equals("set"))
                return objR + "[" + argsR[0] + "] = " + argsR[1];
            else if (method.name.equals("get"))
                return "@" + objR + "[" + argsR[0] + "] ?? null";
            else if (method.name.equals("hasKey"))
                return "array_key_exists(" + argsR[0] + ", " + objR + ")";
        }
        else if (cls.getName().equals("Object")) {
            var argsR = Arrays.stream(args).map(x -> this.main.expr(x)).toArray(String[]::new);
            if (method.name.equals("keys"))
                return "array_keys(" + argsR[0] + ")";
            else if (method.name.equals("values"))
                return "array_values(" + argsR[0] + ")";
        }
        else if (cls.getName().equals("ArrayHelper")) {
            var argsR = Arrays.stream(args).map(x -> this.main.expr(x)).toArray(String[]::new);
            if (method.name.equals("sortBy"))
                return "\\OneLang\\ArrayHelper::sortBy(" + argsR[0] + ", " + argsR[1] + ")";
            else if (method.name.equals("removeLastN"))
                return "array_splice(" + argsR[0] + ", -" + argsR[1] + ")";
        }
        else if (cls.getName().equals("Math")) {
            var argsR = Arrays.stream(args).map(x -> this.main.expr(x)).toArray(String[]::new);
            if (method.name.equals("floor"))
                return "floor(" + argsR[0] + ")";
        }
        else if (cls.getName().equals("JSON")) {
            var argsR = Arrays.stream(args).map(x -> this.main.expr(x)).toArray(String[]::new);
            if (method.name.equals("stringify"))
                return "json_encode(" + argsR[0] + ", JSON_UNESCAPED_SLASHES)";
        }
        else if (cls.getName().equals("RegExpExecArray")) {
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
    
    public String expr(IExpression expr) {
        if (expr instanceof InstanceMethodCallExpression && ((InstanceMethodCallExpression)expr).object.actualType instanceof ClassType)
            return this.convertMethod(((ClassType)((InstanceMethodCallExpression)expr).object.actualType).decl, ((InstanceMethodCallExpression)expr).object, ((InstanceMethodCallExpression)expr).getMethod(), ((InstanceMethodCallExpression)expr).getArgs());
        else if (expr instanceof InstancePropertyReference && ((InstancePropertyReference)expr).object.actualType instanceof ClassType) {
            if (((InstancePropertyReference)expr).property.parentClass.getName().equals("TsString") && ((InstancePropertyReference)expr).property.getName().equals("length"))
                return "strlen(" + this.main.expr(((InstancePropertyReference)expr).object) + ")";
            if (((InstancePropertyReference)expr).property.parentClass.getName().equals("TsArray") && ((InstancePropertyReference)expr).property.getName().equals("length"))
                return "count(" + this.main.expr(((InstancePropertyReference)expr).object) + ")";
        }
        else if (expr instanceof InstanceFieldReference && ((InstanceFieldReference)expr).object.actualType instanceof ClassType) {
            if (((InstanceFieldReference)expr).field.parentInterface.getName().equals("RegExpExecArray") && ((InstanceFieldReference)expr).field.getName().equals("length"))
                return "count(" + this.main.expr(((InstanceFieldReference)expr).object) + ")";
        }
        else if (expr instanceof StaticMethodCallExpression && ((StaticMethodCallExpression)expr).getMethod().parentInterface instanceof Class)
            return this.convertMethod(((Class)((StaticMethodCallExpression)expr).getMethod().parentInterface), null, ((StaticMethodCallExpression)expr).getMethod(), ((StaticMethodCallExpression)expr).getArgs());
        return null;
    }
    
    public String stmt(Statement stmt) {
        return null;
    }
}