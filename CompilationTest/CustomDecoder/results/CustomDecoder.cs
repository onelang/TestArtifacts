using System;
using System.Collections.Generic;

public interface ICustomDecoder
{
    List<int> Decode(List<int> src);
}

public class XorByte: ICustomDecoder
{
    public int XorValue;

    public XorByte(int xorValue)
    {
        this.XorValue = xorValue;
    }

    public List<int> Decode(List<int> src)
    {
        var dest = new List<int> {  };
        
        for (var i = 0; i < src.Count; i++)
        {
            dest.Add(src[i] ^ this.XorValue);
        }
        
        return dest;
    }
}

public class Base64: ICustomDecoder
{
    public List<int> Decode(List<int> src)
    {
        var dest = new List<int> {  };
        
        // 4 base64 chars => 3 bytes
        for (var i = 0; i < src.Count; i += 4)
        {
            var ch0 = this.DecodeChar(src[i]);
            var ch1 = this.DecodeChar(src[i + 1]);
            var ch2 = this.DecodeChar(src[i + 2]);
            var ch3 = this.DecodeChar(src[i + 3]);
            
            var trinity = (ch0 << 18) + (ch1 << 12) + (ch2 << 6) + (ch3);
            
            dest.Add(trinity >> 16);
            dest.Add((trinity >> 8) & 0xff);
            dest.Add(trinity & 0xff);
        }
        
        return dest;
    }
    
    public int DecodeChar(int ch)
    {
        var value = -1;
        if (ch >= 65 && ch <= 90)
        {
            // `A-Z` => 0..25
            value = ch - 65;
        }
        else if (ch >= 97 && ch <= 122)
        {
            // `a-z` => 26..51
            value = ch - 97 + 26;
        }
        else if (ch >= 48 && ch <= 57)
        {
            // `0-9` => 52..61
            value = ch - 48 + 52;
        }
        else if (ch == 43 || ch == 45)
        {
            // `+` or `-` => 62
            value = 62;
        }
        else if (ch == 47 || ch == 95)
        {
            // `/` or `_` => 63
            value = 63;
        }
        else if (ch == 61)
        {
            // `=` => padding
            value = 0;
        }
        else
        {
        }
        return value;
    }
}

public class TestClass
{
    public void TestMethod()
    {
        var src1 = new List<int> { 4, 5, 6 };
        var decoder = new XorByte(0xff);
        var dst1 = decoder.Decode(src1);
        foreach (var x in dst1)
        {
            Console.WriteLine(x);
        }
        
        Console.WriteLine("|");
        
        var src2 = new List<int> { 97, 71, 86, 115, 98, 71, 56, 61 };
        var decoder2 = new Base64();
        var dst2 = decoder2.Decode(src2);
        foreach (var x in dst2)
        {
            Console.WriteLine(x);
        }
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