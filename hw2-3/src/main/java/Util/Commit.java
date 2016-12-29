package Util;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

/**
 * Created by kostya on 01.10.2016.
 */
public class Commit implements Serializable {
    private String message;
    private String uuid;
    private Set<String> files;
    private String date;
    private Commit parent;

    public Commit () {
        files = new HashSet<>();
    }

    public Commit getParent() {
        return parent;
    }

    public void setParent(Commit parent) {
        this.parent = parent;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public Set<String> getFiles() {
        return files;
    }

    public void setFiles(Set<String> files) {
        this.files = files;
    }

    public void addAllFiles(Set<String> files) {
        this.files.addAll(files);
    }

    public void addFile(String file) {
        this.files.add(file);
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    @Override
    public String toString() {
        return "Commit{" +
                "message='" + message + '\'' +
                ", uuid='" + uuid + '\'' +
                ", files=" + files +
                ", date='" + date + '\'' +
                ", parent=" + (parent != null ? parent.getUuid() : "null") +
                '}';
    }
}
