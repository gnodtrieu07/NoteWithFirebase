package vn.edu.tdtu.finalexam;

public class NoteRecycle {
    String id;
    String title;
    String descrip;
    String createdTime;
    String imgPath;
    String videoPath;
    String addingTime;

    public NoteRecycle() {

    }
    public NoteRecycle(String id,String title, String descrip, String createdTime,String imgPath,String videoPath,String addingTime) {
        this.id = id;
        this.title = title;
        this.descrip = descrip;
        this.createdTime = createdTime;
        this.imgPath = imgPath;
        this.addingTime = addingTime;
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

    public String getAddingTime() {
        return addingTime;
    }

    public void setAddingTime(String addingTime) {
        this.addingTime = addingTime;
    }
}
