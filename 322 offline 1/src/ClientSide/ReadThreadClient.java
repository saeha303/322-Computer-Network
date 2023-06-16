package ClientSide;

import ServerSide.Message;
import util.NetworkUtil;

import java.io.*;
import java.util.Scanner;

public class ReadThreadClient implements Runnable {
    private Thread thr;
    private NetworkUtil networkUtil;
    private String name;
    private boolean available;

    public ReadThreadClient(NetworkUtil networkUtil,String name) {
        this.networkUtil = networkUtil;
        this.available=true;
        this.name=name;
        this.thr = new Thread(this);
        thr.start();
    }

    public void run() {
        try {
            while (true) {
                Scanner in=new Scanner(System.in);
                Object o = networkUtil.read();///unread message read from server
                if(o instanceof String && (((String) o).startsWith("Services")||((String) o).startsWith("*"))){
                    System.out.println("hi");
                    System.out.println(o);
                }
                else if(o instanceof String && ((String) o).startsWith("log out")){
                    this.available=false;
                }
                else if(o instanceof String && ((String) o).startsWith("public")){
                    System.out.println(o);
                }
                else if (o instanceof String && (((String) o).startsWith("h,"))) {//h. uploading a file, h,file_name,size,private/public
                    String[] tokens = ((String)o).split(",");
                    String filePath = "src/ClientSide/folders/" + name + "/" + tokens[1]; // Path of the file to be uploaded
                    int chunkSize = 1024;
                    try {

                        // Create a file input stream to read the file
                        File file = new File(filePath);
                        FileInputStream fileInputStream = new FileInputStream(file);
                        BufferedInputStream bufferedInputStream = new BufferedInputStream(fileInputStream);
                        // Create a buffer for reading chunks of data
                        Object line=networkUtil.read();
                        String[] tokens2=((String)line).split(",");
                        byte[] buffer = new byte[Integer.parseInt(tokens2[0])];

                        // Create an output stream to send data to the server
                        OutputStream outputStream = networkUtil.getSocket().getOutputStream();

                        // Read and send the file in chunks
                        int bytesRead;
                        boolean timeout=false;
                        while ((bytesRead = bufferedInputStream.read(buffer)) != -1) {
                            // Write each chunk to the output stream
                            outputStream.write(buffer, 0, bytesRead);
                            long startTime = System.currentTimeMillis();
                            Object ack=networkUtil.read();
                            long endTime = System.currentTimeMillis();
                            long delayInSeconds = (endTime - startTime) / 1000;
                            if(ack.equals("chunk received") && delayInSeconds>30){
                                timeout=true;
                                networkUtil.write("timed out");//timeout message
                                break;
                            }else if(ack.equals("chunk received") && delayInSeconds<=30){
                                networkUtil.write("not timed out");
                            }
                        }

                        if(!timeout){
                            networkUtil.write("File uploaded successfully.");//completion message
                        }
                        bufferedInputStream.close();
//                        outputStream.close();
                        Object msg=networkUtil.read();
                        if(msg.equals("File saved successfully."))
                            System.out.println("File uploaded successfully.");//works
                        else if(msg.equals("File not saved.")){
                            System.out.println("File upload error: ");
                        }
                    } catch (IOException e) {
                        System.out.println("File upload error: " + e.getMessage());
                    }

                }
                else if(o instanceof String && ((String) o).startsWith("c,")){
                    String[] tokens=((String) o).split(",");
                    String savePath = "src/ClientSide/folders/"+name+"/"+tokens[1]; // Local path to save the downloaded file

                    try {
                        System.out.println("omg5");
                        // Get the input stream from the socket
                        InputStream inputStream = networkUtil.getSocket().getInputStream();
                        System.out.println("omg1");
                        // Create a file output stream to save the downloaded file
                        FileOutputStream fileOutputStream = new FileOutputStream(savePath);
                        System.out.println("omg2");
                        BufferedOutputStream bufferedOutputStream = new BufferedOutputStream(fileOutputStream);
                        System.out.println("omg3");
                        // Read data from the input stream and write it to the output stream
                        byte[] buffer = new byte[4096];
                        int bytesRead;
                        while ((bytesRead = inputStream.read(buffer)) != -1) {

                            bufferedOutputStream.write(buffer, 0, bytesRead);
                            if(bytesRead<4096){
                                break;
                            }
                            System.out.println("loop");
                        }
                        bufferedOutputStream.flush();
                        System.out.println("omg4");
                        // Close the streams and socket
                        bufferedOutputStream.close();
                        fileOutputStream.close();
//                        inputStream.close();

                        System.out.println("File downloaded successfully.");//doesn't work
                    } catch (IOException e) {
                        System.out.println("Failed to download the file: " + e.getMessage());
                    }
                }
                else if(o instanceof String && ((String) o).startsWith("e,")){
                    String[] tokens=((String) o).split(",");
                    String savePath = "src/ClientSide/folders/"+name+"/"+tokens[2]; // Local path to save the downloaded file

                    try {
                        // Get the input stream from the socket
                        InputStream inputStream = networkUtil.getSocket().getInputStream();
                        // Create a file output stream to save the downloaded file
                        FileOutputStream fileOutputStream = new FileOutputStream(savePath);
                        BufferedOutputStream bufferedOutputStream = new BufferedOutputStream(fileOutputStream);
                        // Read data from the input stream and write it to the output stream
                        byte[] buffer = new byte[4096];
                        int bytesRead;
                        while ((bytesRead = inputStream.read(buffer)) != -1) {

                            bufferedOutputStream.write(buffer, 0, bytesRead);
                            if(bytesRead<4096){
                                break;
                            }
                        }
                        bufferedOutputStream.flush();
                        // Close the streams and socket
                        bufferedOutputStream.close();
                        fileOutputStream.close();
//                        inputStream.close();

                        System.out.println("File downloaded successfully.");//doesn't work
                    } catch (IOException e) {
                        System.out.println("Failed to download the file: " + e.getMessage());
                    }
                }
                if(o instanceof String && ((String) o).startsWith("These messages")){
                    System.out.println(o);
                }
            }
        } catch (Exception e) {
            System.out.println("accept");
            e.printStackTrace();
            System.out.println("A client is online with the same user name");
        } finally {
            try {
                System.out.println("thread shesh");
                networkUtil.closeConnection();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}



