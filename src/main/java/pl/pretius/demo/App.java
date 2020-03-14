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
import java.util.List;

import org.apache.commons.io.FilenameUtils;

public class App 
{
    public static void main( String[] args ) throws IOException
    {   
        
        final String projectDir = System.getProperty("user.dir");
        //main folders
        List<String> folders = Arrays.asList("HOME","DEV","TEST");
        //counters    
        int overall = 0;
        int dev = 0;
        int test = 0;
        //path
        Path path = Paths.get(projectDir).getParent();
        Path homePath = Paths.get(path+"/HOME");
        //create dirs
        for (String folder : folders) {
            File dir = new File(path+"/"+folder);
            dir.mkdir();
        }

        while(true){    
            try {
                //watch HOME dir
                WatchService watcher = homePath.getFileSystem().newWatchService();
                //register NEW FILES events in HOME dir
                homePath.register(watcher, StandardWatchEventKinds.ENTRY_CREATE);
                System.out.println("Monitoring HOME DIR: ");
                // start listening
                WatchKey watchKey = watcher.take();
                // get list of events as they occur
                List<WatchEvent<?>> events = watchKey.pollEvents();
                //iterate over events
                for (WatchEvent event : events) {
                    File registerTXT = new File(homePath.toString()+"/count.txt");
                    FileWriter writer = new FileWriter(registerTXT);
                    //check if the event refers to a new file created
                    if (isNewFile(event)) {
                        String fileName = event.context().toString();
                        String fileExtention = FilenameUtils.getExtension(fileName);
                        File file = new File(homePath.toString()+"/"+fileName);
                        LocalTime time = LocalTime.now();
                        int hour = time.getHour();

                        if (fileExtention.contains("jar") && isEven(hour)){
                            overall++;
                            dev++;
                            file.renameTo(new File(path.toString()+"/DEV/"+fileName));
                        } else if (fileExtention.contains("jar") && !isEven(hour)){ 
                            file.renameTo(new File(path.toString()+"/TEST/"+fileName));     
                            overall++;
                            test++;         
                        } else if (fileExtention.contains("xml")){                       
                            file.renameTo(new File(path.toString()+"/DEV/"+fileName));   
                            overall++;
                            dev++;
                        }
                        writer.write("Overall = "+overall+" \n");
                        writer.write("/DEV/ = "+dev+"\n");
                        writer.write("/TEST/ = "+test+ "\n");
                        writer.close();          
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    private static boolean isNewFile(WatchEvent event) {
        return event.kind() == StandardWatchEventKinds.ENTRY_CREATE;
    }

    private static boolean isEven(int hour) {
        return hour%2==0;
    }
}
