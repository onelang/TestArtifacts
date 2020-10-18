import java.nio.file.Files;
import java.nio.file.Path;
import java.io.IOException;

public class OneFile {
    public static String readText(String fileName) {
        try {
            return Files.readString(Path.of(fileName));
        } catch(Exception e) {
            return null;
        }
    }

    public static void writeText(String fileName, String data) {
        try {
            Files.writeString(Path.of(fileName), data);
        } catch(Exception e) {
        }
    }

    public static String[] listFiles(String directory, Boolean recursive) {
        return null;
    }
}