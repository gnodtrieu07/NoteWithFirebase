package vn.edu.tdtu.finalexam;

public class Note {
    String id;
    String title;
    String descrip;
    String createdTime;
    String imgPath;
    String videoPath;
    public Note() {

    }
    public Note(String id,String title, String descrip, String createdTime,String imgPath,String videoPath) {
        this.id = id;
        this.title = title;
        this.descrip = descrip;
        this.createdTime = createdTime;
        this.imgPath = imgPath;
        this.videoPath = videoPath;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescrip() {
        return descrip;
    }

    public void setDescrip(String descrip) {
        this.descrip = descrip;
    }

    public String getCreatedTime() {
        return createdTime;
    }

    public void setCreatedTime(String createdTime) {
        this.createdTime = createdTime;
    }

    public String getImgPath() {
        return imgPath;
    }

    public void setImgPath(String imgPath) {
        this.imgPath = imgPath;
    }

    public String getVideoPath() {
        return videoPath;
    }

    public void setVideoPath(String videoPath) {
        this.videoPath = videoPath;
    }
}
