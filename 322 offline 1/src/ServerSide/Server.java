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
        clients=new ArrayList<>();//hardcoded
//        clients.add("a");
//        clients.add("b");
//        clients.add("c");
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
            String service="Services provided:\n"+"a. Look up the list of clients connected to the server\n"
                    +"b. Look up your list of uploaded files, both private and public\n"
                    +"c. Download your files, to download write in this format: c,file_name\n"
                    +"d. Look up the public files of other users\n"
                    +"e. Download the public files of other users\n"
                    +"f. Make a file request\n"
                    +"g. Read unread messages\n"
                    +"h. Upload a file\n"
                    +"i. Go offline\n";
            ClientInfo c=null;
            String clientName = (String) networkUtil.read();
            if(clients.contains(clientName)){
                if(!clientMap.get(clientName).available){
                    System.out.println("at least here");
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
