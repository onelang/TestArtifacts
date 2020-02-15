import java.nio.file.Files;
import java.nio.file.Paths;

class Program {
    public static void main(String[] args) throws Exception {
        Files.write(Paths.get("test.txt"), "example content".getBytes());;
        String fileContent = new String(Files.readAllBytes(Paths.get("test.txt")));;
        System.out.println(fileContent);
    }
}