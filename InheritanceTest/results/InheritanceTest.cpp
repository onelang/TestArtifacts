#include <one.hpp>
#include <iostream>

class IPrinterBase {
  public:
    virtual int someBaseFunc() = 0;
};

class IPrinter : public IPrinterBase {
  public:
    virtual void printIt() = 0;
};

class BasePrinter : public IPrinter {
  public:
    int num_value = 42;

    virtual string getValue() {
        return string("Base");
    }
    
    void printIt() {
        cout << (string() + "BasePrinter: " + this->getValue()) << endl;
    }
    
    int someBaseFunc() {
        return this->num_value;
    }

  private:
};

class ChildPrinter : public BasePrinter {
  public:
    string getValue() {
        return string("Child");
    }

  private:
};

class TestClass {
  public:
    sp<IPrinter> getPrinter(string name) {
        auto result = name == string("child") ? make_shared<ChildPrinter>() : make_shared<BasePrinter>();
        return result;
    }
    
    void testMethod() {
        auto base_printer = this->getPrinter(string("base"));
        auto child_printer = this->getPrinter(string("child"));
        base_printer->printIt();
        child_printer->printIt();
        cout << (string() + to_string(base_printer->someBaseFunc()) + " == " + to_string(child_printer->someBaseFunc())) << endl;
        
        auto base_p2 = make_shared<BasePrinter>();
        auto child_p2 = make_shared<ChildPrinter>();
        cout << (string() + to_string(base_p2->num_value) + " == " + to_string(child_p2->num_value)) << endl;
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