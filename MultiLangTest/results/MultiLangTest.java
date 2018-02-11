import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

class Calculator {
    public Integer calc() throws Exception
    {
        return 4;
    }
}

class Program {
    public static void main(String[] args) throws Exception {
        System.out.println("Hello!");
        
        Calculator calc = new Calculator();
        System.out.println("n = " + calc.calc());
        
        List<Integer> arr = new ArrayList<Integer>(Arrays.asList(1, 2, 3));
        HashMap<String, Integer> map = new HashMap<String, Integer>();
        map.put("a", 2);
        map.put("b", 3);
        System.out.println("map['a'] = " + map.get("a") + ", arr[1] = " + arr.get(1));
        
        if (arr.get(0) == 1) {
            System.out.println("TRUE-X");
        } else {
            System.out.println("FALSE");
        }
        
        Integer sum = 0;
        for (Integer i = 0; i < 10; i++) {
            sum += i + 2;
        }
        System.out.println(sum);
    }
}