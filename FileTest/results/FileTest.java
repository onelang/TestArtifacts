import java.nio.file.Paths;
import java.nio.file.Files;

class Program {
    public static void main(String[] args) throws Exception {
        String fileContent = new String(Files.readAllBytes(Paths.get("../../../input/test.txt")));;
        System.out.println(fileContent);
    }
}