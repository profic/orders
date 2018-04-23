package orders;

@FunctionalInterface
interface UnsafeRunnable {
    void run() throws Exception;
}
