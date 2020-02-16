#include <OneLang-Core-v0.1/one.hpp>
#include <iostream>
#include <map>
#include <string>

class TestClass {
  public:
    void testMethod() {
        int value = 1 + 2 * 3 - 4;
        auto map_ = one::make_shared_map<std::string, int>({
          { "a", 5 },
          { "b", 6 }
        });
        auto text = std::string("Hello world! value = ") + std::to_string(value) + ", map[a] = " + std::to_string(map_->operator[](std::string("a")));
        std::cout << text << std::endl;
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