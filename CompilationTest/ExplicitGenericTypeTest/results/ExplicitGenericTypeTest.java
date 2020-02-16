import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

class TestClass {
    public void testMethod() throws Exception
    {
        List<String> result = new ArrayList<String>(Arrays.asList("y"));
        HashMap<String, Integer> map = new HashMap<String, Integer>();
        map.put("x", 5);
        List<String> keys = new ArrayList(map.keySet());
        System.out.println(result.get(0));
        System.out.println(keys.get(0));
    }
}

class Program {
    public static void main(String[] args) throws Exception {
        try {
            new TestClass().testMethod();
        } catch (Exception err) {
            System.out.println("Exception: " + err.getMessage());
        }
    }
}