package fr.nduheron.poc.springwebclient.properties;

import javax.validation.constraints.NotEmpty;

public class WebClientProperties {

    private TimeoutProperties timeout = new TimeoutProperties();
    private RetryProperties retry = new RetryProperties();
    private PoolProperties pool;
    private int bufferSize = 256;
    private boolean keepAlive;
    private boolean compress = true;
    @NotEmpty
    private String baseUrl;
    private LogProperties log = new LogProperties();
    private HeaderProperties headers = new HeaderProperties();

    public TimeoutProperties getTimeout() {
        return timeout;
    }

    public void setTimeout(TimeoutProperties timeout) {
        this.timeout = timeout;
    }


    public RetryProperties getRetry() {
        return retry;
    }

    public void setRetry(RetryProperties retry) {
        this.retry = retry;
    }

    public int getBufferSize() {
        return bufferSize;
    }

    public void setBufferSize(int bufferSize) {
        this.bufferSize = bufferSize;
    }


    public boolean isKeepAlive() {
        return keepAlive;
    }

    public void setKeepAlive(boolean keepAlive) {
        this.keepAlive = keepAlive;
    }

    public boolean isCompress() {
        return compress;
    }

    public void setCompress(boolean compress) {
        this.compress = compress;
    }

    public PoolProperties getPool() {
        return pool;
    }

    public void setPool(PoolProperties pool) {
        this.pool = pool;
    }

    public String getBaseUrl() {
        return baseUrl;
    }

    public void setBaseUrl(String baseUrl) {
        this.baseUrl = baseUrl;
    }

    public LogProperties getLog() {
        return log;
    }

    public void setLog(LogProperties log) {
        this.log = log;
    }

    public HeaderProperties getHeaders() {
        return headers;
    }

    public void setHeaders(HeaderProperties headers) {
        this.headers = headers;
    }
}
