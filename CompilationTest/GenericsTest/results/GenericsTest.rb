class MapX
  attr_accessor(:value)

  def set(key, value)
      self.value = value
  end

  def get(key)
      return self.value
  end
end

class TestClass
  def test_method()
      map_x = MapX.new()
      map_x.set("hello", 3)
      num_value = map_x.get("hello2")
      puts "#{num_value}"
  end
end

begin
    TestClass.new().test_method()
rescue Exception => err
    puts "Exception: #{err}"
end