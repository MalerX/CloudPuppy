package lupa;

public interface INavigate {
    public String refresh();
    public void mkDir(String nameDir);
    public void back();
    public void joinDir(String nameDir);
    public void upDir();
    public void rmItem(String item);
    public void upload(String uploadFile, int remotePort);
    public int download();
}
