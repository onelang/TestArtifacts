using System;

public class StrLenInferIssue
{
    public static int Test(string str)
    {
        return str.Length;
    }
}

public class Program
{
    static public void Main(string[] args)
    {
        Console.WriteLine(StrLenInferIssue.Test("hello"));
    }
}