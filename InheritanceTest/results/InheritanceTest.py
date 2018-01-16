class BasePrinter:
    def __init__(self):
        self.num_value = 42

    def get_value(self):
        return "Base"
    
    def print_it(self):
        print "BasePrinter: %s" % (self.get_value(), )
    
    def some_base_func(self):
        return self.num_value

class ChildPrinter(BasePrinter):
    def get_value(self):
        return "Child"

class TestClass:
    def get_printer(self, name):
        result = ChildPrinter() if name == "child" else BasePrinter()
        return result
    
    def test_method(self):
        base_printer = self.get_printer("base")
        child_printer = self.get_printer("child")
        base_printer.print_it()
        child_printer.print_it()
        print "%s == %s" % (base_printer.some_base_func(), child_printer.some_base_func(), )
        
        base_p2 = BasePrinter()
        child_p2 = ChildPrinter()
        print "%s == %s" % (base_p2.num_value, child_p2.num_value, )

try:
    TestClass().test_method()
except Exception as err:
    print "Exception: " + err.message