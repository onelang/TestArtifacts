#include <one.hpp>
#include <iostream>

class TestClass {
  public:
    void testMethod() {
        auto str = std::string("ABCDEF");
        auto t_a0_true = str.compare(0, std::string("A").size(), std::string("A")) == 0;
        auto t_a1_false = str.compare(1, std::string("A").size(), std::string("A")) == 0;
        auto t_b1_true = str.compare(1, std::string("B").size(), std::string("B")) == 0;
        auto t_c_d2_true = str.compare(2, std::string("CD").size(), std::string("CD")) == 0;
        std::cout << std::string((t_a0_true ? "true" : "false")) + " " + (t_a1_false ? "true" : "false") + " " + (t_b1_true ? "true" : "false") + " " + (t_c_d2_true ? "true" : "false") << std::endl;
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