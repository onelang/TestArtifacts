class XorByte
  attr_accessor(:xor_value)

  def initialize(xor_value)
      self.xor_value = xor_value
  end

  def decode(src)
      dest = []
      
      i = 0
      while i < src.length
          dest << (src[i] ^ self.xor_value)
          i += 1
      end
      
      return dest
  end
end

class Base64
  def decode(src)
      dest = []
      
      # 4 base64 chars => 3 bytes
      i = 0
      while i < src.length
          ch0 = self.decode_char(src[i])
          ch1 = self.decode_char(src[i + 1])
          ch2 = self.decode_char(src[i + 2])
          ch3 = self.decode_char(src[i + 3])
          trinity = (ch0 << 18) + (ch1 << 12) + (ch2 << 6) + (ch3)
          dest << (trinity >> 16)
          dest << ((trinity >> 8) & 0xff)
          dest << (trinity & 0xff)
          i += 4
      end
      
      return dest
  end

  def decode_char(ch)
      value = -1
      if ch >= 65 && ch <= 90
          value = ch - 65
      elsif ch >= 97 && ch <= 122
          value = ch - 97 + 26
      elsif ch >= 48 && ch <= 57
          value = ch - 48 + 52
      elsif ch == 43 || ch == 45
          value = 62
      elsif ch == 47 || ch == 95
          value = 63
      elsif ch == 61
          value = 0
      else
      end
      return value
  end
end

class TestClass
  def test_method()
      src1 = [4, 5, 6]
      decoder = XorByte.new(0xff)
      dst1 = decoder.decode(src1)
      for x in dst1
          puts x
      end
      
      puts "|"
      
      src2 = [97, 71, 86, 115, 98, 71, 56, 61]
      decoder2 = Base64.new()
      dst2 = decoder2.decode(src2)
      for x in dst2
          puts x
      end
  end
end

begin
    TestClass.new().test_method()
rescue Exception => err
    puts "Exception: #{err}"
end