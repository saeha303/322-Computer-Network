package ServerSide;

import ClientSide.Client;
import util.NetworkUtil;

import java.io.File;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;

public class Server {

    private ServerSocket serverSocket;
    public HashMap<String, ClientInfo> clientMap;
    public ArrayList<String> clients;
    public HashMap<String,ClientInfo> requests;
    Server() {
        clients=new ArrayList<>();
        clientMap = new HashMap<>();
        requests=new HashMap<>();
        try {
            serverSocket = new ServerSocket(33333);
            while (true) {
                Socket clientSocket = serverSocket.accept();
                serve(clientSocket);
            }

        } catch (Exception e) {
            System.out.println("Server starts:" + e);
        }
    }

    public void serve(Socket clientSocket){
        NetworkUtil networkUtil=null;
        try{
            networkUtil = new NetworkUtil(clientSocket);
            String service="Services provided:\n"+"a. Write 'a' to look up the list of clients connected to the server\n"
                    +"b. Write 'b' to look up your list of uploaded files, both private and public\n"
                    +"c. Write 'c,file_name' to download your files\n"
                    +"d. Write 'd' to look up the public files of other users\n"
                    +"e. Write 'e,user_name,file_name' to download the public files of other users\n"
                    +"f. Write 'f,description' to make a file request\n"
                    +"g. Write 'g' to read unread messages\n"
                    +"h. Write 'h,file_name,file_size,public/private,request_id(if any)' to upload a file\n"
//                    +"i. Write 'i' to see all the request id's\n"
                    +"i. Write 'i' to go offline\n";
            ClientInfo c=null;
            String clientName = (String) networkUtil.read();
            if(clients.contains(clientName)){
                if(!clientMap.get(clientName).available){
                    clientMap.get(clientName).available=true;
                    clientMap.get(clientName).util=networkUtil;
                    c=new ClientInfo(clientName,"src/ServerSide/folders/"+clientName,networkUtil);
                    networkUtil.write(service);
                    new ReadThreadServer(clientMap, requests, c);
                }else {
                    //write something to indicate connection is lost
                    networkUtil.closeConnection();
                }
            }else {
                System.out.println(clientName);
                clients.add(clientName);
                String path="src/ServerSide/folders/"+clientName;
                c=new ClientInfo(clientName,path,networkUtil);
                boolean flag=makeDirectory(clientName,path);
                clientMap.put(clientName, c);
                networkUtil.write(service);
                new ReadThreadServer(clientMap, requests, c);
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }
    public boolean makeDirectory(String clientName, String path){
        File folder = new File(path);
        if (!folder.exists()) {
            boolean success = folder.mkdir();

            if (success) {
                return true;
            } else {
                return false;
            }
        } else {
            return false;
        }
    }
    public static void main(String args[]) {
        Server server = new Server();
    }
}
