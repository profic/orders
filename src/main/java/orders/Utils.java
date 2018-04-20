package orders;

public class Utils {

    public static int parseInt(final String s) {
        final int len = s.length();
        int       num = '0' - s.charAt(0);

        int i = 1;
        while (i < len) {
            num = num * 10 + '0' - s.charAt(i++);
        }
        return -1 * num;
    }
}
