class TestClass
  def test_method()
      map = {
      }
      keys = map.keys
  end
end

begin
    TestClass.new().test_method()
rescue Exception => err
    puts "Exception: #{err}"
end