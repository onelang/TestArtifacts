#include <OneLang-Core-v0.1/one.hpp>
#include <iostream>

class TestClass {
  public:
    void testMethod() {
        auto constant_arr = std::make_shared<std::vector<int>>(std::initializer_list<int>{ 5 });
        
        auto mutable_arr = std::make_shared<std::vector<int>>(std::initializer_list<int>{ 1 });
        mutable_arr->push_back(2);
        
        std::cout << std::string("len1: ") + std::to_string(constant_arr->size()) + ", len2: " + std::to_string(mutable_arr->size()) << std::endl;
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