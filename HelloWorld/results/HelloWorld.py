class TestClass:
    def test_method(self):
        value = 1 + 2 * 3 - 4
        map_ = {
          "a": 5,
          "b": 6,
        }
        text = "Hello world! value = %s, map[a] = %s" % (value, map_["a"], )
        print text

try:
    TestClass().test_method()
except Exception as err:
    print "Exception: " + err.message