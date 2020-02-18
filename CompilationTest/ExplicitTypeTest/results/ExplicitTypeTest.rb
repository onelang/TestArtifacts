class TestClass
  def test_method()
      op = "x"
      puts op.length
  end
end

begin
    TestClass.new().test_method()
rescue Exception => err
    puts "Exception: #{err}"
end