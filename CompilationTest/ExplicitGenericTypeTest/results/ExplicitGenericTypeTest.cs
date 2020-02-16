using System;
using System.Collections.Generic;
using System.Linq;

public class TestClass
{
    public void TestMethod()
    {
        var result = new List<string> { "y" };
        var map = new Dictionary<string, int>
        {
            { "x", 5 }
        };
        var keys = map.Keys.ToArray();
        Console.WriteLine(result[0]);
        Console.WriteLine(keys[0]);
    }
}

public class Program
{
    static public void Main(string[] args)
    {
        try 
        {
            new TestClass().TestMethod();
        }
        catch (System.Exception e)
        {
            System.Console.WriteLine($"Exception: {e.Message}");
        }
    }
}