class TestClass
  def test_method()
      result = ["y"]
      map = {
        "x" => 5,
      }
      keys = map.keys
      puts result[0]
      puts keys[0]
  end
end

begin
    TestClass.new().test_method()
rescue Exception => err
    print "Exception: #{err}"
end