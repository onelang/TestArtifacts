class MathUtils 
  def self.calc(n)
      result = 1
      i = 2
      while i <= n
          result = result * i
          if result > 10
              result = result >> 2
          end
          i += 1
      end
      return result
  end

  def self.calc_big(n)
      result = 1
      i = 2
      while i <= n
          result = result * i + 123
          result = result + result
          if result > 10
              result = result >> 2
          end
          i += 1
      end
      return result
  end
end

puts "5 -> #{MathUtils.calc(5)}, 24 -> #{MathUtils.calc_big(24)}"