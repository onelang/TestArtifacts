using System;

public class TokenType
{
    public static string EndToken = "EndToken";
    public static string Whitespace = "Whitespace";
    public static string Identifier = "Identifier";
    public static string OperatorX = "Operator";
    public static string NoInitializer;
}

public class Program
{
    static public void Main(string[] args)
    {
        var casingTest = TokenType.EndToken;
        Console.WriteLine(casingTest);
    }
}