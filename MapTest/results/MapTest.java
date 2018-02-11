import java.util.HashMap;

class TestClass {
    public Integer getResult() throws Exception
    {
        HashMap<String, Integer> mapObj = new HashMap<String, Integer>();
        mapObj.put("x", 5);
        //let containsX = "x" in mapObj;
        //delete mapObj["x"];
        mapObj.put("x", 3);
        return mapObj.get("x");
    }
    
    public void testMethod() throws Exception
    {
        System.out.println("Result = " + this.getResult());
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