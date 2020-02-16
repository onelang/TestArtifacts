#include <OneLang-Core-v0.1/one.hpp>
#include <any>
#include <map>
#include <string>

class TestClass {
  public:
    void testMethod() {
        auto map = one::make_shared_map<std::string, std::any>({
        });
        auto keys = OneMapHelper::keys(map);
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