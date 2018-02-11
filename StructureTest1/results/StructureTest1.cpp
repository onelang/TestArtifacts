#include <one.hpp>
template<typename T>
class List {
  public:
    vec<T> items;

  private:
};

class Item {
  public:
    int offset = 5;
    string str_test = string("test") + string("test2");
    string str_constr = string("constr");

    Item(string str_constr) {
        this->str_constr = str_constr;
    }

  private:
};

class Container {
  public:
    sp<List<Item>> item_list;
    sp<List<string>> string_list;

    void method0() {
    }
    
    string method1(string str) {
        return str;
    }

  private:
};