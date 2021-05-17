package fr.nduheron.poc.springwebclient.properties;

import io.netty.channel.ConnectTimeoutException;
import io.netty.handler.timeout.ReadTimeoutException;
import org.springframework.http.HttpMethod;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class RetryProperties {
    private int count = 2;
    private List<HttpMethod> methods = Arrays.asList(HttpMethod.GET, HttpMethod.PUT, HttpMethod.DELETE);
    private List<? extends Class<? extends Exception>> exceptions = Arrays.asList(ConnectTimeoutException.class, ReadTimeoutException.class);

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }

    public List<HttpMethod> getMethods() {
        return methods;
    }

    public void setMethods(String[] methods) {
        this.methods = Arrays.stream(methods).map(HttpMethod::valueOf).collect(Collectors.toList());
    }

    public List<? extends Class<? extends Exception>> getExceptions() {
        return exceptions;
    }

    public void setExceptions(String[] exceptions) {
        this.exceptions = Arrays.stream(exceptions).map(this::toClass).collect(Collectors.toList());
    }

    private Class<? extends Exception> toClass(String className) {
        try {
            return (Class<? extends Exception>) Class.forName(className);
        } catch (ClassNotFoundException e) {
            throw new IllegalStateException(String.format("Class %s not found.", className), e);
        }
    }
}
