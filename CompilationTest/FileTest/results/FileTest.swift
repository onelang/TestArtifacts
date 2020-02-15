import Foundation

_ = try! "example content".write(toFile: "test.txt", atomically: false, encoding: .utf8)
let fileContent = try! String(contentsOfFile: "test.txt", encoding: String.Encoding.utf8)
print(fileContent)