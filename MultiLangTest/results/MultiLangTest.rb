class Calculator
  def factor(n)
      if n <= 1
          return 1
      else
          return self.factor(n - 1) * n
      end
  end
end

puts "Hello!"

arr = [1, 2, 3]
arr << 4

puts "n = #{arr.length}, arr[0] = #{arr[0]}"

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

calc = Calculator.new()
puts "5! = #{calc.factor(5)}"