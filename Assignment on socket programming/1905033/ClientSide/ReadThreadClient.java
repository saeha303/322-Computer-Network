package ClientSide;

import util.NetworkUtil;

import java.io.*;
import java.util.Scanner;

public class ReadThreadClient implements Runnable {
    private Thread thr;
    private NetworkUtil networkUtil;
    private String name;
    private boolean duplicate;
    private int min_chunk_size=11*1024;//20971520
    private int max_chunk_size=64*1024;//41943040

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
                if(o instanceof String && ((String) o).startsWith(">>")){
                    System.out.println(o);
                }
                else if(o instanceof String && ((String) o).equalsIgnoreCase("duplicate user")){
                    throw new DuplicateUserException();
                }
                else if(o instanceof String && ((String) o).startsWith("c,")){
                    String[] tokens=((String) o).split(",");
                    String savePath = "src\\ClientSide\\folders\\"+name+"\\"+tokens[1]; // Local path to save the downloaded file
                    int length=Integer.parseInt(tokens[2]);
                    if(length>0){
                        try {
                            byte[] buffer = new byte[max_chunk_size];
                            int bytesRead=0;
                            InputStream inputStream=null;
                            BufferedOutputStream fileOutputStream =null;
                            try {
                                inputStream = networkUtil.getSocket().getInputStream();
                                fileOutputStream = new BufferedOutputStream(new FileOutputStream(savePath));
                                int sum=0;
                                System.out.println("Downloading file...");
                                while ((bytesRead = inputStream.read(buffer)) != -1) {
                                    fileOutputStream.write(buffer, 0, bytesRead);
//                                if(bytesRead<max_chunk_size){
//                                    break;
//                                }
                                    sum+=bytesRead;
                                    if(sum==length)
                                        break;
                                }
                                fileOutputStream.flush();
                                fileOutputStream.close();
                            }catch (Exception e){

                            }
                            BufferedReader reader=new BufferedReader(new InputStreamReader(networkUtil.getSocket().getInputStream()));
                            String completionMessage=reader.readLine();

                            System.out.println("Completion Message from server: " + completionMessage);
                            System.out.println("File downloaded successfully.");//doesn't work
                        } catch (Exception e) {
                            System.out.println("Failed to download the file: " + e.getMessage());
                        }
                    }
                    else {
                        System.out.println("File size is 0 bytes, nothing to download.");
                    }
                }
                else if(o instanceof String && ((String) o).startsWith("e,")){
                    String[] tokens=((String) o).split(",");
                    String savePath = "src/ClientSide/folders/"+name+"/"+tokens[2]; // Local path to save the downloaded file
                    int length=Integer.parseInt(tokens[3]);
                    if(length>0){
                        try {
                            // Get the input stream from the socket
                            InputStream inputStream = networkUtil.getSocket().getInputStream();
                            // Create a file output stream to save the downloaded file
                            FileOutputStream fileOutputStream = new FileOutputStream(savePath);
                            BufferedOutputStream bufferedOutputStream = new BufferedOutputStream(fileOutputStream);
                            // Read data from the input stream and write it to the output stream
                            byte[] buffer = new byte[max_chunk_size];
                            int bytesRead;
                            int sum=0;
                            System.out.println("Downloading file...");
                            while ((bytesRead = inputStream.read(buffer)) != -1) {

                                bufferedOutputStream.write(buffer, 0, bytesRead);

                                sum+=bytesRead;
                                if(sum==length)
                                    break;
                            }
                            bufferedOutputStream.flush();
                            fileOutputStream.close();
                            bufferedOutputStream.close();
                            // Close the streams and socket
                            BufferedReader reader=new BufferedReader(new InputStreamReader(networkUtil.getSocket().getInputStream()));
                            String completionMessage=reader.readLine();
                            System.out.println("Completion Message from server: " + completionMessage);
//                        inputStream.close();

                            System.out.println("File downloaded successfully.");//doesn't work
                        } catch (IOException e) {
                            System.out.println("Failed to download the file: " + e.getMessage());
                        }
                    }
                    else {
                        System.out.println("File size is 0 bytes, nothing to download.");
                    }
                }
                else if (o instanceof String && (((String) o).startsWith("h,"))) {//h. uploading a file, h,file_name,size,private/public
                    String[] tokens = ((String)o).split(",");
                    String filePath = "src/ClientSide/folders/" + name + "/" + tokens[1]; // Path of the file to be uploaded
                    try {
//                        if(tokens.length==4 || (tokens.length==5 && tokens[3].equalsIgnoreCase("public"))){
                            // Create a file input stream to read the file
                            File file = new File(filePath);
                            FileInputStream fileInputStream = new FileInputStream(file);
                            BufferedInputStream bufferedInputStream = new BufferedInputStream(fileInputStream);
                            // reading chunk size + file_id
                            Object line=networkUtil.read();
                            if(((String)line).startsWith(">>")){
                                System.out.println(line);
                            }else {
                                String[] tokens2=((String)line).split(",");
                                byte[] buffer = new byte[Integer.parseInt(tokens2[0])];
                                // Create an output stream to send data to the server
                                OutputStream outputStream = networkUtil.getSocket().getOutputStream();

                                // Read and send the file in chunks
                                int bytesRead;
                                boolean timeout=false;
                                System.out.println("Uploading file...");
                                while ((bytesRead = bufferedInputStream.read(buffer)) != -1) {
                                    // Write each chunk to the output stream
                                    outputStream.write(buffer, 0, bytesRead);
                                    long startTime = System.currentTimeMillis();
                                    Object ack=networkUtil.read();
                                    long endTime = System.currentTimeMillis();
                                    long delayInSeconds = (endTime - startTime) / 1000;
                                    if(((String)ack).equalsIgnoreCase("chunk received") && delayInSeconds>30){
                                        timeout=true;
                                        networkUtil.write("timed out");//timeout message
                                        break;
                                    }else if(((String)ack).equalsIgnoreCase("chunk received") && delayInSeconds<=30){
                                        networkUtil.write("not timed out");
//                                        System.out.println("chunk of "+bytesRead+" sent");
                                    }
                                }

                                if(!timeout){
                                    networkUtil.write("File uploaded successfully.");//completion message
                                }
                                bufferedInputStream.close();
                                Object msg=networkUtil.read();
                                if(((String)msg).equalsIgnoreCase("File saved successfully."))
                                    System.out.println("File uploaded successfully.");//works
                                else if(((String)msg).equalsIgnoreCase("File not saved.")){
                                    System.out.println("File upload error");
                                }else if(((String)msg).startsWith("File saved successfully.")){
                                    System.out.println("File uploaded successfully.");
                                    System.out.println("Provided request id is not valid, please upload the file once again with the correct id\nso that the person who requested can be notified.");
                                }
                            }

//                        }
//                        else {
//                            System.out.println("File upload doesn't meet criteria");
//                        }
                    } catch (FileNotFoundException e){
                        System.out.println("File not found, please give a valid file name.");
                    }
                    catch (IOException e) {
                        System.out.println("File upload error: " + e);
                    }
                }
            }
        } catch (DuplicateUserException e){
            System.out.println(e);
        }
        catch (Exception e) {
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



