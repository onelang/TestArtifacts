using System;

public interface IPrinterBase
{
    int SomeBaseFunc();
}

public interface IPrinter: IPrinterBase
{
    void PrintIt();
}

public class BasePrinter: IPrinter
{
    public virtual string GetValue()
    {
        return "Base";
    }
    
    public void PrintIt()
    {
        Console.WriteLine($"BasePrinter: {this.GetValue()}");
    }
    
    public int SomeBaseFunc()
    {
        return 42;
    }
}

public class ChildPrinter: BasePrinter
{
    public override string GetValue()
    {
        return "Child";
    }
}

public class TestClass
{
    public IPrinter GetPrinter(string name)
    {
        var result = name == "child" ? new ChildPrinter() : new BasePrinter();
        return result;
    }
    
    public void TestMethod()
    {
        var basePrinter = this.GetPrinter("base");
        var childPrinter = this.GetPrinter("child");
        basePrinter.PrintIt();
        childPrinter.PrintIt();
        Console.WriteLine($"{basePrinter.SomeBaseFunc()} == {childPrinter.SomeBaseFunc()}");
    }
}

public class Program
{
    static public void Main()
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