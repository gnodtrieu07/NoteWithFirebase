package vn.edu.tdtu.finalexam;

public class User {
    public String fullname,phone,password;
    public boolean active;

    public User() {

    }
    public User(String fullname, String email, String password, boolean active) {
        this.fullname = fullname;
        this.phone = email;
        this.password = password;
        this.active = active;
    }

    public String getFullname() {
        return fullname;
    }

    public void setFullname(String fullname) {
        this.fullname = fullname;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
