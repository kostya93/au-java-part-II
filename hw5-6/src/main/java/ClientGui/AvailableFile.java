package ClientGui;

import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleLongProperty;
import javafx.beans.property.SimpleStringProperty;

/**
 * Created by kostya on 03.12.2016.
 */
public class AvailableFile {
    private final SimpleIntegerProperty id;
    private final SimpleLongProperty size;
    private final SimpleStringProperty name;

    public AvailableFile(int id, long size, String name) {
        this.id = new SimpleIntegerProperty(id);
        this.size = new SimpleLongProperty(size);
        this.name = new SimpleStringProperty(name);
    }

    public AvailableFile(SimpleIntegerProperty id, SimpleLongProperty size, SimpleStringProperty name) {
        this.id = id;
        this.size = size;
        this.name = name;
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

    public long getSize() {
        return size.get();
    }

    public SimpleLongProperty sizeProperty() {
        return size;
    }

    public void setSize(long size) {
        this.size.set(size);
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
}
