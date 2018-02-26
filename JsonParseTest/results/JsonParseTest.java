import com.google.gson.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

class Program {
    public static void main(String[] args) throws Exception {
        JsonElement obj1 = new JsonParser().parse("{ \"a\":1, \"b\":2 }");
        if (!obj1.isJsonObject()) {
            throw new Exception("expected to be object!");
        }
        List<Map.Entry<String,JsonElement>> obj1Props = new ArrayList(obj1.getAsJsonObject().entrySet());
        if (obj1Props.size() != 2) {
            throw new Exception("expected 2 properties");
        }
        if (!obj1Props.get(0).getKey().equals("a")) {
            throw new Exception("expected first property to be named 'a'");
        }
        JsonElement obj1Prop0Value = obj1Props.get(0).getValue();
        if (!(obj1Prop0Value.isJsonPrimitive() && ((JsonPrimitive)obj1Prop0Value).isNumber()) || obj1Prop0Value.getAsInt() != 1) {
            throw new Exception("expected 'a' to be 1 (number)");
        }
        System.out.println("b = " + obj1.getAsJsonObject().get("b").getAsInt());
    }
}