package ClientSide;

import ServerSide.Message;
import util.NetworkUtil;

import java.io.*;
import java.util.Scanner;

public class WriteThreadClient implements Runnable {

    private Thread thr;
    private NetworkUtil networkUtil;
    String name;

    public WriteThreadClient(NetworkUtil networkUtil, String name) {
        this.networkUtil = networkUtil;
        this.name = name;
        this.thr = new Thread(this);
        thr.start();
    }

    public void run() {
        try {
            Scanner input = new Scanner(System.in);
            while (true) {
                System.out.print("Enter your choice: ");
                String text = input.nextLine();
                networkUtil.write(text);//to upload file
//                if (text.contains("h,")) {//h. uploading a file, h,file_name,size,private/public
//                    String[] tokens = text.split(",");
//                    String filePath = "src/ClientSide/folders/" + name + "/" + tokens[1]; // Path of the file to be uploaded
//                    int chunkSize = 1024;
//                    try {
//
//                        // Create a file input stream to read the file
//                        File file = new File(filePath);
//                        FileInputStream fileInputStream = new FileInputStream(file);
//                        BufferedInputStream bufferedInputStream = new BufferedInputStream(fileInputStream);
//                        // Create a buffer for reading chunks of data
//                        byte[] buffer = new byte[chunkSize];
//
//                        // Create an output stream to send data to the server
//                        OutputStream outputStream = networkUtil.getSocket().getOutputStream();
//
//                        // Read and send the file in chunks
//                        int bytesRead;
//                        while ((bytesRead = bufferedInputStream.read(buffer)) != -1) {
//                            // Write each chunk to the output stream
//                            outputStream.write(buffer, 0, bytesRead);
//                        }
//                        bufferedInputStream.close();
//                        outputStream.close();
//
//                        System.out.println("File uploaded successfully.");
//                    } catch (IOException e) {
//                        System.out.println("File upload error: " + e.getMessage());
//                    }
//
//                }
//                if(text.contains("c,")){
//                    String[] tokens=text.split(",");
//                    String savePath = "src/ClientSide/folders/"+name+"/"+tokens[1]; // Local path to save the downloaded file
//
//                    try {
//                        System.out.println("omg5");
//                        // Get the input stream from the socket
//                        InputStream inputStream = networkUtil.getSocket().getInputStream();
//                        System.out.println("omg1");
//                        // Create a file output stream to save the downloaded file
//                        FileOutputStream fileOutputStream = new FileOutputStream(savePath);
//                        System.out.println("omg2");
//                        BufferedOutputStream bufferedOutputStream = new BufferedOutputStream(fileOutputStream);
//                        System.out.println("omg3");
//                        // Read data from the input stream and write it to the output stream
//                        byte[] buffer = new byte[4096];
//                        int bytesRead;
//                        while ((bytesRead = inputStream.read(buffer)) != -1) {
//                            bufferedOutputStream.write(buffer, 0, bytesRead);
//                        }
//                        System.out.println("omg4");
//                        // Close the streams and socket
//                        bufferedOutputStream.close();
//                        fileOutputStream.close();
//                        inputStream.close();
//
//                        System.out.println("File downloaded successfully.");
//                    } catch (IOException e) {
//                        System.out.println("Failed to download the file: " + e.getMessage());
//                    }
//                }
//                else {
//                    networkUtil.write(text);//to upload file
//                }
            }
        } catch (Exception e) {
            System.out.println("A client is online with the same user name");
//            e.printStackTrace();
        } finally {
            try {
                networkUtil.closeConnection();
            } catch (IOException e) {
                System.out.println("A client is online with the same user name");
//                e.printStackTrace();
            }
        }
    }
}



