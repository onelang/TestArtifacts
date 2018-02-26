class BasePrinter
  attr_accessor(:num_value)

  def initialize()
      @num_value = 42
  end

  def get_value()
      return "Base"
  end

  def print_it()
      puts "BasePrinter: #{self.get_value()}"
  end

  def some_base_func()
      return self.num_value
  end
end

class ChildPrinter < BasePrinter
  def get_value()
      return "Child"
  end
end

class TestClass
  def get_printer(name)
      result = name == "child" ? ChildPrinter.new() : BasePrinter.new()
      return result
  end

  def test_method()
      base_printer = self.get_printer("base")
      child_printer = self.get_printer("child")
      base_printer.print_it()
      child_printer.print_it()
      puts "#{base_printer.some_base_func()} == #{child_printer.some_base_func()}"
      
      base_p2 = BasePrinter.new()
      child_p2 = ChildPrinter.new()
      puts "#{base_p2.num_value} == #{child_p2.num_value}"
  end
end

begin
    TestClass.new().test_method()
rescue Exception => err
    print "Exception: #{err}"
end