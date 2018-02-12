using System.Collections.Generic;
using System.Linq;
using System;

public class TestClass
{
    public void TestMethod()
    {
        var value = 1 + 2 * 3 - 4;
        var map_ = new Dictionary<string, int>
        {
            { "a", 5 },
            { "b", 6 }
        };
        var text = $"Hello world! value = {value}, map[a] = {map_["a"]}";
        Console.WriteLine(text);
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