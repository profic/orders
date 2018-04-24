package orders;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Random;

public class Generator {
    private static final Random r = new Random();

    private static final int ELEMS_IN_GROUP = 17;
    private static final int GROUPS_CNT     = 50_000;
    private static final int MAX_PRICE      = 10000;
    private static final int MAX_SIZE       = 1000;
    private static final int ROWS_CNT       = 1_000_000;

    public static void main(String[] args) throws Exception {
        Path path = Paths.get("<path to output>");
        generateData(path.toFile());
    }

    private static void generateData(final File file) throws IOException {

        ArrayList<Integer> prices = new ArrayList<>(ROWS_CNT);

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
            int id = 0;
            for (int j = 0; j < GROUPS_CNT; j++) {
                for (int i = 0; i < ELEMS_IN_GROUP; i++) {

                    int price = r.nextInt(MAX_PRICE);
                    int size  = r.nextInt(MAX_SIZE);

                    prices.add(price);
                    writeOrder(writer, id, price, size);

                    writePriceSizeQuery(prices, writer, id, i);
                    writeOrderQuery(writer, i);
                    writeCancelOrder(writer, id, i);
                    id++;
                }
            }
        }
    }

    private static void writeCancelOrder(final BufferedWriter writer, final int id, final int i) throws IOException {
        if (i == 13) {
            writer.write("c," + r.nextInt(id));
            writer.newLine();
        }
    }

    private static void writeOrderQuery(final BufferedWriter writer, final int i) throws IOException {
        if (i == 10) {
            if (r.nextBoolean()) {
                writer.write("q,sellers");
            } else {
                writer.write("q,buyers");
            }
            writer.newLine();
        }
    }

    private static void writePriceSizeQuery(
            final ArrayList<Integer> prices,
            final BufferedWriter writer,
            final int id,
            final int idx) throws IOException {
        if (idx == 5) {
            writer.write("q,size," + prices.get(r.nextInt(id)));
            writer.newLine();
        }
    }

    private static void writeOrder(
            final BufferedWriter writer,
            final int id,
            final int price,
            final int size) throws IOException {
        boolean buy = r.nextBoolean();

        String order = buy ?
                String.format("o,%s,b,%s,%s", id, price, size) :
                String.format("o,%s,s,%s,%s", id, price, size);
        writer.write(order);
        writer.newLine();
    }

}
