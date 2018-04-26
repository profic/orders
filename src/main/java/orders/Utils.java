package orders;

public class Utils {

    private Utils() {
        throw new UnsupportedOperationException("No way.");
    }

    /**
     * Faster than {@link Integer#parseInt(String)}, but with limitation: parses only positive decimal integers
     *
     * @param s positive decimal integer in string representation
     * @return positive decimal integer
     */
    public static int parseInt(final String s, int from, int to) {
        int num = '0' - s.charAt(from);

        int i = from + 1;
        while (i < to) {
            num = num * 10 + '0' - s.charAt(i++);
        }
        return -1 * num;
    }

}
