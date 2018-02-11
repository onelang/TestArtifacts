class TestClass:
    def get_result(self):
        map_obj = {
          "x": 5,
        }
        #let containsX = "x" in mapObj;
        #delete mapObj["x"];
        map_obj["x"] = 3
        return map_obj["x"]
    
    def test_method(self):
        print "Result = %s" % (self.get_result())

try:
    TestClass().test_method()
except Exception as err:
    print "Exception: " + err.message