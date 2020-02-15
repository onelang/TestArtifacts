using System;
using System.IO;

public class Program
{
    static public void Main(string[] args)
    {
        File.WriteAllText("test.txt", "example content");
        var fileContent = File.ReadAllText("test.txt");
        Console.WriteLine(fileContent);
    }
}