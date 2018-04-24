package orders;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicReferenceArray;


public class ReadJob {

    public static final String   END       = "END";
    public static final String[] EMPTY_ARR = new String[0];

    private final AtomicReferenceArray<String[]> readArr;
    private final Path                           path;
    private final ExecutorService                executor;

    public ReadJob(int size, final Path path, final ExecutorService executor) {
        readArr = new AtomicReferenceArray<>(size);
        this.path = path;
        this.executor = executor;
    }

    public Future<?> read(final Runnable beforeExecution) {
        return executor.submit(() -> {
            beforeExecution.run();
            int pos = 0;
            try (BufferedReader b = Files.newBufferedReader(path)) {

                boolean run = true;
                while (run) {
                    String[] buf = new String[OrdersProcessor.BUF_SIZE];
                    for (int i = 0; i < buf.length; i++) {
                        String line = b.readLine();
                        if (line == null) {
                            run = false;
                            buf[i] = END;
                            break;
                        } else {
                            buf[i] = line;
                        }
                    }
                    readArr.set(pos++, buf);
                }

            } catch (IOException e) {
                throw new RuntimeException(e);
            } finally {
                readArr.set(pos, EMPTY_ARR);
            }
//            System.out.println("END READING");
        });
    }

    public AtomicReferenceArray<String[]> getReadArr() {
        return readArr;
    }
}
