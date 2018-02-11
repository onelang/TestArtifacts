class MathUtils {
  class func calc(n: Int) -> Int {
      var result = 1
      var i = 2
      while i <= n {
          result = result * i
          if result > 10 {
              result = result >> 2
          }
          i += 1
      }
      return result
  }

  class func calcBig(n: Int) -> OneBigInteger? {
      var result: OneBigInteger? = OneBigInteger.fromInt(value: 1)
      var i = 2
      while i <= n {
          result = result * i + 123
          result = result + result
          if result > 10 {
              result = result >> 2
          }
          i += 1
      }
      return result
  }
}


print("5 -> \(MathUtils.calc(n: 5)), 24 -> \(MathUtils.calcBig(n: 24)!)")