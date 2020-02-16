#include <iostream>

class NumberUnaryIssue {
  public:
    void test(int num) {
        num--;
    }

  private:
};

int main()
{
    std::cout << std::string("ok") << std::endl;
    return 0;
}