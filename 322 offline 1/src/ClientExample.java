import java.io.*;
import java.net.Socket;

public class ClientExample {
    public static void main(String[] args) {
        String serverAddress = "127.0.0.1";
        int serverPort = 33333;

        try{
            Socket socket = new Socket(serverAddress, serverPort);
            // Receive the file from the server
//            receiveFile(socket, "src\\ClientSide\\folders\\test.txt");
            byte[] buffer = new byte[1024];
            int bytesRead;

            try  {
                InputStream socketInputStream = socket.getInputStream();
                BufferedOutputStream fileOutputStream = new BufferedOutputStream(new FileOutputStream("src\\ClientSide\\folders\\test.txt"));
                while ((bytesRead = socketInputStream.read(buffer)) != -1) {
                    System.out.println(bytesRead);
                    fileOutputStream.write(buffer, 0, bytesRead);
                    if(bytesRead<1024)
                        break;
                }
                fileOutputStream.flush();
            }catch (Exception e){

            }
            // Receive the completion message from the server
            BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
//            System.out.println(socket);
            String completionMessage = reader.readLine();
            System.out.println("Completion Message: " + completionMessage);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void receiveFile(Socket socket, String filePath) throws IOException {
        byte[] buffer = new byte[1024];
        int bytesRead;

        try (InputStream socketInputStream = socket.getInputStream();
             BufferedOutputStream fileOutputStream = new BufferedOutputStream(new FileOutputStream(filePath))) {

            while ((bytesRead = socketInputStream.read(buffer)) != -1) {
                System.out.println(bytesRead);
                fileOutputStream.write(buffer, 0, bytesRead);
            }
        }
    }
}

