import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;

public class ServerExample {
    public static void main(String[] args) {
        int port = 33333;

        try {
            ServerSocket serverSocket = new ServerSocket(port);
            System.out.println("Server listening on port " + port);

//            while (true) {
                Socket clientSocket = serverSocket.accept();
                System.out.println("Client connected: " + clientSocket.getInetAddress());

                // Send the file to the client
//                sendFile(clientSocket, "src\\ServerSide\\folders\\test.txt");
                File file = new File("src\\ServerSide\\folders\\test.txt");
                byte[] buffer = new byte[1024];
                int bytesRead;

                try{
                    BufferedInputStream fileInputStream = new BufferedInputStream(new FileInputStream(file));
                    OutputStream socketOutputStream = clientSocket.getOutputStream();
                    while ((bytesRead = fileInputStream.read(buffer)) != -1) {
                        socketOutputStream.write(buffer, 0, bytesRead);
                    }
                }catch (Exception e){

                }
                // Send completion message to the client
                PrintWriter writer = new PrintWriter(clientSocket.getOutputStream(), true);
//                System.out.println(clientSocket);
                writer.println("File downloaded successfully.");

                // Close the socket
//                clientSocket.close();
//            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void sendFile(Socket clientSocket, String filePath) throws IOException {
        File file = new File(filePath);
        byte[] buffer = new byte[1024];
        int bytesRead;

        try (BufferedInputStream fileInputStream = new BufferedInputStream(new FileInputStream(file));
             OutputStream socketOutputStream = clientSocket.getOutputStream()) {

            while ((bytesRead = fileInputStream.read(buffer)) != -1) {
                socketOutputStream.write(buffer, 0, bytesRead);
            }
        }
    }
}

