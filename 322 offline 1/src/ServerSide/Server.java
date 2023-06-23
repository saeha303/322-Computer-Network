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
            String service="Services provided:\n"+"* Write 'L' to look up the list of clients connected to the server\n"
                    +"* Write 'l' to look up your list of uploaded files or the public files of other users\n"
                    +"* Write 'd' to download your files or the public files of other users\n"
                    +"* Write 'r' to make a file request\n"
                    +"* Write 'm' to read messages\n"
                    +"* Write 'u' to upload a file\n"
                    +"* Write 'o' to go offline\n";
            ClientInfo c=null;
            String clientName = (String) networkUtil.read();
            if(clients.contains(clientName)){
                if(!clientMap.get(clientName).available){
                    clientMap.get(clientName).available=true;
                    clientMap.get(clientName).util=networkUtil;
                    System.out.println(clientMap.get(clientName));
//                    c=new ClientInfo(clientName,"src/ServerSide/folders/"+clientName,networkUtil);
                    networkUtil.write(service);
                    new ReadThreadServer(clientMap, requests, clientName);
                }else {
                    networkUtil.write("duplicate user");
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
                new ReadThreadServer(clientMap, requests, clientName);
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
