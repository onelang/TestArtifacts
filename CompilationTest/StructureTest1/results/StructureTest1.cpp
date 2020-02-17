#include <OneLang-Core-v0.1/one.hpp>
#include <iostream>
#include <string>

template<typename T>
class MyList {
  public:
    one::vec<T> items;

  private:
};

class Item {
  public:
    int offset = 5;
    std::string str_test = std::string("test") + std::string("test2");
    std::string str_constr = std::string("constr");

    Item(std::string str_constr) {
        this->str_constr = str_constr;
    }

  private:
};

class Container {
  public:
    one::sp<MyList<Item>> item_list;
    one::sp<MyList<std::string>> string_list;

    void method0() {
    }
    
    std::string method1(std::string str) {
        return str;
    }

  private:
};

int main()
{
    std::cout << std::string("ok") << std::endl;
    return 0;
}