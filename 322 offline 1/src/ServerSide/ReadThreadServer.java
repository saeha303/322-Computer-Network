package ServerSide;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

public class ReadThreadServer implements Runnable {
    private Thread thr;
    private ClientInfo client;
    public HashMap<String, ClientInfo> clientMap;
    private HashMap<String, ClientInfo> requests;
    private int bufferSize=500*1048576;//1MB,524288000
    private int min_chunk_size=11*1024;//20971520
    private int max_chunk_size=30*4096;//41943040

    public ReadThreadServer(HashMap<String, ClientInfo> map, HashMap<String,ClientInfo> req, String client) {
        this.clientMap = map;
        this.requests=req;
        this.client=clientMap.get(client);
        this.thr = new Thread(this);
        thr.start();
    }

    public void run() {
        try {
            while (true) {

                Object o = client.util.read();//far more complicated, chunk by chunk
                System.out.println(client.util);
                if(o instanceof String && o.equals("a")){
                    String s="* marked users are currently online\n";
                    for (Map.Entry<String, ClientInfo> entry : clientMap.entrySet()) {
                        String key = entry.getKey();
                        ClientInfo value = entry.getValue();
                        s+=key +" "+ (value.available?"*\n":"\n");

                    }
                    client.util.write(s);
                }
                else if(o instanceof String && o.equals("b")){
                    String directoryPath = client.folderPath;File directory = new File(directoryPath);

                    // Verify if the directory exists and is a directory
                    if (directory.exists() && directory.isDirectory()) {
                        // Get an array of File objects representing the files in the directory
                        File[] files = directory.listFiles();

                        // Iterate through the files and print their names
                        if (files != null) {
                            String s="public and private files are marked as + and -, respectively\n";
                            for (File file : files) {
                                if (file.isFile()) {
                                    System.out.println(file.canRead());
                                    System.out.println(file.canWrite());
                                    if(file.canRead() && file.canWrite())
                                        s+="+"+file.getName()+"\n";
                                    else
                                        s+="-"+file.getName()+"\n";
                                }
                            }
                            client.util.write(s);
                        }
                    } else {
                        System.out.println("Directory does not exist or is not a directory.");
                    }
                }
                else if(o instanceof String && (((String) o).startsWith("c,"))){

                    String[] tokens=((String) o).split(",");
                    String filePath = "src\\ServerSide\\folders\\"+client.name+"\\"+tokens[1]; // Path of the file to be sent to client

                    try {
                        File file = new File(filePath);
                        FileInputStream fileInputStream = new FileInputStream(file);
                        byte[] buffer = new byte[max_chunk_size];
                        int bytesRead;
                        try {
                            BufferedInputStream bufferedInputStream=new BufferedInputStream(fileInputStream);
                            client.util.write(o);
                            OutputStream outputStream = client.util.getSocket().getOutputStream();
                            while ((bytesRead = bufferedInputStream.read(buffer)) != -1) {
                                outputStream.write(buffer, 0, bytesRead);
                            }
                        }catch (Exception e){

                        }
                        Thread.sleep(50);
                        PrintWriter writer=new PrintWriter(client.util.getSocket().getOutputStream(),true);
                        writer.println("File sent successfully.");
                            System.out.println("in c");
                            System.out.println("File sent successfully.");//works
                    } catch (FileNotFoundException e){
                        client.util.write("File not found, please give a valid file name.");
                        System.out.println("File not found, please give a valid file name.");
                    } catch (IOException e) {
                        e.printStackTrace();
                        System.out.println("Server error: " + e.getMessage());
                    }
                }
                else if(o instanceof String && o.equals("d")){
                    String s="public and private files are marked as + and -, respectively\n";
                    for (Map.Entry<String, ClientInfo> client : clientMap.entrySet()) {
                        String key = client.getKey();
                        ClientInfo value = client.getValue();
                        if(!key.equals(this.client.name)){
                            s+=key+"\n";
                            String directoryPath = value.folderPath; // Specify the directory path

                            // Create a File object representing the directory
                            File directory = new File(directoryPath);

                            // Verify if the directory exists and is a directory
                            if (directory.exists() && directory.isDirectory()) {
                                // Get an array of File objects representing the files in the directory
                                File[] files = directory.listFiles();

                                // Iterate through the files and print their names
                                if (files != null) {

                                    for (File file : files) {
                                        if (file.isFile()) {
                                            if(file.canRead() && file.canWrite())
                                                s+="+"+file.getName()+"\n";
                                        }
                                    }
                                }
                            } else {
                                System.out.println("Directory does not exist or is not a directory.");
                            }
                        }

                    }
                    client.util.write(s);
                }
                else if(o instanceof String && (((String) o).startsWith("e,"))){//e,user name,file
                    System.out.println(o);
                    String[] tokens=((String) o).split(",");

                    try {
//                        while (true) {
                        // Create a file input stream to read the file
                        String filePath=clientMap.get(tokens[1]).folderPath+"/"+tokens[2];
                        System.out.println(0);
                        File file = new File(filePath);
                        System.out.println("1");
                        FileInputStream fileInputStream = new FileInputStream(file);
                        System.out.println(2);
                        client.util.write(o);
                        System.out.println(3);
                        // Get the output stream from the socket
                        OutputStream outputStream = client.util.getSocket().getOutputStream();

                        // Read data from the file input stream and write it to the output stream
                        byte[] buffer = new byte[max_chunk_size];
                        int bytesRead;
                        while ((bytesRead = fileInputStream.read(buffer)) != -1) {
                            outputStream.write(buffer, 0, bytesRead);
                        }
                        Thread.sleep(50);
                        PrintWriter writer=new PrintWriter(client.util.getSocket().getOutputStream(),true);
                        writer.println("File sent successfully.");
                        // Close the streams and socket
//                            outputStream.close();
                        fileInputStream.close();
                        System.out.println("in e");
                        System.out.println("File sent successfully.");//works
//                        }
                    } catch (NullPointerException e){
                        client.util.write("User not found, please give a valid user name.");
                    }
                    catch (FileNotFoundException e){
                        client.util.write("File not found, please give a valid file name.");
                        System.out.println("File not found, please give a valid file name.");
                    }
                    catch (IOException e) {
                        System.out.println("Server error: " + e.getMessage());
                    }
                }
                else if(o instanceof String && ((String) o).startsWith("f,")){//f,description
                    String[] tokens=((String) o).split(",");

                    int length = 10; // Length of the random string
                    String characters = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
                    Random random = new Random();
                    StringBuilder sb = new StringBuilder();

                    for (int i = 0; i < length; i++) {
                        int randomIndex = random.nextInt(characters.length());
                        char randomChar = characters.charAt(randomIndex);
                        sb.append(randomChar);
                    }
                    String request_id=sb.toString();
                    requests.put(request_id,client);
                    for (Map.Entry<String, ClientInfo> client : clientMap.entrySet()){
                        String key=client.getKey();
                        ClientInfo value=client.getValue();
                        if(!key.equals(this.client.name)){
                            String s="There is a file request from "+this.client.name+"\n"+tokens[1]+"\nThe request id is "+request_id+"\n";
                            if(value.available){
                                value.util.write("broadcast#"+s);
                            }else {
                                value.messages.add(s);
                            }

                        }
                    }
                }
                else if(o instanceof String && o.equals("g")){
                    ArrayList<String> list=client.messages;
                    String s="These messages will disappear right after you have seen them\n";
                    for(int i=0;i<list.size();i++){
                        s+="-";
                        s+=list.get(i);
                    }
                    client.util.write(s);
                    client.removeMessages();
                }
                else if(o instanceof String && ((String) o).startsWith("h,")){//h,file name, file size,public/private,request_id
                    System.out.println(o);
                    client.util.write(o);
                    String[] tokens=((String) o).split(",");
                    System.out.println(tokens.length);
                    String savePath = "src/ServerSide/folders/"+client.name+"/"+tokens[1]; // Local path to save the uploaded file
                    if(bufferSize>=Integer.parseInt(tokens[2]) && (tokens.length==4 || (tokens.length==5 && tokens[3].equals("public"))) && Integer.parseInt(tokens[2])>0){
                        Random random = new Random();
                        int chunkSize = random.nextInt(max_chunk_size - min_chunk_size + 1) + min_chunk_size;// Chunk size in bytes
                        int length=10;
                        String characters = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
                        StringBuilder sb = new StringBuilder();

                        for (int i = 0; i < length; i++) {
                            int randomIndex = random.nextInt(characters.length());
                            char randomChar = characters.charAt(randomIndex);
                            sb.append(randomChar);
                        }
                        String fileID=sb.toString();
                        client.util.write(chunkSize+","+fileID);
                        FileOutputStream fileOutputStream =null;
                        BufferedOutputStream bufferedOutputStream =null;
                        try {

//                        while (true) {

                            // Create a file output stream to save the uploaded file
                            fileOutputStream = new FileOutputStream(savePath);
                            bufferedOutputStream = new BufferedOutputStream(fileOutputStream);

                            // Create a buffer for reading chunks of data
                            byte[] buffer = new byte[chunkSize];

                            // Get the input stream from the socket
                            InputStream inputStream = client.util.getSocket().getInputStream();

                            // Read and save the file in chunks
                            int bytesRead;
                            boolean timeout=false;
                            int sum=0;
                            while ((bytesRead = inputStream.read(buffer)) != -1) {
                                // Write each chunk to the output stream
                                sum+=bytesRead;
                                System.out.println("plssss");
                                bufferedOutputStream.write(buffer, 0, bytesRead);
                                System.out.println("be");
//                                Thread.sleep(31000);
                                client.util.write("chunk received");
                                System.out.println("here");
                                System.out.println("pls");
                                Object msg=client.util.read();

                                if(msg.equals("timed out")){

                                    timeout=true;
                                    break;
                                }
                                if(bytesRead<chunkSize){
                                    break;
                                }
                                System.out.println("pls");
                            }
                            bufferedOutputStream.flush();
                            if(!timeout){
                                client.util.read();
                            }
                            // Close the streams and socket
                            bufferedOutputStream.close();
                            fileOutputStream.close();
//                            inputStream.close();
                            if(sum==Integer.parseInt(tokens[2]) && !timeout){
                                client.util.write("File saved successfully.");//doesn't work
                            }
                            else {
                                client.util.write("File not saved.");
                                Files.delete(Path.of(savePath));//works
                            }
                            if (tokens[3].equals("public")) {
                                File file = new File(savePath);
                                file.setReadable(true, false);
                                file.setWritable(true, false);
//                                System.out.println(file.canRead());
//                                System.out.println(file.canWrite());
                            } else if(tokens[3].equals("private") && tokens.length<5){
                                File file = new File(savePath);
                                file.setReadable(true, false);
                                file.setWritable(false, false);
//                                System.out.println(file.canRead());
//                                System.out.println(file.canWrite());
                            } else if(tokens[3].equals("private") && tokens.length==5){
                                System.out.println("Shared file cannot be private");
                            }
                            bufferSize-=Integer.parseInt(tokens[2]);
//                        }
                            if(tokens.length==5){
                                System.out.println("tokens 5 na?");
                                if(requests.containsKey(tokens[4])){
                                    System.out.println(requests.get(tokens[4]).name);
                                    requests.get(tokens[4]).messages.add("here is your requested file from "+client.name);
                                }
                            }
                        } catch (IOException e) {
                            bufferedOutputStream.close();
                            fileOutputStream.close();
                            Files.delete(Path.of(savePath));
                            System.out.println("Server error: " + e.getMessage());
                        }
                    }
                    else {
                        System.out.println("Something is wrong");
                    }
                }
//                if(o instanceof String && o.equals("i")){
//                    for (Map.Entry<String, ClientInfo> entry : requests.entrySet()){
//
//                    }
//                }
                else if(o instanceof String && o.equals("i")){
                    client.available=false;
                    clientMap.get(client.name).available=false;
//                    client.util.write("log out");
//                    client.util.closeConnection();
                    break;
                }
            }
        } catch (Exception e) {
            System.out.println("r u here");
            System.out.println(e);
        } finally {
            try {
                System.out.println("amar kothati furolo");
                client.util.closeConnection();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}



