package Client;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;

public class InitializedFile {
    String name;
    String content;


    public InitializedFile(String name, File file) {
        this.name = name;
        this.content = convertToContent(file);
    }

    public InitializedFile(String name) {
        this.name = name;
    }


    private String convertToContent(File file) {
        try {
            String file_content = "";
            Scanner scanner = new Scanner(file);
            while (scanner.hasNextLine()){
                file_content = String.format("%s%s", file_content, scanner.nextLine());
            }
            scanner.close();
            return file_content;

        } catch (FileNotFoundException e) {
            System.out.println("Creating new file");
            return "No Content";
        }
    }
}
