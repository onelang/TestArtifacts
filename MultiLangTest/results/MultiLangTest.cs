using System;
using System.Collections.Generic;
using System.Linq;

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
        var map = new Dictionary<string, int>
        {
            { "a", 2 },
            { "b", 3 }
        };
        Console.WriteLine($"map['a'] = {map["a"]}, arr[1] = {arr[1]}");
        
        if (arr[0] == 1)
        {
            Console.WriteLine("TRUE-X");
        }
        else
        {
            Console.WriteLine("FALSE");
        }
        
        var sum = 0;
        for (var i = 0; i < 10; i++)
        {
            sum += i + 2;
        }
        Console.WriteLine(sum);
    }
}    