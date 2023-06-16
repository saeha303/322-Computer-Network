package ServerSide;//import java.io.*;
//import java.nio.file.*;
//public class ServerSide.just {
//    //Main() method start
//    public static void main(String args[]) {
//        try
//        {
//            Files.createDirectories(Paths.get("E:/CSE/hudai"));
//        }
//        catch(IOException e)
//        {
//            e.printStackTrace();
//        }
//    }
//}

import java.io.File;

public class just {
    public static void main(String[] args) {
        String folderPath = "src/ServerSide/folders/hudai"; // Specify the desired folder path
//        C:\Users\HP\IdeaProjects\322 offline 1\src\ServerSide\folders
        // Create a File object representing the folder
        File folder = new File(folderPath);

        // Check if the folder already exists
        if (!folder.exists()) {
            // Create the folder using mkdir() method
            boolean success = folder.mkdir();

            if (success) {
                System.out.println("Folder created successfully.");
            } else {
                System.out.println("Failed to create the folder.");
            }
        } else {
            System.out.println("The folder already exists.");
        }
    }
}
