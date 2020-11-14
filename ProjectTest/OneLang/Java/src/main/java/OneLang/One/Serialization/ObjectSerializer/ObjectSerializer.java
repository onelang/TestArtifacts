import java.util.ArrayList;

public class ObjectSerializer {
    public static String serialize(ReflectedValue obj) {
        if (obj.type instanceof ClassType) {
            var members = new ArrayList<String>();
            for (var field : ((ClassType)obj.type).decl.getFields())
                members.add("\"" + field.getName() + "\": " + ObjectSerializer.serialize(obj.getField(field.getName())));
            return members.size() == 0 ? "{}" : "{\n" + Global.pad(/* TODO: UnresolvedMethodCallExpression */ members.join(",\n")) + "\n}";
        }
        else
            return "\"<UNKNOWN-TYPE>\"";
    }
}