class MyList
  attr_accessor(:items)
end

class Item
  attr_accessor(:offset)
  attr_accessor(:str_test)
  attr_accessor(:str_constr)

  def initialize(str_constr)
      @offset = 5
      @str_test = "test" + "test2"
      @str_constr = "constr"

      self.str_constr = str_constr
  end
end

class Container
  attr_accessor(:item_list)
  attr_accessor(:string_list)

  def method0()
  end

  def method1(str)
      return str
  end
end

puts "ok"