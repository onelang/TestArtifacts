#include <OneLang-Core-v0.1/one.hpp>
#include <iostream>

class ICustomDecoder {
  public:
    virtual one::vec<int> decode(one::vec<int> src) = 0;
};

class XorByte : public ICustomDecoder {
  public:
    int xor_value;

    XorByte(int xor_value) {
        this->xor_value = xor_value;
    }

    one::vec<int> decode(one::vec<int> src) {
        auto dest = std::make_shared<std::vector<int>>(std::initializer_list<int>{  });
        
        for (int i = 0; i < src->size(); i++) {
            dest->push_back(src->at(i) ^ this->xor_value);
        }
        
        return dest;
    }

  private:
};

class Base64 : public ICustomDecoder {
  public:
    one::vec<int> decode(one::vec<int> src) {
        auto dest = std::make_shared<std::vector<int>>(std::initializer_list<int>{  });
        
        // 4 base64 chars => 3 bytes
        for (int i = 0; i < src->size(); i += 4) {
            int ch0 = this->decodeChar(src->at(i));
            int ch1 = this->decodeChar(src->at(i + 1));
            int ch2 = this->decodeChar(src->at(i + 2));
            int ch3 = this->decodeChar(src->at(i + 3));
            
            int trinity = (ch0 << 18) + (ch1 << 12) + (ch2 << 6) + (ch3);
            
            dest->push_back(trinity >> 16);
            dest->push_back((trinity >> 8) & 0xff);
            dest->push_back(trinity & 0xff);
        }
        
        return dest;
    }
    
    int decodeChar(int ch) {
        int value = -1;
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

  private:
};

class TestClass {
  public:
    void testMethod() {
        auto src1 = std::make_shared<std::vector<int>>(std::initializer_list<int>{ 4, 5, 6 });
        auto decoder = std::make_shared<XorByte>(0xff);
        auto dst1 = decoder->decode(src1);
        for (auto it = dst1->begin(); it != dst1->end(); ++it) {
            auto x = *it;
            std::cout << x << std::endl;
        }
        
        std::cout << std::string("|") << std::endl;
        
        auto src2 = std::make_shared<std::vector<int>>(std::initializer_list<int>{ 97, 71, 86, 115, 98, 71, 56, 61 });
        auto decoder2 = std::make_shared<Base64>();
        auto dst2 = decoder2->decode(src2);
        for (auto it = dst2->begin(); it != dst2->end(); ++it) {
            auto x = *it;
            std::cout << x << std::endl;
        }
    }

  private:
};

int main()
{
    try {
        TestClass c;
        c.testMethod();
    } catch(std::exception& err) {
        std::cout << "Exception: " << err.what() << '\n';
    }
    return 0;
}