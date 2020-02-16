#include <iostream>

class TestClass {
  public:
    bool getResult() {
        return true;
    }
    
    void testMethod() {
        std::cout << (this->getResult() ? std::string("true") : std::string("false")) << std::endl;
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