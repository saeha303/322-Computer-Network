import ServerSide.ClientInfo;
import util.NetworkUtil;

import java.io.*;
public class ServerThread implements Runnable {
    private Thread thr;
    private NetworkUtil client;

    public ServerThread(NetworkUtil client) {
        this.client=client;
        this.thr = new Thread(this);
        thr.start();
    }

    public void run() {
        try {
            while (true) {

                System.out.println("first");
                Object o = client.read();//far more complicated, chunk by chunk

                if(o instanceof String && (((String) o).startsWith("c,"))){

                    String[] tokens=((String) o).split(",");
                    String filePath = "src\\ServerSide\\folders\\"+client+"\\"+tokens[1]; // Path of the file to be sent to client

                    try {
                        File file = new File(filePath);
                        FileInputStream fileInputStream = new FileInputStream(file);
                        byte[] buffer = new byte[4096];
                        int bytesRead;
                        try {
                            BufferedInputStream bufferedInputStream=new BufferedInputStream(fileInputStream);
                            client.write(o);
                            OutputStream outputStream = client.getSocket().getOutputStream();
                            while ((bytesRead = bufferedInputStream.read(buffer)) != -1) {
                                outputStream.write(buffer, 0, bytesRead);
                            }
                        }catch (Exception e){

                        }
                        PrintWriter writer=new PrintWriter(client.getSocket().getOutputStream(),true);
                        writer.println("File sent successfully.");
                        System.out.println("in c");
                        System.out.println("File sent successfully.");//works
                    } catch (FileNotFoundException e){
                        client.write("File not found, please give a valid file name.");
                        System.out.println("File not found, please give a valid file name.");
                    } catch (IOException e) {
                        e.printStackTrace();
                        System.out.println("Server error: " + e.getMessage());
                    }
                }

            }
        } catch (Exception e) {
            System.out.println("r u here");
            System.out.println(e);
        } finally {
            try {
                System.out.println("amar kothati furolo");
                client.closeConnection();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}



