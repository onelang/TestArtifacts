import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

class Calculator {
    public Integer factor(Integer n) throws Exception
    {
        if (n <= 1) {
            return 1;
        } else {
            return this.factor(n - 1) * n;
        }
    }
}

class Program {
    public static void main(String[] args) throws Exception {
        System.out.println("Hello!");
        
        List<Integer> arr = new ArrayList<Integer>(Arrays.asList(1, 2, 3));
        arr.add(4);
        
        System.out.println("n = " + arr.size() + ", arr[0] = " + arr.get(0));
        
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
        
        Calculator calc = new Calculator();
        System.out.println("5! = " + calc.factor(5));
    }
}