package com.taboola.async_profiler.utils;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

public class IOUtils {

    private static final int EOF = -1;
    private static final int DEFAULT_BUFFER_SIZE = 1024 * 4;

    public InputStream getDisposableFileInputStream(String path) throws FileNotFoundException {
        return new FileInputStream(path) {
            @Override
            public void close() throws IOException {
                try {
                    super.close();
                } finally {
                    safeDeleteIfExists(path);
                }
            }
        };
    }

    public void safeDeleteIfExists(String filePath) {
        try {
            Files.deleteIfExists(Paths.get(filePath));
        } catch (Exception e) {}
    }

    public String createTempFile(String prefix, String suffix) throws IOException {
        File file = File.createTempFile(prefix, suffix);
        file.deleteOnExit();
        return file.getAbsolutePath();
    }

    public void copy(InputStream inputStream, String path) throws IOException {
        Files.copy(inputStream, Paths.get(path), StandardCopyOption.REPLACE_EXISTING);
    }

    public int copy(InputStream inputStream, OutputStream outputStream) throws IOException {
        byte[] buffer = new byte[DEFAULT_BUFFER_SIZE];
        long count = 0;
        int n = 0;
        while (EOF != (n = inputStream.read(buffer))) {
            outputStream.write(buffer, 0, n);
            count += n;
        }

        if (count > Integer.MAX_VALUE) {
            return -1;
        }

        return (int) count;
    }

    public String readToString(InputStream inputStream) {
        int bufferSize = 1024;
        char[] buffer = new char[bufferSize];
        StringBuilder result = new StringBuilder();
        Reader in = new InputStreamReader(inputStream, StandardCharsets.UTF_8);

        try {
            for (int numRead; (numRead = in.read(buffer, 0, buffer.length)) > 0; ) {
                result.append(buffer, 0, numRead);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        return result.toString();
    }
}
