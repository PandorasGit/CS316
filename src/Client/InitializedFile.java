package Client;

import java.io.File;

public class InitializedFile {
    String name;
    File file;


    public InitializedFile(String name, String file) {
        this.name = name;
        this.file = convertToFile(file);
    }

    public InitializedFile(String name, File file) {
        this.name = name;
        this.file = file;
    }

    private File convertToFile(String file){
        return new File(file);
    }
}
