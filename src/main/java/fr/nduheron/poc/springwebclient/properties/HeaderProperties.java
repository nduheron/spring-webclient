package fr.nduheron.poc.springwebclient.properties;

import java.util.Collections;
import java.util.List;

public class HeaderProperties {
    private List<String> mdc = Collections.emptyList();
    private List<String> http = Collections.emptyList();

    public List<String> getMdc() {
        return mdc;
    }

    public void setMdc(List<String> mdc) {
        this.mdc = mdc;
    }

    public List<String> getHttp() {
        return http;
    }

    public void setHttp(List<String> http) {
        this.http = http;
    }
}
