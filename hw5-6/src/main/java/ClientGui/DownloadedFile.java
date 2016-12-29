package ClientGui;

import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleLongProperty;
import javafx.beans.property.SimpleStringProperty;

/**
 * Created by kostya on 03.12.2016.
 */
public class DownloadedFile {
    private final SimpleIntegerProperty id;
    private final SimpleStringProperty name;
    private final SimpleDoubleProperty done;
    private final SimpleLongProperty size;
    private final SimpleStringProperty path;

    public DownloadedFile(int id, String name, double done, long size, String path) {
        this.id = new SimpleIntegerProperty(id);
        this.name = new SimpleStringProperty(name);
        this.done = new SimpleDoubleProperty(done);
        this.size = new SimpleLongProperty(size);
        this.path = new SimpleStringProperty(path);
    }

    public DownloadedFile(SimpleIntegerProperty id, SimpleStringProperty name, SimpleDoubleProperty done, SimpleLongProperty size, SimpleStringProperty path) {
        this.id = id;
        this.name = name;
        this.done = done;
        this.size = size;
        this.path = path;
    }

    public int getId() {
        return id.get();
    }

    public SimpleIntegerProperty idProperty() {
        return id;
    }

    public void setId(int id) {
        this.id.set(id);
    }

    public String getName() {
        return name.get();
    }

    public SimpleStringProperty nameProperty() {
        return name;
    }

    public void setName(String name) {
        this.name.set(name);
    }

    public double getDone() {
        return done.get();
    }

    public SimpleDoubleProperty doneProperty() {
        return done;
    }

    public void setDone(double done) {
        this.done.set(done);
    }

    public long getSize() {
        return size.get();
    }

    public SimpleLongProperty sizeProperty() {
        return size;
    }

    public void setSize(long size) {
        this.size.set(size);
    }

    public String getPath() {
        return path.get();
    }

    public SimpleStringProperty pathProperty() {
        return path;
    }

    public void setPath(String path) {
        this.path.set(path);
    }
}
