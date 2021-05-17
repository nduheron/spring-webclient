package fr.nduheron.poc.springwebclient.properties;

public class TimeoutProperties {
    private int read = 5000;
    private int connection = 500;

    public int getRead() {
        return read;
    }

    public void setRead(int read) {
        this.read = read;
    }

    public int getConnection() {
        return connection;
    }

    public void setConnection(int connection) {
        this.connection = connection;
    }
}
