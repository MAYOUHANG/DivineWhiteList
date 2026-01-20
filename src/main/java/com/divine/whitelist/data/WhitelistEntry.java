package com.divine.whitelist.data;

public class WhitelistEntry {
    private final String nameOriginal;
    private String qq;
    private String note;
    private String createdAt;
    private String createdBy;
    private String updatedAt;
    private String updatedBy;

    public WhitelistEntry(String nameOriginal, String qq, String note, String createdAt, String createdBy) {
        this.nameOriginal = nameOriginal;
        this.qq = qq;
        this.note = note;
        this.createdAt = createdAt;
        this.createdBy = createdBy;
        this.updatedAt = createdAt;
        this.updatedBy = createdBy;
    }

    public String getNameOriginal() {
        return nameOriginal;
    }

    public String getQq() {
        return qq;
    }

    public void setQq(String qq) {
        this.qq = qq;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }

    public String getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(String updatedAt) {
        this.updatedAt = updatedAt;
    }

    public String getUpdatedBy() {
        return updatedBy;
    }

    public void setUpdatedBy(String updatedBy) {
        this.updatedBy = updatedBy;
    }
}
