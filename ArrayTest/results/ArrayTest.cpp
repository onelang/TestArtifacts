#include <one.hpp>
#include <iostream>
#include <vector>

class TestClass {
  public:
    void testMethod() {
        auto constant_arr = make_shared<vector<int>>(initializer_list<int>{ 5 });
        
        auto mutable_arr = make_shared<vector<int>>(initializer_list<int>{ 1 });
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