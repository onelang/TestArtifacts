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
    public int NumValue = 42;

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
        return this.NumValue;
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
        
        var baseP2 = new BasePrinter();
        var childP2 = new ChildPrinter();
        Console.WriteLine($"{baseP2.NumValue} == {childP2.NumValue}");
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