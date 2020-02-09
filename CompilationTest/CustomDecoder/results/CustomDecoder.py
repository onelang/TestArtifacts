class XorByte:
    def __init__(self, xor_value):
        self.xor_value = xor_value

    def decode(self, src):
        dest = []
        
        i = 0
        while i < len(src):
            dest.append(src[i] ^ self.xor_value)
            i += 1
        
        return dest

class Base64:
    def decode(self, src):
        dest = []
        
        # 4 base64 chars => 3 bytes
        i = 0
        while i < len(src):
            ch0 = self.decode_char(src[i])
            ch1 = self.decode_char(src[i + 1])
            ch2 = self.decode_char(src[i + 2])
            ch3 = self.decode_char(src[i + 3])
            trinity = (ch0 << 18) + (ch1 << 12) + (ch2 << 6) + (ch3)
            dest.append(trinity >> 16)
            dest.append((trinity >> 8) & 0xff)
            dest.append(trinity & 0xff)
            i += 4
        
        return dest
    
    def decode_char(self, ch):
        value = -1
        if ch >= 65 and ch <= 90:
            value = ch - 65
        elif ch >= 97 and ch <= 122:
            value = ch - 97 + 26
        elif ch >= 48 and ch <= 57:
            value = ch - 48 + 52
        elif ch == 43 or ch == 45:
            value = 62
        elif ch == 47 or ch == 95:
            value = 63
        elif ch == 61:
            value = 0
        else:
            pass
        return value

class TestClass:
    def test_method(self):
        src1 = [4, 5, 6]
        decoder = XorByte(0xff)
        dst1 = decoder.decode(src1)
        for x in dst1:
            print x
        
        print "|"
        
        src2 = [97, 71, 86, 115, 98, 71, 56, 61]
        decoder2 = Base64()
        dst2 = decoder2.decode(src2)
        for x in dst2:
            print x

try:
    TestClass().test_method()
except Exception as err:
    print "Exception: " + err.message