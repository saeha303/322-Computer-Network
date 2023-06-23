package ServerSide;

import util.NetworkUtil;

import java.util.ArrayList;

public class ClientInfo {
    String name;
    boolean available;
    String folderPath;
    NetworkUtil util;
    ArrayList<String>  messages=new ArrayList<>();


    ClientInfo(String n, String f, NetworkUtil u){
        this.name=n;
        this.folderPath=f;
        this.util=u;
        this.available=true;
    }
    void removeMessages(){
        messages.clear();
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
