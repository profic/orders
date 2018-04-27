package orders;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicReferenceArray;

public class ParseJob {

    public static final Object PARSE_END = new Object();

    private final ExecutorService              executor;
    private final AtomicReferenceArray<Object> parsedArr;

    public ParseJob(final ExecutorService executor, final int size) {
        this.executor = executor;
        this.parsedArr = new AtomicReferenceArray<>(size);
    }


    public Future<?> parse(final AtomicReferenceArray<String> readArr) {
        return executor.submit(() -> {
            int     position = 0;
            boolean run      = true;

            AtomicReferenceArray<Object> parsedArr = this.parsedArr;
            while (run) {
                String s;
                while ((s = readArr.get(position)) != null) {
                    if (ReadJob.END.equals(s)) {
                        run = false;
                        break;
                    }
                    parsedArr.set(position, doParse(s));
                    position++;
                }
                Thread.yield();
            }
            parsedArr.set(position, PARSE_END);
        });
    }

    private static Object doParse(final String s) {
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

    private static <T extends OrderActor> T processOrder(final String s) {
        int  endIdIdx  = s.indexOf(',', 2);
        char orderType = s.charAt(endIdIdx + 1);
        if (orderType == 's') {
            return parse(s, endIdIdx, Ctor.SELLER);
        } else {
            return parse(s, endIdIdx, Ctor.BUYER);
        }
    }

    private static <T extends OrderActor> T parse(
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

    public AtomicReferenceArray<Object> getParsedArr() {
        return parsedArr;
    }
}
