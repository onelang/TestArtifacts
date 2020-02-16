using System;

public class TestClass
{
    public void TestMethod()
    {
        var op = "x";
        Console.WriteLine(op.Length);
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