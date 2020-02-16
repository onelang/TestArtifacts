#include <iostream>
#include <string>

class TestClass {
  public:
    void testMethod() {
        auto op = std::string("x");
        std::cout << op.size() << std::endl;
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