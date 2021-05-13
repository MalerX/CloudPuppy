package lupa;

import java.nio.file.Paths;

public interface INavigate {
    public String refresh();
    public void mkDir(String nameDir);
    public void back();
    public void joinDir(String nameDir);
    public void upDir();
    public void rmItem(String item);
    public byte[] sendFile(Paths file);
    public void receivedFile();
}
