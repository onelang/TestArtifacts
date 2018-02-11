using System;

public class TestClass
{
    public int NotThrows()
    {
        return 5;
    }
    
    public void FThrows()
    {
        throw new Exception("exception message");
    }
    
    public void TestMethod()
    {
        Console.WriteLine(this.NotThrows());
        this.FThrows();
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