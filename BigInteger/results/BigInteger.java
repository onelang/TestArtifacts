import java.math.BigInteger;

class MathUtils {
    public static Integer calc(Integer n) throws Exception
    {
        Integer result = 1;
        for (Integer i = 2; i <= n; i++) {
            result = result * i;
            if (result > 10) {
                result = result >> 2;
            }
        }
        return result;
    }
    
    public static BigInteger calcBig(Integer n) throws Exception
    {
        BigInteger result = BigInteger.valueOf(1);
        for (Integer i = 2; i <= n; i++) {
            result = result.multiply(BigInteger.valueOf(i)).add(BigInteger.valueOf(123));
            result = result.add(result);
            if (result.compareTo(BigInteger.valueOf(10)) > 0) {
                result = result.shiftRight(2);
            }
        }
        return result;
    }
}

class Program {
    public static void main(String[] args) throws Exception {
        System.out.println("5 -> " + MathUtils.calc(5) + ", 24 -> " + MathUtils.calcBig(24));
    }
}