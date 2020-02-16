#include <iostream>
#include <stdexcept>

class TestClass {
  public:
    int notThrows() {
        return 5;
    }
    
    void fThrows() {
        throw std::runtime_error(std::string("exception message"));
    }
    
    void testMethod() {
        std::cout << this->notThrows() << std::endl;
        this->fThrows();
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