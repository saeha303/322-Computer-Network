package ServerSide;

import java.io.Serializable;

public class Message implements Serializable {
    boolean read;
    String text;

    public Message(String t) {
        text=t;
        read=false;
    }
}