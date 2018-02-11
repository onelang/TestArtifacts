import Foundation

let obj1: Any? = try? JSONSerialization.jsonObject(with: "{ \"a\":1, \"b\":2 }".data(using: .utf8)!)
if !((obj1 is [String: Any?])) {
    throw OneError.RuntimeError("expected to be object!")
}
let obj1Props: [String?]? = Array((obj1 as? [String: Any?])!.keys)
if obj1Props!.count != 2 {
    throw OneError.RuntimeError("expected 2 properties")
}
if obj1Props![0] != "a" {
    throw OneError.RuntimeError("expected first property to be named 'a'")
}
let obj1Prop0Value: Any? = (obj1 as! [String: Any?])[obj1Props![0]!]!
if !((obj1Prop0Value is Int)) || (obj1Prop0Value as! Int) != 1 {
    throw OneError.RuntimeError("expected 'a' to be 1 (number)")
}
print("b = \(((obj1 as? [String: Any?])!["b"] as! Int))")