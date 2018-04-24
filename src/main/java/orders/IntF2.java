package orders;

@FunctionalInterface
interface IntF2<In> {
    void apply(int in1, In in2);
}
