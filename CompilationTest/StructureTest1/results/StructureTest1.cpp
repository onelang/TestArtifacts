template<typename T>
class List {
  public:
    vec<T> items;

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
    sp<List<Item>> item_list;
    sp<List<std::string>> string_list;

    void method0() {
    }
    
    std::string method1(std::string str) {
        return str;
    }

  private:
};