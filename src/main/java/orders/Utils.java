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
    public static int parseInt(final String s) {
        int len = s.length();
        int num = '0' - s.charAt(0);

        int i = 1;
        while (i < len) {
            num = num * 10 + '0' - s.charAt(i++);
        }
        return -1 * num;
    }


    public static int parseInt(final char[] arr, int from, int to) {
        int num = '0' - arr[from];

        int i = from + 1;
        while (i < to) {
            num = num * 10 + '0' - arr[i++];
        }
        return -1 * num;
    }
}
