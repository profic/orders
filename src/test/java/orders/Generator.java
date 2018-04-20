package orders;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Random;
import java.util.stream.Stream;

public class Generator {
    public static final Random r = new Random();

    public static void main(String[] args) throws Exception {
//        Path path = Paths.get("c:\\Users\\Uladzislau_Malchanau\\Desktop", "data.txt");
//        write(path.toFile());
        Path path = Paths.get("c:\\Users\\Uladzislau_Malchanau\\Desktop", "data3.txt");
        write2(path.toFile());
//            nio(path);
//        io(path);
    }

    private static void io(final Path path) throws IOException {
        long start   = System.currentTimeMillis();
        int  bufSize = 10000 * 8192;
        try (BufferedReader r = new BufferedReader(new FileReader(path.toFile()), bufSize)) {
            r.lines().forEach(l -> {});
        }
        System.out.println(System.currentTimeMillis() - start);
    }

    private static void nio(final Path path) throws Exception {
        long start = System.currentTimeMillis();
        try (Stream<String> lines = Files.lines(path)) {
            lines.forEach(l -> {});
        }
        System.out.println(System.currentTimeMillis() - start);
    }


    private static void write(final File file) throws IOException {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
            for (int j = 0; j < 10000000; j++) {
                for (int i = 0; i < 17; i++) {

                    int id    = r.nextInt(1_000_000);
                    int price = r.nextInt(10000);
                    int size  = r.nextInt(1000);

                    boolean buy = r.nextBoolean();

                    String order = buy ?
                            String.format("o,%s,b,%s,%s", id, price, size) :
                            String.format("o,%s,s,%s,%s", id, price, size);
                    writer.write(order);
                    writer.newLine();

                    if (i == 5) {
                        writer.write("q,size," + r.nextInt(1000));
                        writer.newLine();
                    }
                    if (i == 10) {
                        if (r.nextBoolean()) {
                            writer.write("q,sellers");
                        } else {
                            writer.write("q,buyers");
                        }
                        writer.newLine();
                    }
                    if (i == 13) {
                        writer.write("c," + r.nextInt(1_000_000));
                        writer.newLine();
                    }
                }
            }
        }
    }

    private static void write2(final File file) throws IOException {

        ArrayList<Integer> prices = new ArrayList<>(1_000_000);

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
            int id = 0;
            for (int j = 0; j < 50_000; j++) {
                for (int i = 0; i < 17; i++) {

                    int price = r.nextInt(10000);
                    int size  = r.nextInt(1000);

                    prices.add(price);
                    boolean buy = r.nextBoolean();

                    String order = buy ?
                            String.format("o,%s,b,%s,%s", id, price, size) :
                            String.format("o,%s,s,%s,%s", id, price, size);
                    writer.write(order);
                    writer.newLine();

                    if (i == 5) {
                        writer.write("q,size," + prices.get(r.nextInt(id)));
                        writer.newLine();
                    }
                    if (i == 10) {
                        if (r.nextBoolean()) {
                            writer.write("q,sellers");
                        } else {
                            writer.write("q,buyers");
                        }
                        writer.newLine();
                    }
                    if (i == 13) {
                        writer.write("c," + r.nextInt(id));
                        writer.newLine();
                    }
                    id++;
                }
            }
        }
    }

}
