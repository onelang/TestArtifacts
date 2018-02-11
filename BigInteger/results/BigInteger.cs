using System.Numerics;
using System;

public class MathUtils
{
    public static int Calc(int n)
    {
        var result = 1;
        for (var i = 2; i <= n; i++)
        {
            result = result * i;
            if (result > 10)
            {
                result = result >> 2;
            }
        }
        return result;
    }
    
    public static BigInteger CalcBig(int n)
    {
        var result = new BigInteger(1);
        for (var i = 2; i <= n; i++)
        {
            result = result * i + 123;
            result = result + result;
            if (result > 10)
            {
                result = result >> 2;
            }
        }
        return result;
    }
}

public class Program
{
    static public void Main(string[] args)
    {
        Console.WriteLine($"5 -> {MathUtils.Calc(5)}, 24 -> {MathUtils.CalcBig(24)}");
    }
}    