package ClientSide;

import util.NetworkUtil;

import java.io.*;
import java.util.Scanner;

public class ReadThreadClient implements Runnable {
    private Thread thr;
    private NetworkUtil networkUtil;
    private String name;
    private boolean duplicate;
    private int min_chunk_size=1024;
    private int max_chunk_size=4096;

    public ReadThreadClient(NetworkUtil networkUtil,String name) {
        this.networkUtil = networkUtil;
        this.duplicate=false;
        this.name=name;
        this.thr = new Thread(this);
        thr.start();
    }

    public void run() {
        try {
            while (true) {
                Scanner in=new Scanner(System.in);
                Object o = networkUtil.read();///unread message read from server
                if(o instanceof String && (((String) o).startsWith("Services")||((String) o).startsWith("*")
                        ||((String) o).startsWith("User not found")||((String) o).startsWith("File not found")
                        ||((String) o).startsWith("public"))){
                    System.out.println(o);
                }
//                else if(o instanceof String && ((String) o).startsWith("User not found")){
//                    System.out.println(o);
//                }
//                else if(o instanceof String && ((String) o).startsWith("File not found")){
//                    System.out.println(o);
//                }
//                else if(o instanceof String && ((String) o).startsWith("public")){
//                    System.out.println(o);
//                }
                else if(o instanceof String && ((String) o).startsWith("broadcast#")){
                    String[] tokens=((String) o).split("#");
                    System.out.println("!!!!!!!!!!!YOU HAVE A NEW MESSAGE!!!!!!!!!!!");
                    System.out.println(tokens[1]);
                }
                else if(o instanceof String && ((String) o).equals("duplicate user")){
                    throw new DuplicateUserException();
                }
                else if(o instanceof String && ((String) o).startsWith("c,")){
                    String[] tokens=((String) o).split(",");
                    String savePath = "src\\ClientSide\\folders\\"+name+"\\"+tokens[1]; // Local path to save the downloaded file

                    try {
                        byte[] buffer = new byte[max_chunk_size];
                        int bytesRead=0;

                        try {
                            InputStream inputStream = networkUtil.getSocket().getInputStream();
                            BufferedOutputStream fileOutputStream = new BufferedOutputStream(new FileOutputStream(savePath));
                            while ((bytesRead = inputStream.read(buffer)) != -1) {
                                System.out.println("bytesread: "+bytesRead);
                                fileOutputStream.write(buffer, 0, bytesRead);
                                if(bytesRead<max_chunk_size){
                                    break;
                                }
                            }
                            fileOutputStream.flush();
                        }catch (Exception e){

                        }
                        BufferedReader reader=new BufferedReader(new InputStreamReader(networkUtil.getSocket().getInputStream()));
                        String completionMessage=reader.readLine();
                        System.out.println("Completion Message: " + completionMessage);
                        System.out.println("File downloaded successfully.");//doesn't work

                    } catch (Exception e) {
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
                        byte[] buffer = new byte[max_chunk_size];
                        int bytesRead;
                        while ((bytesRead = inputStream.read(buffer)) != -1) {

                            bufferedOutputStream.write(buffer, 0, bytesRead);
                            if(bytesRead<4096){
                                break;
                            }
                        }
                        bufferedOutputStream.flush();
                        // Close the streams and socket
                        BufferedReader reader=new BufferedReader(new InputStreamReader(networkUtil.getSocket().getInputStream()));
                        String completionMessage=reader.readLine();
                        System.out.println("Completion Message: " + completionMessage);
                        bufferedOutputStream.close();
                        fileOutputStream.close();
//                        inputStream.close();

                        System.out.println("File downloaded successfully.");//doesn't work
                    } catch (IOException e) {
                        System.out.println("Failed to download the file: " + e.getMessage());
                    }
                }
                else if (o instanceof String && (((String) o).startsWith("h,"))) {//h. uploading a file, h,file_name,size,private/public
                    String[] tokens = ((String)o).split(",");
                    String filePath = "src/ClientSide/folders/" + name + "/" + tokens[1]; // Path of the file to be uploaded
                    try {
                        if(tokens.length==4 || (tokens.length==5 && tokens[3].equals("public"))){
                            // Create a file input stream to read the file
                            File file = new File(filePath);
                            FileInputStream fileInputStream = new FileInputStream(file);
                            BufferedInputStream bufferedInputStream = new BufferedInputStream(fileInputStream);
                            // Create a buffer for reading chunks of data
                            Object line=networkUtil.read();
                            String[] tokens2=((String)line).split(",");
                            byte[] buffer = new byte[Integer.parseInt(tokens2[0])];
                            System.out.println("Required chunk size is "+tokens2[0]);
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
//                                    System.out.println("chunk of "+bytesRead+" sent");
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
                                System.out.println("File upload error");
                            }
                        }
                        else {
                            System.out.println("File upload doesn't meet criteria");
                        }
                    } catch (FileNotFoundException e){
                        System.out.println("File not found, please give a valid file name.");
                    }
                    catch (IOException e) {

                        System.out.println("File upload error: " + e);
                    }
                }
                else if(o instanceof String && ((String) o).startsWith("These messages")){
                    System.out.println(o);
                }
            }
        } catch (DuplicateUserException e){
            System.out.println(e);
        }
        catch (Exception e) {
//            System.out.println("accept");
//            e.printStackTrace();
//            if(this.duplicate){
//                System.out.println("A client is online with the same user name.\nPress 'o' to log out");
//            }
//
//            else
                System.out.println("User has logged out.");
        } finally {
            try {
//                System.out.println("thread shesh");
                networkUtil.closeConnection();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}



