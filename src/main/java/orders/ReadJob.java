package orders;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;


public class ReadJob {

    public static final String END = "END";

    private final BlockingQueue<String> readArr;
    private final Path                  path;
    private final ExecutorService       executor;

    public ReadJob(int size, final Path path, final ExecutorService executor) {
        readArr = new LinkedBlockingQueue<>(size);
        this.path = path;
        this.executor = executor;
    }

    public Future<?> read() {
        return executor.submit(() -> {
            try (BufferedReader b = Files.newBufferedReader(path)) {
                String line;
                while ((line = b.readLine()) != null) {
                    readArr.add(line);
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            } finally {
                readArr.add(END);
            }
        });
    }

    public BlockingQueue<String> getReadQueue() {
        return readArr;
    }
}
