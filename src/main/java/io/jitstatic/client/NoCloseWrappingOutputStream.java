package io.jitstatic.client;

import java.io.IOException;
import java.io.OutputStream;

class NoCloseWrappingOutputStream  extends OutputStream {

    private final OutputStream wrapped;
    
    public NoCloseWrappingOutputStream(OutputStream os) {
        this.wrapped = os;
    }

    @Override
    public void write(int b) throws IOException {
        wrapped.write(b);
    }
    @Override
    public void close() throws IOException {
        // Do nothing
    }
    @Override
    public void flush() throws IOException {
        wrapped.flush();
    }
    
    @Override
    public void write(byte[] b) throws IOException {
        wrapped.write(b);
    }
    
    @Override
    public void write(byte[] b, int off, int len) throws IOException {
        wrapped.write(b, off, len);
    }
    
}

