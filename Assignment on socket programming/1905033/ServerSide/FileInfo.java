package ServerSide;

public class FileInfo {
    private String fileId;
    private String fileName;
    private int fileSize;
    private String requestId;
    FileInfo(String id, String name, int size,String reqId){
        fileId=id;
        fileName=name;
        fileSize=size;
        requestId=reqId;
    }
}
