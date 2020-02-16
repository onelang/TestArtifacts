#include <iostream>
#include <string>

enum class TestEnum { Item1, Item2 };
const char* TestEnumToStr[] = { "Item1", "Item2" };

class TestClass {
  public:
    void testMethod() {
        auto enum_v = TestEnum::Item1;
        if (3 * 2 == 6) {
            enum_v = TestEnum::Item2;
        }
        
        auto check1 = enum_v == TestEnum::Item2 ? std::string("SUCCESS") : std::string("FAIL");
        auto check2 = enum_v == TestEnum::Item1 ? std::string("FAIL") : std::string("SUCCESS");
        
        std::cout << std::string("Item1: ") + TestEnumToStr[(int)(TestEnum::Item1)] + ", Item2: " + TestEnumToStr[(int)(enum_v)] + ", checks: " + check1 + " " + check2 << std::endl;
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