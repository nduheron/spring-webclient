package fr.nduheron.poc.springwebclient.properties;

import java.time.Duration;

public class TimeoutProperties {
    private Duration read = Duration.ofSeconds(5);
    private Duration connection = Duration.ofMillis(500);

    public Duration getRead() {
        return read;
    }

    public void setRead(Duration read) {
        this.read = read;
    }

    public Duration getConnection() {
        return connection;
    }

    public void setConnection(Duration connection) {
        this.connection = connection;
    }
}
