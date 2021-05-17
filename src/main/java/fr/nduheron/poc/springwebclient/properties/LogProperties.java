package fr.nduheron.poc.springwebclient.properties;

import org.springframework.http.HttpHeaders;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class LogProperties {
    private boolean enable = true;
    private List<String> obfuscateHeaders = Collections.singletonList(HttpHeaders.AUTHORIZATION);

    public boolean isEnable() {
        return enable;
    }

    public void setEnable(boolean enable) {
        this.enable = enable;
    }

    public List<String> getObfuscateHeaders() {
        return obfuscateHeaders;
    }

    public void setObfuscateHeaders(String[] obfuscateHeaders) {
        this.obfuscateHeaders = Arrays.asList(obfuscateHeaders);
    }

}
