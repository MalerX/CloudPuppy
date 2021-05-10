package lupa;

import org.apache.log4j.Logger;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public class Navigator {
    private static final Logger log = Logger.getLogger(Navigator.class);
    public static final String DELIMETR = "Hz784kj'''wf8d3";        //TODO Подобрать делимитер получше.

    private final File root;
    private File currentDir;
    private File lastDir;

    public Navigator(String root) {
        this.root = new File(root).getAbsoluteFile();
        if (this.root.exists()) {
            log.info(String.format("User work directory %s already exists", root));
        } else if (this.root.mkdir())
            log.info(String.format("User work directory %s created.", root));
        this.currentDir = this.lastDir = this.root;
    }

    public String refresh() {
        List<File> filesInCurrDir = Arrays.asList(
                Objects.requireNonNull(currentDir.listFiles()));
        //        Сначала папки, затем файлы.
        filesInCurrDir.sort((o1, o2) -> {
            if (o1.isDirectory() && o2.isFile())
                return -1;
            if (o1.isFile() && o2.isDirectory())
                return 1;
            return 0;
        });

        StringBuilder result = new StringBuilder(currentDir.getPath()).append(DELIMETR);

        for (File o :
                filesInCurrDir) {
            result.append(o.getName()).append(DELIMETR);
        }
        return result.substring(0, result.length() - DELIMETR.length());
    }

    public void mkDir(String nameDir) {
        if (nameDir == null ||
                Files.exists(Paths.get(currentDir.getPath(), nameDir))) {
            log.info(String.format("Operation aborted or directory with name %s in %s already exists.",
                    nameDir, currentDir.getPath()));
            return;
        }
        try {
            Files.createDirectory(Paths.get(currentDir.getPath(), nameDir));
            log.info(String.format("Successfully create directory %s in %s .",
                    nameDir, currentDir.getPath()));
        } catch (IOException e) {
            log.error(String.format("Impossible create directory with name %s in %s",
                    nameDir, currentDir.getPath()));
        }
    }

    public void back() {
        if (lastDir.exists()) {
            File tmpFile = lastDir;
            lastDir = currentDir;
            currentDir = tmpFile;
            log.info(String.format("Directory change from %s to %s is done",
                    lastDir.getName(), currentDir.getName()));
        } else
            log.info("The requested directory does not exist or has been deleted.");
    }

    public void joinDir(String nameDir) {
        if (nameDir == null)
            return;
        File tmp = new File(Paths.get(currentDir.toString(), nameDir).toUri());
        if (tmp.isDirectory()) {
            lastDir = currentDir;
            currentDir = tmp;
            log.info(String.format("Directory change from %s to %s is done",
                    lastDir.getName(), currentDir.getName()));
        }
    }

    public void upDir() {
        if (currentDir.equals(root)) {
            log.info(String.format("%s is root directory. Moving higher is prohibited",
                    root.getName()));
            return;
        }
        lastDir = currentDir;
        currentDir = currentDir.getParentFile();
        log.info(String.format("Directory change from %s to %s is done",
                lastDir.getName(), currentDir.getName()));
    }

    public void rmItem(String item) {
        if (item == null)
            return;
        File rmFile = new File(Paths.get(currentDir.getPath(), item).toUri());
        if (rmFile.exists()) {
            if (rmFile.delete())
                log.info(String.format("Item %s remove", item));
        } else
            log.info(String.format("Removing item %s fail.", item));
    }
}
