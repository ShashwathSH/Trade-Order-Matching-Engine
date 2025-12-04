package matching;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.List;

public class FileLogger implements Closeable {
    private final BufferedWriter writer;

    public FileLogger(String path) throws IOException {
        Path p = Path.of(path);
        Files.createDirectories(p.getParent() == null ? Path.of(".") : p.getParent());
        writer = Files.newBufferedWriter(p, StandardOpenOption.CREATE, StandardOpenOption.APPEND);
    }

    public synchronized void log(String line) {
        try {
            writer.write(line);
            writer.newLine();
            writer.flush();
        } catch (IOException e) {
            System.err.println("Logger error: " + e.getMessage());
        }
    }

    public synchronized void logOrders(String header, List<Order> orders) {
        log(header);
        for (Order o : orders) log(o.toString());
    }

    @Override
    public void close() throws IOException {
        writer.close();
    }
}
