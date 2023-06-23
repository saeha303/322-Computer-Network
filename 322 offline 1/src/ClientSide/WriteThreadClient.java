package ClientSide;

import util.NetworkUtil;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.util.Scanner;

public class WriteThreadClient implements Runnable {

    private Thread thr;
    private NetworkUtil networkUtil;
    String name;

    public WriteThreadClient(NetworkUtil networkUtil, String name) {
        this.networkUtil = networkUtil;
        this.name = name;
        this.thr = new Thread(this);
        thr.start();
    }

    public void run() {
        try {
            Scanner input = new Scanner(System.in);
            while (true) {
                System.out.print("Enter your choice: ");
                String text = input.nextLine();

                if(text.equals("L"))
                    networkUtil.write("a");//to upload file
                else if(text.equals("l")){
                    System.out.println("If you want to see your own file, press 'u'\n"
                    +"If you want to see other users public files, press 'o'");
                    text=input.nextLine();
                    if(text.equals("u"))
                        networkUtil.write("b");
                    else if(text.equals("o"))
                        networkUtil.write("d");
                }
                else if(text.equals("d")){
                    System.out.println("If you want to download your own file, press 'u'\n"
                            +"If you want to download other users public files, press 'o'");
                    String s="";
                    text=input.nextLine();
                    if(text.equals("u")){
                        s+="c,";
                        System.out.println("Give file name");
                        text=input.nextLine();
                        s+=text;

                    }

                    else if(text.equals("o")){
                        s+="e,";
                        System.out.println("Give the name of the user who owns the file");
                        text=input.nextLine();
                        s+=text+",";
                        System.out.println("Give file name");
                        text=input.nextLine();
                        s+=text;
                    }
                    networkUtil.write(s);
                }
                else if(text.equals("r")){
                    String s="f,";
                    System.out.println("Write a short file description");
                    text=input.nextLine();
                    s+=text;
                    networkUtil.write(s);
                }
                else if(text.equals("m")){
                    networkUtil.write("g");
                }
                else if(text.equals("u")){
                    String s="h,";
                    System.out.println("Write the file name");
                    text=input.nextLine();

                    s+=text+",";
                    String path = "src/ClientSide/folders/"+this.name+"/"+text; // Local path to save the uploaded file
                    System.out.println(path);

                    try{
                        long length= Files.size(Path.of(path));
                        s+=length+",";
                    }catch (NoSuchFileException e){
                        s+=0+",";//needs modification
                    }catch (Exception e){
                        e.printStackTrace();
                    }
                    System.out.println("Tell if the file will be private or public");
                    text=input.nextLine();
                    s+=text;
                    System.out.println("Is there a request id? Press 'y' or 'n'");
                    text=input.nextLine();
                    if(text.equals("y")){
                        text=input.nextLine();
                        s+=","+text;
                    }
                    networkUtil.write(s);
                }
                else if(text.equals("o")){
                    networkUtil.write("i");
                    break;
                }
            }
        } catch (Exception e) {
//            System.out.println("ber ho");
//            e.printStackTrace();
        } finally {
            try {
                networkUtil.closeConnection();
            } catch (IOException e) {
//                System.out.println("A client is online with the same user name");
//                e.printStackTrace();
            }
        }
    }
}



