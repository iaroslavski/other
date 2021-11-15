import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.RandomAccessFile;

/**
 * This class counts the number of unique IP addresses, stored in a file.
 *
 * @author Vladimir Yaroslavskiy
 * @version 2021.11.02
 */
public class IPaddressCounter {

    private static final String DEFAULT_INPUT_FILE_NAME = "ip_addresses";
    private static final int COUNT_LENGTH = 1 << 30;

    public static void main(String[] arg) {
        String inputName = arg.length == 1 ? arg[0] : DEFAULT_INPUT_FILE_NAME;
        new IPaddressCounter().main(inputName);
    }

    /**
     * Computes pseudo historgam and then finds the unique addresses.
     * Pseudo historgam contains non actual counts, but 0 (no), 1 (one), 3 (many)
     * values only, because we need to know unique addresses only.
     * Two bits are used for 1 IP address.
     *
     * @param inputName the name of input file
     */
    private void main(String inputName) {
        byte[] count = new byte[COUNT_LENGTH];
        countIPs(inputName, count);

        out("\nCounting...");
        long unique = countUnique(count);
        out("\nUnique addresses: " + unique);
    }

    private void countIPs(String inputName, byte[] count) {
        try (BufferedReader reader = new BufferedReader(new FileReader(inputName))) {
            long total = new RandomAccessFile(inputName, "r").getChannel().size() / 14;
            total = total == 0 ? 1 : total;
            out("Reading approx. " + total + " IP addresses...");
            countIPs(reader, count, total);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void countIPs(BufferedReader reader, byte[] count, long total) throws IOException {
        String line;
        long percent = 0;

        for (long k = 0, current = 0; (line = reader.readLine()) != null; current = (++k * 100) / total) {
            long ip = toLong(line);

            int i = (int) (ip / 4);
            byte b = count[i];

            int shift = ((int) (ip & 3)) * 2;
            int cnt = (b >> shift) & 3;

            if (cnt == 0) {
                b |= 1 << shift;
                count[i] = b;
            } else if (cnt == 1) {
                b |= 2 << shift;
                count[i] = b;
            }

            if (current > percent) {
                percent = current;
                out(percent + "% done");
            }
        }
    }

    private long countUnique(byte[] count) {
        long unique = 0;
        
        for (int i = 0; i < COUNT_LENGTH; ++i) {
            byte b = count[i];

            if ((b & 3) == 1) {
                ++unique;
            }
            if (((b >> 2) & 3) == 1) {
                ++unique;
            }
            if (((b >> 4) & 3) == 1) {
                ++unique;
            }
            if (((b >> 6) & 3) == 1) {
                ++unique;
            }
        }
        return unique;
    }

    private long toLong(String ip) {
        String[] digits = ip.split("\\.");

        if (digits.length != 4) {
            throw new IllegalArgumentException("No 4 digits in ip: " + ip);
        }
        return (toDigit(digits[0]) << 24) | (toDigit(digits[1]) << 16) | (toDigit(digits[2]) << 8) | toDigit(digits[3]);
    }

    private long toDigit(String s) {
        try {
            long i = Long.valueOf(s);

            if (i < 0 || i > 255) {
                throw new IllegalArgumentException("IP digit is out of range: " + s);
            }
            return i;
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Not a number: " + s);
        }
    }

    private void out(Object object) {
        System.out.println(object);
    }
}
