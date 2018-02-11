package main

import "fmt"

type ICustomDecoder interface {
    Decode(src []int) []int 
}

type XorByte struct {
    XorValue int
}

func NewXorByte(xorValue int) *XorByte {
    this := new(XorByte)
    this.XorValue = xorValue
    return this
}

func (this *XorByte) Decode(src []int) []int {
    dest := []int{}
    
    for i := 0; i < len(src); i++ {
        dest = append(dest, src[i] ^ this.XorValue)
    }
    
    return dest
}

type Base64 struct {
}

func NewBase64() *Base64 {
    this := new(Base64)
    return this
}

func (this *Base64) Decode(src []int) []int {
    dest := []int{}
    
    // 4 base64 chars => 3 bytes
    for i := 0; i < len(src); i += 4 {
        ch0 := this.DecodeChar(src[i])
        ch1 := this.DecodeChar(src[i + 1])
        ch2 := this.DecodeChar(src[i + 2])
        ch3 := this.DecodeChar(src[i + 3])
        
        trinity := (ch0 << 18) + (ch1 << 12) + (ch2 << 6) + (ch3)
        
        dest = append(dest, trinity >> 16)
        dest = append(dest, (trinity >> 8) & 0xff)
        dest = append(dest, trinity & 0xff)
    }
    
    return dest
}

func (this *Base64) DecodeChar(ch int) int {
    value := -1
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

type TestClass struct {
}

func NewTestClass() *TestClass {
    this := new(TestClass)
    return this
}

func (this *TestClass) TestMethod() {
    src1 := []int{4, 5, 6}
    decoder := NewXorByte(0xff)
    dst1 := decoder.Decode(src1)
    for _, x := range dst1 {
        fmt.Println(x)
    }
    
    fmt.Println("|")
    
    src2 := []int{97, 71, 86, 115, 98, 71, 56, 61}
    decoder2 := NewBase64()
    dst2 := decoder2.Decode(src2)
    for _, x := range dst2 {
        fmt.Println(x)
    }
}

func main() {
    defer func() {
      if r := recover(); r != nil {
          fmt.Print("Exception: ", r)
      }
    }()

    c := NewTestClass()
    c.TestMethod();
}