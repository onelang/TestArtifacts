interface ICustomDecoder {
  decode(src: OneArray);
}

class XorByte implements ICustomDecoder {
  xorValue: number;

  constructor(xorValue: number) {
      this.xorValue = xorValue;
  }

  decode(src: OneArray) {
    const dest = [];
    
    for (let i = 0; i < src.length; i++) {
        dest.push(src[i] ^ this.xorValue);
    }
    
    return dest;
  }
}

class Base64 implements ICustomDecoder {
  decode(src: OneArray) {
    const dest = [];
    
    // 4 base64 chars => 3 bytes
    for (let i = 0; i < src.length; i += 4) {
        const ch0 = this.decodeChar(src[i]);
        const ch1 = this.decodeChar(src[i + 1]);
        const ch2 = this.decodeChar(src[i + 2]);
        const ch3 = this.decodeChar(src[i + 3]);
        
        const trinity = (ch0 << 18) + (ch1 << 12) + (ch2 << 6) + (ch3);
        
        dest.push(trinity >> 16);
        dest.push((trinity >> 8) & 0xff);
        dest.push(trinity & 0xff);
    }
    
    return dest;
  }
  
  decodeChar(ch: number) {
    let value = -1;
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
  testMethod() {
    const src1 = [4, 5, 6];
    const decoder = new XorByte(0xff);
    const dst1 = decoder.decode(src1);
    for (const x of dst1) {
        console.log(x);
    }
    
    console.log("|");
    
    const src2 = [97, 71, 86, 115, 98, 71, 56, 61];
    const decoder2 = new Base64();
    const dst2 = decoder2.decode(src2);
    for (const x of dst2) {
        console.log(x);
    }
  }
}

try {
  new TestClass().testMethod();
} catch(e) {
  console.log(`Exception: ${e.message}`);
}