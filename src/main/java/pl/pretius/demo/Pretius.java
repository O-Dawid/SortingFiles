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

class Pretius {
    private HashMap<Counters, Integer> counters = new HashMap<Counters, Integer>();
    private List<Folder> folders;
    private Path projectPath;
    private Path foldersPath;
    private Path homePath;

    Pretius() {
        init();
    }

    public void process() {
        while (true) {
            try {
                List<WatchEvent<?>> events = getFolderEvents(homePath);
                for (WatchEvent<?> event : events) {
                    if (isNewFile(event)) {
                        String eventFilename = getEventFilename(event);
                        String eventFileExtention = getFileExtention(eventFilename);
                        File eventFile = new File(FolderUtil.selectFileFromFolder(homePath, eventFilename));
                        int hour = getHour();

                        if (isJarOrXml(eventFileExtention)) {
                            increaseMapValue(counters, Counters.overall);
                            if (!isEven(hour)) {
                                moveFileToFolder(eventFile, Folder.TEST);
                                increaseMapValue(counters, Counters.test);
                            } else {
                                moveFileToFolder(eventFile, Folder.DEV);
                                increaseMapValue(counters, Counters.dev);
                            }
                        }
                        logMapToFile(counters, homePath, "count.txt");
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    private void init() {
        counters.put(Counters.overall, 0);
        counters.put(Counters.dev, 0);
        counters.put(Counters.test, 0);

        folders = Arrays.asList(Folder.values());
        projectPath = FolderUtil.getProjectFolder();
        foldersPath = FolderUtil.goBack(projectPath);
        homePath = FolderUtil.selectFolder(foldersPath, Folder.HOME);
        FolderUtil.createFolders(foldersPath, folders);
    }

    private void increaseMapValue(HashMap<Counters, Integer> map, Counters key) {
        map.replace(key, map.get(key) + 1);
    }

    private String getEventFilename(WatchEvent<?> event) {
        return event.context().toString();
    }

    private void moveFileToFolder(File file, Folder folder) {
        file.renameTo(new File(
                (Paths.get(file.getPath()).getParent()).getParent().toString() + "/" + folder + "/" + file.getName()));
    }

    private String getFileExtention(String fileName) {
        return FilenameUtils.getExtension(fileName);
    }

    private boolean isJarOrXml(String fileExtention) {
        return (fileExtention.contains("jar")) || (fileExtention.contains("xml"));
    }

    private List<WatchEvent<?>> getFolderEvents(Path path) throws IOException, InterruptedException {
        WatchService watcher = path.getFileSystem().newWatchService();
        path.register(watcher, StandardWatchEventKinds.ENTRY_CREATE);
        WatchKey watchKey = watcher.take();
        List<WatchEvent<?>> events = watchKey.pollEvents();
        return events;
    }

    private int getHour() {
        return LocalTime.now().getHour();
    }

    /**
     * @return a log as .txt file under Path
     */
    private void logMapToFile(HashMap<Counters, Integer> map, Path path, String filename) throws IOException {
        File registerTXT = new File(FolderUtil.selectFileFromFolder(path, filename));
        FileWriter writer = new FileWriter(registerTXT);

        for (Map.Entry<Counters, Integer> counter : map.entrySet()) {
            writer.write(counter.getKey() + " = " + counter.getValue() + " \n");
        }
        writer.close();
    }

    private boolean isNewFile(WatchEvent<?> event) {
        return event.kind() == StandardWatchEventKinds.ENTRY_CREATE;
    }

    private boolean isEven(int hour) {
        return hour % 2 == 0;
    }

}
