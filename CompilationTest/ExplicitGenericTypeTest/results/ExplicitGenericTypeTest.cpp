#include <one.hpp>
#include <iostream>
#include <map>
#include <{{libDir}}/one.hpp>

class TestClass {
  public:
    void testMethod() {
        auto result = std::make_shared<std::vector<std::string>>(std::initializer_list<std::string>{  });
        auto map = make_shared_map<std::string, int>({
          { "x", 5 }
        });
        auto keys = OneMapHelper::keys(map);
        std::cout << result << std::endl;
        std::cout << keys << std::endl;
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