class TestClass
  def test_method()
      value = 1 + 2 * 3 - 4
      map_ = {
        "a" => 5,
        "b" => 6,
      }
      text = "Hello world! value = #{value}, map[a] = #{map_["a"]}"
      puts text
  end
end

begin
    TestClass.new().test_method()
rescue Exception => err
    print "Exception: #{err}"
end