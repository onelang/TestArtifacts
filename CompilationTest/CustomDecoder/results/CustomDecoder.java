import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

interface ICustomDecoder {
  List<Integer> decode(List<Integer> src) throws Exception;
}

class XorByte implements ICustomDecoder {
    public Integer xorValue;

    public XorByte(Integer xorValue) throws Exception {
        this.xorValue = xorValue;
    }

    public List<Integer> decode(List<Integer> src) throws Exception
    {
        List<Integer> dest = new ArrayList<Integer>(Arrays.asList());
        
        for (Integer i = 0; i < src.size(); i++) {
            dest.add(src.get(i) ^ this.xorValue);
        }
        
        return dest;
    }
}

class Base64 implements ICustomDecoder {
    public List<Integer> decode(List<Integer> src) throws Exception
    {
        List<Integer> dest = new ArrayList<Integer>(Arrays.asList());
        
        // 4 base64 chars => 3 bytes
        for (Integer i = 0; i < src.size(); i += 4) {
            Integer ch0 = this.decodeChar(src.get(i));
            Integer ch1 = this.decodeChar(src.get(i + 1));
            Integer ch2 = this.decodeChar(src.get(i + 2));
            Integer ch3 = this.decodeChar(src.get(i + 3));
            
            Integer trinity = (ch0 << 18) + (ch1 << 12) + (ch2 << 6) + (ch3);
            
            dest.add(trinity >> 16);
            dest.add((trinity >> 8) & 0xff);
            dest.add(trinity & 0xff);
        }
        
        return dest;
    }
    
    public Integer decodeChar(Integer ch) throws Exception
    {
        Integer value = -1;
        if (ch >= 65 && ch <= 90) {
            // `A-Z` => 0..25
            value = ch - 65;
        } else if (ch >= 97 && ch <= 122) {
            // `a-z` => 26..51
            value = ch - 97 + 26;
        } else if (ch >= 48 && ch <= 57) {
            // `0-9` => 52..61
            value = ch - 48 + 52;
        } else if (ch == 43 || ch == 45) {
            // `+` or `-` => 62
            value = 62;
        } else if (ch == 47 || ch == 95) {
            // `/` or `_` => 63
            value = 63;
        } else if (ch == 61) {
            // `=` => padding
            value = 0;
        } else {
        }
        return value;
    }
}

class TestClass {
    public void testMethod() throws Exception
    {
        List<Integer> src1 = new ArrayList<Integer>(Arrays.asList(4, 5, 6));
        ICustomDecoder decoder = new XorByte(0xff);
        List<Integer> dst1 = decoder.decode(src1);
        for (Integer x : dst1) {
            System.out.println(x);
        }
        
        System.out.println("|");
        
        List<Integer> src2 = new ArrayList<Integer>(Arrays.asList(97, 71, 86, 115, 98, 71, 56, 61));
        Base64 decoder2 = new Base64();
        List<Integer> dst2 = decoder2.decode(src2);
        for (Integer x : dst2) {
            System.out.println(x);
        }
    }
}

class Program {
    public static void main(String[] args) throws Exception {
        try {
            new TestClass().testMethod();
        } catch (Exception err) {
            System.out.println("Exception: " + err.getMessage());
        }
    }
}