class TestClass:
    def test_method(self):
        result = ["y"]
        map = {
          "x": 5,
        }
        keys = map.keys()
        print result[0]
        print keys[0]

try:
    TestClass().test_method()
except Exception as err:
    print "Exception: " + err.message