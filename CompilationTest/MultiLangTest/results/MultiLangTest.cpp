#include <OneLang-Core-v0.1/one.hpp>
#include <iostream>
#include <map>

class Calculator {
  public:
    int factor(int n) {
        if (n <= 1) {
            return 1;
        } else {
            return this->factor(n - 1) * n;
        }
    }

  private:
};

int main()
{
    std::cout << std::string("Hello!") << std::endl;
    
    auto arr = std::make_shared<std::vector<int>>(std::initializer_list<int>{ 1, 2, 3 });
    arr->push_back(4);
    
    std::cout << std::string("n = ") + std::to_string(arr->size()) + ", arr[0] = " + std::to_string(arr->at(0)) << std::endl;
    
    auto map = one::make_shared_map<std::string, int>({
      { "a", 2 },
      { "b", 3 }
    });
    std::cout << std::string("map['a'] = ") + std::to_string(map->operator[](std::string("a"))) + ", arr[1] = " + std::to_string(arr->at(1)) << std::endl;
    
    if (arr->at(0) == 1) {
        std::cout << std::string("TRUE-X") << std::endl;
    } else {
        std::cout << std::string("FALSE") << std::endl;
    }
    
    int sum = 0;
    for (int i = 0; i < 10; i++) {
        sum += i + 2;
    }
    std::cout << sum << std::endl;
    
    auto calc = std::make_shared<Calculator>();
    std::cout << std::string("5! = ") + std::to_string(calc->factor(5)) << std::endl;
    return 0;
}