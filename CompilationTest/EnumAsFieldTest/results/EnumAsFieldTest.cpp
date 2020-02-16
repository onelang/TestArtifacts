#include <iostream>

enum class SomeKind { EnumVal0, EnumVal1, EnumVal2 };
const char* SomeKindToStr[] = { "EnumVal0", "EnumVal1", "EnumVal2" };

class TestClass {
  public:
    SomeKind enum_field = SomeKind::EnumVal2;

    void testMethod() {
        std::cout << std::string("Value: ") + SomeKindToStr[(int)(this->enum_field)] << std::endl;
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