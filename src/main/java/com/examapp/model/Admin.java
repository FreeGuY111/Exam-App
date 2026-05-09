package com.examapp.model;

import com.examapp.util.Constants;

/**
 * Admin user model with elevated privileges.
 */
public class Admin extends User {

    private static final long serialVersionUID = 1L;

    private int totalExamsCreated;
    private int totalStudentsManaged;

    public Admin() {
        super();
        setRole(Constants.ROLE_ADMIN);
    }

    public Admin(String username, String passwordHash, String fullName, String email) {
        super(username, passwordHash, fullName, email, Constants.ROLE_ADMIN);
        this.totalExamsCreated = 0;
        this.totalStudentsManaged = 0;
    }

    public int getTotalExamsCreated() {
        return totalExamsCreated;
    }

    public void setTotalExamsCreated(int totalExamsCreated) {
        this.totalExamsCreated = totalExamsCreated;
    }

    public void incrementExamsCreated() {
        this.totalExamsCreated++;
    }

    public int getTotalStudentsManaged() {
        return totalStudentsManaged;
    }

    public void setTotalStudentsManaged(int totalStudentsManaged) {
        this.totalStudentsManaged = totalStudentsManaged;
    }

    @Override
    public String toString() {
        return "Admin{username='" + getUsername() + "', examsCreated=" + totalExamsCreated + "}";
    }
}