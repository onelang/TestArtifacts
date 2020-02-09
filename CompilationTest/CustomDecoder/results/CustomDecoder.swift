protocol ICustomDecoder {
    func decode(src: [Int]?) -> [Int]?;
}

class XorByte: ICustomDecoder {
  var xorValue: Int

  init(xorValue: Int) {
      self.xorValue = xorValue
  }

  func decode(src: [Int]?) -> [Int]? {
      var dest: [Int]? = [Int]()
      
      var i = 0
      while i < src!.count {
          dest!.append(src![i] ^ self.xorValue)
          i += 1
      }
      
      return dest
  }
}

class Base64: ICustomDecoder {
  func decode(src: [Int]?) -> [Int]? {
      var dest: [Int]? = [Int]()
      
      // 4 base64 chars => 3 bytes
      var i = 0
      while i < src!.count {
          let ch0 = self.decodeChar(ch: src![i])
          let ch1 = self.decodeChar(ch: src![i + 1])
          let ch2 = self.decodeChar(ch: src![i + 2])
          let ch3 = self.decodeChar(ch: src![i + 3])
          
          let trinity = (ch0 << 18) + (ch1 << 12) + (ch2 << 6) + (ch3)
          
          dest!.append(trinity >> 16)
          dest!.append((trinity >> 8) & 0xff)
          dest!.append(trinity & 0xff)
          i += 4
      }
      
      return dest
  }

  func decodeChar(ch: Int) -> Int {
      var value = -1
      if ch >= 65 && ch <= 90 {
          // `A-Z` => 0..25
          value = ch - 65
      } else if ch >= 97 && ch <= 122 {
          // `a-z` => 26..51
          value = ch - 97 + 26
      } else if ch >= 48 && ch <= 57 {
          // `0-9` => 52..61
          value = ch - 48 + 52
      } else if ch == 43 || ch == 45 {
          // `+` or `-` => 62
          value = 62
      } else if ch == 47 || ch == 95 {
          // `/` or `_` => 63
          value = 63
      } else if ch == 61 {
          // `=` => padding
          value = 0
      } else {
      }
      return value
  }
}

class TestClass {
  func testMethod() -> Void {
      let src1: [Int]? = [4, 5, 6]
      let decoder: ICustomDecoder? = XorByte(xorValue: 0xff)
      let dst1: [Int]? = decoder!.decode(src: src1)
      for x in dst1! {
          print(x)
      }
      
      print("|")
      
      let src2: [Int]? = [97, 71, 86, 115, 98, 71, 56, 61]
      let decoder2: Base64? = Base64()
      let dst2: [Int]? = decoder2!.decode(src: src2)
      for x in dst2! {
          print(x)
      }
  }
}

TestClass().testMethod()