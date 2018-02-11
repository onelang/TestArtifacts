class Calculator 
  def calc()
      return 4
  end
end

puts "Hello!"

calc = Calculator.new()
puts "n = #{calc.calc()}"

arr = [1, 2, 3]
map = {
  "a" => 2,
  "b" => 3,
}
puts "map['a'] = #{map["a"]}, arr[1] = #{arr[1]}"

if arr[0] == 1
    puts "TRUE-X"
else
    puts "FALSE"
end

sum = 0
i = 0
while i < 10
    sum += i + 2
    i += 1
end
puts sum