import java.util.ArrayList;
import java.util.Arrays;
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
        System.out.println("n = " + (calc.calc()));
        
        List<Integer> arr = new ArrayList<Integer>(Arrays.asList(1, 2, 3));
        System.out.println("arr[1] = " + (arr.get(1)));
    }
}