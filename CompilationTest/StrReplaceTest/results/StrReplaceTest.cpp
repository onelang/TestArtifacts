#include <iostream>
#include <string>

class TestClass {
  public:
    void testMethod() {
        auto str = std::string("A x B x C x D");
        auto result = OneStringHelper::replace(str, std::string("x"), std::string("y"));
        std::cout << std::string("R: ") + result + ", O: " + str << std::endl;
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