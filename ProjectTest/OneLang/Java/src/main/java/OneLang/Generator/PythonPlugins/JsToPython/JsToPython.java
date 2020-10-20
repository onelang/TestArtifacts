import java.util.Set;
import java.util.HashSet;
import java.util.Arrays;
import java.util.stream.Collectors;

public class JsToPython implements IGeneratorPlugin {
    public Set<String> unhandledMethods;
    public PythonGenerator main;
    
    public JsToPython(PythonGenerator main)
    {
        this.main = main;
        this.unhandledMethods = new HashSet<String>();
    }
    
    public String convertMethod(Class cls, Expression obj, Method method, Expression[] args) {
        if (cls.getName().equals("TsArray")) {
            var objR = this.main.expr(obj);
            var argsR = Arrays.stream(args).map(x -> this.main.expr(x)).toArray(String[]::new);
            if (method.name.equals("includes"))
                return argsR[0] + " in " + objR;
            else if (method.name.equals("set"))
                return objR + "[" + argsR[0] + "] = " + argsR[1];
            else if (method.name.equals("get"))
                return objR + "[" + argsR[0] + "]";
            else if (method.name.equals("join"))
                return argsR[0] + ".join(" + objR + ")";
            else if (method.name.equals("map"))
                return "list(map(" + argsR[0] + ", " + objR + "))";
            else if (method.name.equals("push"))
                return objR + ".append(" + argsR[0] + ")";
            else if (method.name.equals("pop"))
                return objR + ".pop()";
            else if (method.name.equals("filter"))
                return "list(filter(" + argsR[0] + ", " + objR + "))";
            else if (method.name.equals("every"))
                return "ArrayHelper.every(" + argsR[0] + ", " + objR + ")";
            else if (method.name.equals("some"))
                return "ArrayHelper.some(" + argsR[0] + ", " + objR + ")";
            else if (method.name.equals("concat"))
                return objR + " + " + argsR[0];
            else if (method.name.equals("shift"))
                return objR + ".pop(0)";
            else if (method.name.equals("find"))
                return "next(filter(" + argsR[0] + ", " + objR + "), None)";
        }
        else if (cls.getName().equals("TsString")) {
            var objR = this.main.expr(obj);
            var argsR = Arrays.stream(args).map(x -> this.main.expr(x)).toArray(String[]::new);
            if (method.name.equals("split")) {
                if (args[0] instanceof RegexLiteral) {
                    var pattern = (((RegexLiteral)args[0])).pattern;
                    if (!pattern.startsWith("^")) {
                        //return `${objR}.split(${JSON.stringify(pattern)})`;
                        this.main.imports.add("import re");
                        return "re.split(" + JSON.stringify(pattern) + ", " + objR + ")";
                    }
                }
                
                return argsR[0] + ".split(" + objR + ")";
            }
            else if (method.name.equals("replace")) {
                if (args[0] instanceof RegexLiteral) {
                    this.main.imports.add("import re");
                    return "re.sub(" + JSON.stringify((((RegexLiteral)args[0])).pattern) + ", " + argsR[1] + ", " + objR + ")";
                }
                
                return argsR[0] + ".replace(" + objR + ", " + argsR[1] + ")";
            }
            else if (method.name.equals("includes"))
                return argsR[0] + " in " + objR;
            else if (method.name.equals("startsWith"))
                return objR + ".startswith(" + Arrays.stream(argsR).collect(Collectors.joining(", ")) + ")";
            else if (method.name.equals("indexOf"))
                return objR + ".find(" + argsR[0] + ", " + argsR[1] + ")";
            else if (method.name.equals("lastIndexOf"))
                return objR + ".rfind(" + argsR[0] + ", 0, " + argsR[1] + ")";
            else if (method.name.equals("substr"))
                return argsR.length == 1 ? objR + "[" + argsR[0] + ":]" : objR + "[" + argsR[0] + ":" + argsR[0] + " + " + argsR[1] + "]";
            else if (method.name.equals("substring"))
                return objR + "[" + argsR[0] + ":" + argsR[1] + "]";
            else if (method.name.equals("repeat"))
                return objR + " * (" + argsR[0] + ")";
            else if (method.name.equals("toUpperCase"))
                return objR + ".upper()";
            else if (method.name.equals("toLowerCase"))
                return objR + ".lower()";
            else if (method.name.equals("endsWith"))
                return objR + ".endswith(" + argsR[0] + ")";
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
                return objR + ".get(" + argsR[0] + ")";
            else if (method.name.equals("hasKey"))
                return argsR[0] + " in " + objR;
        }
        else if (cls.getName().equals("Object")) {
            var argsR = Arrays.stream(args).map(x -> this.main.expr(x)).toArray(String[]::new);
            if (method.name.equals("keys"))
                return argsR[0] + ".keys()";
            else if (method.name.equals("values"))
                return argsR[0] + ".values()";
        }
        else if (cls.getName().equals("Set")) {
            var objR = this.main.expr(obj);
            var argsR = Arrays.stream(args).map(x -> this.main.expr(x)).toArray(String[]::new);
            if (method.name.equals("values"))
                return objR + ".keys()";
            else if (method.name.equals("has"))
                return argsR[0] + " in " + objR;
            else if (method.name.equals("add"))
                return objR + "[" + argsR[0] + "] = None";
        }
        else if (cls.getName().equals("ArrayHelper")) {
            var argsR = Arrays.stream(args).map(x -> this.main.expr(x)).toArray(String[]::new);
            if (method.name.equals("sortBy"))
                return "sorted(" + argsR[0] + ", key=" + argsR[1] + ")";
            else if (method.name.equals("removeLastN"))
                return "del " + argsR[0] + "[-" + argsR[1] + ":]";
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
                return "len(" + this.main.expr(((InstancePropertyReference)expr).object) + ")";
            if (((InstancePropertyReference)expr).property.parentClass.getName().equals("TsArray") && ((InstancePropertyReference)expr).property.getName().equals("length"))
                return "len(" + this.main.expr(((InstancePropertyReference)expr).object) + ")";
        }
        else if (expr instanceof InstanceFieldReference && ((InstanceFieldReference)expr).object.actualType instanceof ClassType) {
            if (((InstanceFieldReference)expr).field.parentInterface.getName().equals("RegExpExecArray") && ((InstanceFieldReference)expr).field.getName().equals("length"))
                return "len(" + this.main.expr(((InstanceFieldReference)expr).object) + ")";
        }
        else if (expr instanceof StaticMethodCallExpression && ((StaticMethodCallExpression)expr).getMethod().parentInterface instanceof Class)
            return this.convertMethod(((Class)((StaticMethodCallExpression)expr).getMethod().parentInterface), null, ((StaticMethodCallExpression)expr).getMethod(), ((StaticMethodCallExpression)expr).getArgs());
        return null;
    }
    
    public String stmt(Statement stmt) {
        return null;
    }
}