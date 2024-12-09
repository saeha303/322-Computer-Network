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
    private int max_chunk_size=64*1024;//41943040

    public ReadThreadServer(HashMap<String, ClientInfo> map, HashMap<String,ClientInfo> req, String client) {
        this.clientMap = map;
        this.requests=req;
        this.client=clientMap.get(client);
        this.thr = new Thread(this);
        thr.start();
    }
    public String randomIdGenerator(){
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
        return sb.toString();
    }
    public int randomChunkGenerator(){
        Random random = new Random();
        int chunkSize = random.nextInt(max_chunk_size - min_chunk_size + 1) + min_chunk_size;// Chunk size in bytes

        return chunkSize;
    }
    public void run() {
        try {
            while (true) {

                Object o = client.util.read();//far more complicated, chunk by chunk
                if(o instanceof String && ((String)o).equalsIgnoreCase("a")){
                    String s=">>* marked users are currently online\n";
                    for (Map.Entry<String, ClientInfo> entry : clientMap.entrySet()) {
                        String key = entry.getKey();
                        ClientInfo value = entry.getValue();
                        s+=key +" "+ (value.available?"*\n":"\n");

                    }
                    client.util.write(s);
                }
                else if(o instanceof String && ((String)o).equalsIgnoreCase("b")){
                    String directoryPath = client.folderPath;File directory = new File(directoryPath);

                    // Verify if the directory exists and is a directory
                    if (directory.exists() && directory.isDirectory()) {
                        // Get an array of File objects representing the files in the directory
                        File[] files = directory.listFiles();

                        // Iterate through the files and print their names
                        if (files != null) {
                            String s=">>Public and private files are marked as + and -, respectively\n";
                            for (File file : files) {
                                if (file.isFile()) {
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
                        BufferedInputStream bufferedInputStream=new BufferedInputStream(fileInputStream);
                        long length=file.length();
                        if(length>0){
                            client.util.write(o+","+length);
                            byte[] buffer = new byte[max_chunk_size];
                            int bytesRead;

                            OutputStream outputStream = client.util.getSocket().getOutputStream();
                            while ((bytesRead = bufferedInputStream.read(buffer)) != -1) {
                                outputStream.write(buffer, 0, bytesRead);
                            }

                            Thread.sleep(50);
                            PrintWriter writer=new PrintWriter(client.util.getSocket().getOutputStream(),true);
                            writer.println("File sent successfully.");
                            System.out.println("File sent successfully.");//works
                        }
                        else {
                            System.out.println("File size is 0 bytes");
                        }
                        fileInputStream.close();
                        bufferedInputStream.close();
                    } catch (FileNotFoundException e){
                        client.util.write(">>File not found, please give a valid file name.");
                        System.out.println("File not found, please give a valid file name.");
                    } catch (Exception e) {
                        e.printStackTrace();
                        System.out.println("Server error: " + e.getMessage());
                    }
                }
                else if(o instanceof String && ((String)o).equalsIgnoreCase("d")){
                    String s=">>Public and private files are marked as + and -, respectively\n";
                    for (Map.Entry<String, ClientInfo> client : clientMap.entrySet()) {
                        String key = client.getKey();
                        ClientInfo value = client.getValue();
                        if(!key.equalsIgnoreCase(this.client.name)){
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
                    String[] tokens=((String) o).split(",");

                    try {
//                        while (true) {
                        // Create a file input stream to read the file
                        String filePath=clientMap.get(tokens[1]).folderPath+"/"+tokens[2];
                        File file = new File(filePath);
                        FileInputStream fileInputStream = new FileInputStream(file);
                        BufferedInputStream bufferedInputStream=new BufferedInputStream(fileInputStream);
                        long length=file.length();
                        if(tokens[1].equalsIgnoreCase(client.name)){
                            client.util.write(">>To download your own file, press 'd', then 'u'.");
                        }
                        else if(file.canRead() && !file.canWrite()){
                            client.util.write(">>This is a private file.");
                        }
                        else if(length==0){
                            client.util.write(">>File size is 0 bytes");
                        }
                        else if(length>0) {
                            client.util.write(o+","+file.length());
                            byte[] buffer = new byte[max_chunk_size];
                            int bytesRead;
                            // Get the output stream from the socket
                            OutputStream outputStream = client.util.getSocket().getOutputStream();

                            // Read data from the file input stream and write it to the output stream

                            while ((bytesRead = bufferedInputStream.read(buffer)) != -1) {
                                outputStream.write(buffer, 0, bytesRead);
                            }
                            Thread.sleep(50);
                            PrintWriter writer=new PrintWriter(client.util.getSocket().getOutputStream(),true);
                            writer.println("File sent successfully.");
                            System.out.println("File sent successfully.");//works
                        }
                        fileInputStream.close();
                        bufferedInputStream.close();
//                        }
                    } catch (NullPointerException e){
                        client.util.write(">>User not found, please give a valid user name.");
                    }
                    catch (FileNotFoundException e){
                        client.util.write(">>File not found, please give a valid file name.");
                        System.out.println("File not found, please give a valid file name.");
                    }
                    catch (IOException e) {
                        System.out.println("Server error: " + e.getMessage());
                    }
                }
                else if(o instanceof String && ((String) o).startsWith("f,")){//f,description
                    String[] tokens=((String) o).split(",");


                    String request_id=randomIdGenerator();
                    requests.put(request_id,client);
                    for (Map.Entry<String, ClientInfo> client : clientMap.entrySet()){
                        String key=client.getKey();
                        ClientInfo value=client.getValue();
                        if(!key.equalsIgnoreCase(this.client.name)){
                            String s="There is a file request from "+this.client.name+"\n"+tokens[1]+"\nThe request id is "+request_id+"\n";
                                value.messages.add(s);
//                            }

                        }
                    }
                }
                else if(o instanceof String && ((String)o).equalsIgnoreCase("g")){
                    ArrayList<String> list=client.messages;
                    String s=">>Unread messages are marked as **\n";
                    for(int i=0;i<list.size();i++){
                        if(i<=client.readTill){
                            s+="--";
                        }else
                            s+="**";
                        s+=list.get(i);
                    }
                    client.util.write(s);
//                    client.removeMessages();
                    client.markAllAsRead();

                }

                else if(o instanceof String && ((String) o).startsWith("h,")){//h,file name, file size,public/private,request_id
                    System.out.println(o);
                    client.util.write(o);
                    String[] tokens=((String) o).split(",");
                    String savePath = "src/ServerSide/folders/"+client.name+"/"+tokens[1]; // Local path to save the uploaded file
                    if(bufferSize>=Integer.parseInt(tokens[2]) && (tokens.length==4 || (tokens.length==5 && tokens[3].equalsIgnoreCase("public"))) && Integer.parseInt(tokens[2])>0){
                        int chunkSize=randomChunkGenerator();
                        String fileID=randomIdGenerator();
                        String t="";
                        if(tokens.length==5)
                            t=tokens[4];
                        FileInfo fileInfo=new FileInfo(fileID,tokens[1],Integer.parseInt(tokens[2]),t);
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
                                bufferedOutputStream.write(buffer, 0, bytesRead);

//                                Thread.sleep(1000);
                                client.util.write("chunk received");
                                System.out.println("chunk received");
                                Object msg=client.util.read();
                                if(((String)msg).equalsIgnoreCase("timed out")){

                                    timeout=true;
                                    break;
                                }
                                if(bytesRead<chunkSize){
                                    break;
                                }
                            }
                            bufferedOutputStream.flush();
                            if(!timeout){
                                System.out.println("Completion message from client: "+client.util.read());
                            }
                            // Close the streams and socket
                            bufferedOutputStream.close();
                            fileOutputStream.close();
//                            inputStream.close();
                            if(sum==Integer.parseInt(tokens[2]) && !timeout){//Integer.parseInt(tokens[2])
                                if (tokens[3].equalsIgnoreCase("public")) {
                                    File file = new File(savePath);
                                    file.setReadable(true, false);
                                    file.setWritable(true, false);
                                } else if(tokens[3].equalsIgnoreCase("private") && tokens.length<5){
                                    File file = new File(savePath);
                                    file.setReadable(true, false);
                                    file.setWritable(false, false);
                                }
                                bufferSize-=Integer.parseInt(tokens[2]);
                                String s="";
                                if(tokens.length==5){
                                    if(requests.containsKey(tokens[4])){
                                        requests.get(tokens[4]).messages.add(client.name+" has uploaded your requested file.\n");
                                    }else {
                                        s+="Provided request id is not valid, please upload the file once again with the correct id\nso that the person who requested can be notified.";
                                    }
                                }
                                client.util.write("File saved successfully."+s);//doesn't work
                            }
                            else {
                                client.util.write("File not saved.");
                                Files.delete(Path.of(savePath));//works after stream close
                            }

                        } catch (IOException e) {
                            bufferedOutputStream.close();
                            fileOutputStream.close();
                            Files.delete(Path.of(savePath));
                            System.out.println("Server error: " + e.getMessage());
                        }
                    }
                    else if(bufferSize<Integer.parseInt(tokens[2])){
                        client.util.write(">>File size has exceeded buffer size");
                        System.out.println("File size has exceeded buffer size");
                    }else if(tokens.length==5 && tokens[3].equalsIgnoreCase("private")){
                        client.util.write(">>File with request id cannot be uploaded as private.");
                        System.out.println("File with request id cannot be uploaded as private.");
                    }else if(Integer.parseInt(tokens[2])==0){
                        client.util.write(">>File size is 0 bytes.");
                        System.out.println("File size is 0 bytes.");
                    }
                }
                else if(o instanceof String && ((String)o).equalsIgnoreCase("i")){
                    client.available=false;
                    clientMap.get(client.name).available=false;
                    break;
                }
            }
        } catch (Exception e) {
            System.out.println(e);
        } finally {
            try {
                clientMap.get(client.name).available=false;
                client.util.closeConnection();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}



