using System;
using System.Collections.Generic;

public class Calculator
{
    public int Calc()
    {
        return 4;
    }
}

public class Program
{
    static public void Main(string[] args)
    {
        Console.WriteLine("Hello!");
        
        var calc = new Calculator();
        Console.WriteLine($"n = {calc.Calc()}");
        
        var arr = new List<int> { 1, 2, 3 };
        Console.WriteLine($"arr[1] = {arr[1]}");
    }
}    