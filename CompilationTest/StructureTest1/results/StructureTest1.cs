using System;
using System.Collections.Generic;

public class MyList<T>
{
    public List<T> Items;
}

public class Item
{
    public int Offset = 5;
    public string StrTest = "test" + "test2";
    public string StrConstr = "constr";

    public Item(string strConstr)
    {
        this.StrConstr = strConstr;
    }
}

public class Container
{
    public MyList<Item> ItemList;
    public MyList<string> StringList;

    public void Method0()
    {
    }
    
    public string Method1(string str)
    {
        return str;
    }
}

public class Program
{
    static public void Main(string[] args)
    {
        Console.WriteLine("ok");
    }
}