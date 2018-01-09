import java.util.HashMap;

class TestClass {
    public void testMethod() throws Exception
    {
        Integer value = 1 + 2 * 3 - 4;
        HashMap<String, Integer> map_ = new HashMap<String, Integer>();
        map_.put("a", 5);
        map_.put("b", 6);
        String text = "Hello world! value = " + value + ", map[a] = " + map_.get("a");
        System.out.println(text);
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