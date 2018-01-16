#include <one.hpp>
#include <memory>
#include <fstream>
#include <vector>
#include <map>
#include <iostream>

class IPrinterBase {
  public:
    virtual int someBaseFunc() = 0;
};

class IPrinter: public IPrinterBase {
  public:
    virtual void printIt() = 0;
};

class BasePrinter: public IPrinter {
  public:
    virtual string getValue() {
        return string("Base");
    }
    
    void printIt() {
        cout << (string() + "BasePrinter: " + this->getValue()) << endl;
    }
    
    int someBaseFunc() {
        return 42;
    }

  private:
};

class ChildPrinter: public BasePrinter {
  public:
    string getValue() {
        return string("Child");
    }

  private:
};

class TestClass {
  public:
    IPrinter getPrinter(string name) {
        auto result = name == string("child") ? make_shared<ChildPrinter>() : make_shared<BasePrinter>();
        return result;
    }
    
    void testMethod() {
        auto base_printer = this->getPrinter(string("base"));
        auto child_printer = this->getPrinter(string("child"));
        base_printer->printIt();
        child_printer->printIt();
        cout << (string() + to_string(base_printer->someBaseFunc()) + " == " + to_string(child_printer->someBaseFunc())) << endl;
    }

  private:
};

int main()
{
    try {
        TestClass c;
        c.testMethod();
    } catch(std::exception& err) {
        cout << "Exception: " << err.what() << '\n';
    }
    return 0;
}