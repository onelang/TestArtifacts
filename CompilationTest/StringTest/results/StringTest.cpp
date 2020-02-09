#include <one.hpp>
#include <vector>

class TestClass {
  public:
    std::string testMethod() {
        auto x = std::string("x");
        auto y = std::string("y");
        
        auto z = std::string("z");
        z += std::string("Z");
        z += x;
        
        auto a = std::string("abcdef").substr(2, 4 - 2);
        auto arr = OneStringHelper::split(std::string("ab  cd ef"), std::string(" "));
        
        return z + std::string("|") + x + y + std::string("|") + a + std::string("|") + arr->at(2);
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