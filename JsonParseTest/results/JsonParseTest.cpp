#include <one.hpp>
#include <stdexcept>
#include <vector>
#include <iostream>

int main()
{
    auto obj1 = OneJson::parse(string("{ \"a\":1, \"b\":2 }"));
    if (!obj1->isObject()) {
        throw std::runtime_error(string("expected to be object!"));
    }
    auto obj1_props = obj1->asObject()->getProperties();
    if (obj1_props->size() != 2) {
        throw std::runtime_error(string("expected 2 properties"));
    }
    if (obj1_props->at(0)->getName() != string("a")) {
        throw std::runtime_error(string("expected first property to be named 'a'"));
    }
    auto obj1_prop0_value = obj1_props->at(0)->getValue(obj1);
    if (!obj1_prop0_value->isNumber() || obj1_prop0_value->asNumber() != 1) {
        throw std::runtime_error(string("expected 'a' to be 1 (number)"));
    }
    cout << (string() + "b = " + to_string(obj1->asObject()->get(string("b"))->asNumber())) << endl;
    return 0;
}