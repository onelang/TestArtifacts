using Newtonsoft.Json.Linq;
using System;
using System.Linq;
using System.Collections.Generic;

public class Program
{
    static public void Main(string[] args)
    {
        var obj1 = JToken.Parse("{ \"a\":1, \"b\":2 }");
        if (!(obj1.Type == JTokenType.Object))
        {
            throw new Exception("expected to be object!");
        }
        var obj1Props = obj1.Cast<JProperty>().ToList();
        if (obj1Props.Count != 2)
        {
            throw new Exception("expected 2 properties");
        }
        if (obj1Props[0].Name != "a")
        {
            throw new Exception("expected first property to be named 'a'");
        }
        var obj1Prop0Value = obj1Props[0].Value;
        if (!(obj1Prop0Value.Type == JTokenType.Integer) || (long)((JValue)obj1Prop0Value).Value != 1)
        {
            throw new Exception("expected 'a' to be 1 (number)");
        }
        Console.WriteLine($"b = {(long)((JValue)obj1["b"]).Value}");
    }
}    