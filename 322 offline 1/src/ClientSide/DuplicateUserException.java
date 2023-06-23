package ClientSide;

public class DuplicateUserException extends Exception{

    DuplicateUserException() {

    }
    @Override
    public String toString() {
        return "A client is online with the same user name.\nPress 'o' to log out";
    }
}
