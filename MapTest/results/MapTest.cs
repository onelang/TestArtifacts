using System.Collections.Generic;
using System.Linq;
using System;

public class TestClass
{
    public int GetResult()
    {
        var mapObj = new Dictionary<string, int>
        {
          { "x", 5 }
        };
        //let containsX = "x" in mapObj;
        //delete mapObj["x"];
        mapObj["x"] = 3;
        return mapObj["x"];
    }
    
    public void TestMethod()
    {
        Console.WriteLine($"Result = {this.GetResult()}");
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