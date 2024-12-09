package ServerSide;

import util.NetworkUtil;

import java.util.ArrayList;

public class ClientInfo {
    String name;
    boolean available;
    String folderPath;
    NetworkUtil util;
    ArrayList<String>  messages=new ArrayList<>();
    int readTill;

    ClientInfo(String n, String f, NetworkUtil u){
        this.name=n;
        this.folderPath=f;
        this.util=u;
        this.available=true;
        this.readTill=-1;
    }
    void removeMessages(){
        messages.clear();
    }
    void markAllAsRead(){
        readTill=messages.size()-1;
    }
    @Override
    public String toString() {
        return "ClientInfo{" +
                "name='" + name + '\'' +
                ", available=" + available +
                ", folderPath='" + folderPath + '\'' +
                ", util=" + util +
                ", messages=" + messages +
                '}';
    }
}
