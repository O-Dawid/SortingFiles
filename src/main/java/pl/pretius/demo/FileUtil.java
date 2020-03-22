package pl.pretius.demo;

import java.io.File;
import java.nio.file.Path;

import org.apache.commons.io.FilenameUtils;

class FileUtil {

    static Path getFilePath(File file) {
        return file.getAbsoluteFile().toPath();
    }

    static String getFileExtention(String fileName) {
        return FilenameUtils.getExtension(fileName);
    }

    static void moveFileToFolder(File file, Folder folder) {
        file.renameTo(new File((FolderUtil.goBack(FolderUtil.goBack(FileUtil.getFilePath(file))) + "/" + folder + "/"
                + file.getName())));
    }

}