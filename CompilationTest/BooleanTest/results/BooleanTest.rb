class TestClass
  def test_method()
      a = true
      b = false
      c = a && b
      d = a || b
      puts "a: #{a}, b: #{b}, c: #{c}, d: #{d}"
  end
end

begin
    TestClass.new().test_method()
rescue Exception => err
    puts "Exception: #{err}"
end