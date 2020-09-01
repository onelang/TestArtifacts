import sys
import os

sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)) + "/onepkg")

from OneLang.Generator.CsharpGenerator import CsharpGenerator
from OneLang.Test.SelfTestRunner import SelfTestRunner

SelfTestRunner("../../../../../").run_test(CsharpGenerator())
