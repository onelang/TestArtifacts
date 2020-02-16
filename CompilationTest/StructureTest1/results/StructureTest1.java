import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

class List<T> {
    public List<T> items;
}

class Item {
    public Integer offset = 5;
    public String strTest = "test" + "test2";
    public String strConstr = "constr";

    public Item(String strConstr) throws Exception {
        this.strConstr = strConstr;
    }
}

class Container {
    public List<Item> itemList;
    public List<String> stringList;

    public void method0() throws Exception
    {
    }
    
    public String method1(String str) throws Exception
    {
        return str;
    }
}

class Program {
    public static void main(String[] args) throws Exception {
        System.out.println("ok");
    }
}