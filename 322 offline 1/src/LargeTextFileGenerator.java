
import java.io.BufferedWriter;
        import java.io.FileWriter;
        import java.io.IOException;

public class LargeTextFileGenerator {
    public static void main(String[] args) {
        String filePath = "src\\ClientSide\\folders\\charu\\test7.txt";
        long fileSizeInBytes = (long) (350 * 1024 * 1024); // 500MB

        generateLargeTextFile(filePath, fileSizeInBytes);
    }

    private static void generateLargeTextFile(String filePath, long fileSizeInBytes) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(filePath))) {
            long bytesWritten = 0;
            String line = "Lorem ipsum dolor sit amet, consectetur adipiscing elit.";

            while (bytesWritten < fileSizeInBytes) {
                writer.write(line);
                writer.newLine();
                bytesWritten += line.length() + 2; // Account for line and newline characters
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
