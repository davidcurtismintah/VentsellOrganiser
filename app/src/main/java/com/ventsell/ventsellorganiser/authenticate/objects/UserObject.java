package com.ventsell.ventsellorganiser.authenticate.objects;

import java.io.Serializable;

public class UserObject implements Serializable {

    private String id;
    private String email;
    private String password;
    private String full_name;
    private String image;

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getFullName() {
        return full_name;
    }

    public void setFullName(String full_name) {
        this.full_name = full_name;
    }

    @Override
    public String toString() {
        return "UserObject{" +
                "id='" + id + '\'' +
                ", email='" + email + '\'' +
                ", password='" + password + '\'' +
                ", full_name='" + full_name + '\'' +
                ", image='" + image + '\'' +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        UserObject userObject = (UserObject) o;

        if (id != null ? !id.equals(userObject.id) : userObject.id != null) return false;
        if (email != null ? !email.equals(userObject.email) : userObject.email != null) return false;
        if (password != null ? !password.equals(userObject.password) : userObject.password != null)
            return false;
        if (full_name != null ? !full_name.equals(userObject.full_name) : userObject.full_name != null)
            return false;
        return image != null ? image.equals(userObject.image) : userObject.image == null;

    }

    @Override
    public int hashCode() {
        int result = id != null ? id.hashCode() : 0;
        result = 31 * result + (email != null ? email.hashCode() : 0);
        result = 31 * result + (password != null ? password.hashCode() : 0);
        result = 31 * result + (full_name != null ? full_name.hashCode() : 0);
        result = 31 * result + (image != null ? image.hashCode() : 0);
        return result;
    }
}
