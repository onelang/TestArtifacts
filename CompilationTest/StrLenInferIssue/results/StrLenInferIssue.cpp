#include <iostream>
#include <string>

class StrLenInferIssue {
  public:
    static int test(std::string str) {
        return str.size();
    }

  private:
};

int main()
{
    std::cout << StrLenInferIssue::test(std::string("hello")) << std::endl;
    return 0;
}