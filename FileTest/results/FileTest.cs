using System.IO;
using System;

public class Program
{
    static public void Main(string[] args)
    {
        var fileContent = File.ReadAllText("../../../input/test.txt");
        Console.WriteLine(fileContent);
    }
}    