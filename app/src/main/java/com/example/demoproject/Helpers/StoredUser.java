package com.example.demoproject.Helpers;

import android.os.Parcel;
import android.os.Parcelable;

public class StoredUser implements Parcelable {

    private String name, id, mail, department, phone, state;

    public StoredUser() {}

    public StoredUser(String name, String id, String mail, String department, String state, String phone) {
        this.name = name;
        this.id = id;
        this.mail = mail;
        this.department = department;
        this.state = state;
        this.phone = phone;
    }

    protected StoredUser(Parcel in) {
        name = in.readString();
        id = in.readString();
        mail = in.readString();
        department = in.readString();
        state = in.readString();
        phone = in.readString();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(name);
        dest.writeString(id);
        dest.writeString(mail);
        dest.writeString(department);
        dest.writeString(state);
        dest.writeString(phone);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<StoredUser> CREATOR = new Creator<StoredUser>() {
        @Override
        public StoredUser createFromParcel(Parcel in) {
            return new StoredUser(in);
        }

        @Override
        public StoredUser[] newArray(int size) {
            return new StoredUser[size];
        }
    };

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getMail() {
        return mail;
    }

    public void setMail(String mail) {
        this.mail = mail;
    }

    public String getDepartment() {
        return department;
    }

    public void setDepartment(String department) {
        this.department = department;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

}
