#include <OneLang-Core-v0.1/one.hpp>

class TestClass {
  public:
    void methodTest(one::vec<std::string> method_param) {
    }
    
    void testMethod() {
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