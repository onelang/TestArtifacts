const obj1 = JSON.parse("{ \"a\":1, \"b\":2 }");
if (!(typeof (obj1) === 'object' && !Array.isArray(obj1))) {
    throw new Error("expected to be object!");
}
const obj1Props = Object.keys(obj1);
if (obj1Props.length != 2) {
    throw new Error("expected 2 properties");
}
if (obj1Props[0] != "a") {
    throw new Error("expected first property to be named 'a'");
}
const obj1Prop0Value = obj1[obj1Props[0]];
if (!(typeof (obj1Prop0Value) === 'number') || obj1Prop0Value != 1) {
    throw new Error("expected 'a' to be 1 (number)");
}
console.log(`b = ${obj1["b"]}`);