package pl.pretius.demo;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

public class FolderUtil {

    static void createFolders(Path path, List<Folder> folders) {
        for (Folder folder : folders) {
            File dir = new File(path+"/"+folder);
            dir.mkdir();
        }
    }

    static Path getProjectFolder() {
        return Paths.get(System.getProperty("user.dir"));
    }

    static Path goBack(Path path) {
        return path.getParent();
    }

    static Path selectFolder(Path path, Folder folder) {
        return Paths.get(path + "/" + folder);
    }

    static String selectFileFromFolder(Path path, String fileName) {
        return path.toString()+"/"+fileName;
    }


    
}