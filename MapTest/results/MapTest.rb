class TestClass 
  def get_result()
      map_obj = {
        "x" => 5,
      }
      #let containsX = "x" in mapObj;
      #delete mapObj["x"];
      map_obj["x"] = 3
      return map_obj["x"]
  end

  def test_method()
      puts "Result = #{self.get_result()}"
  end
end

begin
    TestClass.new().test_method()
rescue Exception => err
    print "Exception: #{err}"
end