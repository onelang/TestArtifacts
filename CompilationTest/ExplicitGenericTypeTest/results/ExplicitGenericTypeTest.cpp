#include <OneLang-Core-v0.1/one.hpp>
#include <iostream>
#include <map>
#include <string>

class TestClass {
  public:
    void testMethod() {
        auto result = std::make_shared<std::vector<std::string>>(std::initializer_list<std::string>{ std::string("y") });
        auto map = one::make_shared_map<std::string, int>({
          { "x", 5 }
        });
        auto keys = OneMapHelper::keys(map);
        std::cout << result->at(0) << std::endl;
        std::cout << keys->at(0) << std::endl;
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