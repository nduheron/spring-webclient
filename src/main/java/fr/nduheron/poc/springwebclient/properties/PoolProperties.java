package fr.nduheron.poc.springwebclient.properties;

import javax.validation.constraints.NotEmpty;

public class PoolProperties {
    @NotEmpty
    private String name;
    private Integer maxSize;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getMaxSize() {
        return maxSize;
    }

    public void setMaxSize(Integer maxSize) {
        this.maxSize = maxSize;
    }
}
