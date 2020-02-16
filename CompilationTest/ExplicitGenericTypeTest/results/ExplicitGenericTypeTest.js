class TestClass {
  testMethod() {
    const result = ["y"];
    const map = {
      x: 5
    };
    const keys = Object.keys(map);
    console.log(result[0]);
    console.log(keys[0]);
  }
}

try {
  new TestClass().testMethod();
} catch(e) {
  console.log(`Exception: ${e.message}`);
}