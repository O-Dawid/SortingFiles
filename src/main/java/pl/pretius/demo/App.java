package pl.pretius.demo;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardWatchEventKinds;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.time.LocalTime;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.FilenameUtils;

public class App {
    public static void main(String[] args) throws IOException {
        HashMap<Counters, Integer> counters = new HashMap<Counters, Integer>();
        counters.put(Counters.overall, 0);
        counters.put(Counters.dev, 0);
        counters.put(Counters.test, 0);

        // Get Path to main folder
        Path projectPath = FolderUtil.getProjectFolder();
        // go back from main folder and set it as path
        Path foldersPath = FolderUtil.goBack(projectPath);
        FolderUtil.createFolders(foldersPath);
        Path homePath = FolderUtil.selectFolder(foldersPath, Folder.HOME);

        while (true) {
            try {
                List<WatchEvent<?>> events = getFolderEvents(homePath);
                // iterate over events
                for (WatchEvent<?> event : events) {
                    // check if the event refers to a new file created
                    if (isNewFile(event)) {
                        String fileName = getEventFilename(event);
                        String fileExtention = getFileExtention(fileName);
                        File file = new File(FolderUtil.selectFileFromFolder(homePath, fileName));
                        int hour = getHour();

                        if (isJarOrXml(fileExtention)) {
                            increaseMapValue(counters, Counters.overall);
                            if (!isEven(hour)) {
                                moveFileToFolder(file, Folder.TEST);
                                increaseMapValue(counters, Counters.test);
                            } else {
                                moveFileToFolder(file, Folder.DEV);
                                increaseMapValue(counters, Counters.dev);
                            }
                        }
                        log(counters, homePath, "count.txt");
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    private static Integer increaseMapValue(HashMap<Counters, Integer> map, Counters key) {
        return map.replace(key, map.get(key) + 1);
    }

    private static String getEventFilename(WatchEvent<?> event) {
        return event.context().toString();
    }

    private static void moveFileToFolder(File file, Folder folder) {
        file.renameTo(new File(
                (Paths.get(file.getPath()).getParent()).getParent().toString() + "/" + folder + "/" + file.getName()));
    }

    private static String getFileExtention(String fileName) {
        return FilenameUtils.getExtension(fileName);
    }

    private static boolean isJarOrXml(String fileExtention) {
        return (fileExtention.contains("jar")) || (fileExtention.contains("xml"));
    }

    private static List<WatchEvent<?>> getFolderEvents(Path path) throws IOException, InterruptedException {
        // watch HOME dir
        WatchService watcher = path.getFileSystem().newWatchService();
        // register NEW FILES events in HOME dir
        path.register(watcher, StandardWatchEventKinds.ENTRY_CREATE);
        System.out.println("Monitoring HOME DIR: ");
        // start listening
        WatchKey watchKey = watcher.take();
        // get list of events as they occur
        List<WatchEvent<?>> events = watchKey.pollEvents();
        return events;
    }

    private static int getHour() {
        return LocalTime.now().getHour();
    }

    private static void log(HashMap<Counters, Integer> map, Path path, String filename) throws IOException {
        File registerTXT = new File(FolderUtil.selectFileFromFolder(path, filename));
        FileWriter writer = new FileWriter(registerTXT);

        for (Map.Entry<Counters, Integer> counter : map.entrySet()) {
            writer.write(counter.getKey() + " = " + counter.getValue() + " \n");
        }
        writer.close();
    }

    private static boolean isNewFile(WatchEvent<?> event) {
        return event.kind() == StandardWatchEventKinds.ENTRY_CREATE;
    }

    private static boolean isEven(int hour) {
        return hour % 2 == 0;
    }
}
