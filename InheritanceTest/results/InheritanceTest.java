interface IPrinterBase {
  Integer someBaseFunc() throws Exception;
}

interface IPrinter extends IPrinterBase {
  void printIt() throws Exception;
}

class BasePrinter implements IPrinter {
    public Integer numValue = 42;

    public String getValue() throws Exception
    {
        return "Base";
    }
    
    public void printIt() throws Exception
    {
        System.out.println("BasePrinter: " + this.getValue());
    }
    
    public Integer someBaseFunc() throws Exception
    {
        return this.numValue;
    }
}

class ChildPrinter extends BasePrinter {
    public String getValue() throws Exception
    {
        return "Child";
    }
}

class TestClass {
    public IPrinter getPrinter(String name) throws Exception
    {
        IPrinter result = name.equals("child") ? new ChildPrinter() : new BasePrinter();
        return result;
    }
    
    public void testMethod() throws Exception
    {
        IPrinter basePrinter = this.getPrinter("base");
        IPrinter childPrinter = this.getPrinter("child");
        basePrinter.printIt();
        childPrinter.printIt();
        System.out.println(basePrinter.someBaseFunc() + " == " + childPrinter.someBaseFunc());
        
        BasePrinter baseP2 = new BasePrinter();
        ChildPrinter childP2 = new ChildPrinter();
        System.out.println(baseP2.numValue + " == " + childP2.numValue);
    }
}

class Program {
    public static void main(String[] args) throws Exception {
        try {
            new TestClass().testMethod();
        } catch (Exception err) {
            System.out.println("Exception: " + err.getMessage());
        }
    }
}