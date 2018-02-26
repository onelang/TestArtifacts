using System;
using System.Collections.Generic;
using System.Linq;

public class Calculator
{
    public int Factor(int n)
    {
        if (n <= 1)
        {
            return 1;
        }
        else
        {
            return this.Factor(n - 1) * n;
        }
    }
}

public class Program
{
    static public void Main(string[] args)
    {
        Console.WriteLine("Hello!");
        
        var arr = new List<int> { 1, 2, 3 };
        arr.Add(4);
        
        Console.WriteLine($"n = {arr.Count}, arr[0] = {arr[0]}");
        
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
        
        var calc = new Calculator();
        Console.WriteLine($"5! = {calc.Factor(5)}");
    }
}