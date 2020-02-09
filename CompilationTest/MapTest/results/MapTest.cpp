#include <one.hpp>
#include <iostream>
#include <map>
#include <{{libDir}}/one.hpp>

class TestClass {
  public:
    int getResult() {
        auto map_obj = make_shared_map<std::string, int>({
          { "x", 5 }
        });
        //let containsX = "x" in mapObj;
        //delete mapObj["x"];
        map_obj->operator[](std::string("x")) = 3;
        return map_obj->operator[](std::string("x"));
    }
    
    void testMethod() {
        std::cout << std::string("Result = ") + std::to_string(this->getResult()) << std::endl;
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