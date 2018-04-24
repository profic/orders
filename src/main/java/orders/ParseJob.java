package orders;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicReferenceArray;

public class ParseJob {

    public static final Object[] PARSE_END = new Object[0];

    private final ExecutorService                executor;
    private final AtomicReferenceArray<Object[]> parsedArr;

    public ParseJob(final ExecutorService executor) {
        this.executor = executor;
        this.parsedArr = new AtomicReferenceArray<>(OrdersProcessor.CHUNK_CNT);
    }


    public Future<Object> parse(Runnable beforeRun, final AtomicReferenceArray<String[]> readArr) {
        return executor.submit(() -> {
            beforeRun.run();
            int                            position  = 0;
            boolean                        run       = true;
            AtomicReferenceArray<Object[]> parsedArr = this.parsedArr;
            while (run) {
                String[] buf;
                while ((buf = readArr.get(position)) != null) {
                    if (ReadJob.EMPTY_ARR == buf) {
                        run = false;
                        break;
                    }
                    Object[] outBuf = new Object[OrdersProcessor.BUF_SIZE];
                    for (int i = 0; i < buf.length; i++) {
                        String s = buf[i];
                        if (ReadJob.END.equals(s)) {
                            run = false;
                            break;
                        }
                        outBuf[i] = doParse(s);
                    }
                    parsedArr.set(position, outBuf);
                    position++;
                }
                Thread.yield();
            }
            parsedArr.set(position - 1, PARSE_END);
            return null;
        });
    }

    private Object doParse(final String s) {
        Object res   = null;
        char   sType = s.charAt(0);
        if (sType == 'o') {
            res = processOrder(s);
        } else if (sType == 'c') {
            res = Utils.parseInt(s, 2, s.length());
        } else if (sType == 'q') {
            res = s;
        }

        return res;
    }

    private <T extends OrderActor> T processOrder(final String s) {
        int  endIdIdx  = s.indexOf(',', 2);
        char orderType = s.charAt(endIdIdx + 1);
        if (orderType == 's') {
            return parse(s, endIdIdx, Ctor.SELLER);
        } else {
            return parse(s, endIdIdx, Ctor.BUYER);
        }
    }

    private <T extends OrderActor> T parse(
            final String s,
            final int endIdIdx,
            Ctor ctor
    ) {
        int len           = s.length();
        int beginPriceIdx = s.indexOf(',', endIdIdx + 1) + 1;
        int endPriceIdx   = s.lastIndexOf(',', len - 2);

        int beginIdIdx   = 2;
        int endSizeIdx   = len;
        int beginSizeIdx = endPriceIdx + 1;

        int id    = Utils.parseInt(s, beginIdIdx, endIdIdx);
        int price = Utils.parseInt(s, beginPriceIdx, endPriceIdx);
        int size  = Utils.parseInt(s, beginSizeIdx, endSizeIdx);

        return (T) ctor.create(id, size, price);
    }

    public AtomicReferenceArray<Object[]> getParsedArr() {
        return parsedArr;
    }
}
