#include <one.hpp>
#include <iostream>

int main()
{
    auto file_content = OneFile::readText(string("../../../input/test.txt"));
    cout << (file_content) << endl;
    return 0;
}