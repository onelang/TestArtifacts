#include <one.hpp>
#include <iostream>

class ConstructorTest {
  public:
    int field2;
    int field1;

    ConstructorTest(int field1) {
        this->field1 = field1;
        this->field2 = field1 * this->field1 * 5;
    }

  private:
};

class TestClass {
  public:
    void testMethod() {
        auto test = make_shared<ConstructorTest>(3);
        std::cout << test->field2 << std::endl;
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