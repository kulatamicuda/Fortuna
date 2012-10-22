package se.grunka.fortuna.tests;

import java.io.BufferedOutputStream;
import java.io.FileOutputStream;
import java.io.OutputStream;

import se.grunka.fortuna.Fortuna;

public class Dump {
    private static final int MEGABYTE = 1024 * 1024;

    // Compression test: xz -e9zvkf random.data

    public static void main(String[] args) throws Exception {
        if (args.length < 1 || args.length > 2) {
            usage();
            System.exit(args.length == 0 ? 0 : 1);
        }
        long megabytes = 0;
        try {
            megabytes = Long.parseLong(args[0]);
        } catch (NumberFormatException e) {
            usage();
            System.out.println("Megabytes was not a number: " + args[0]);
            System.exit(1);
        }
        if (megabytes < 1) {
            usage();
            System.out.println("Needs to be at least one megabyte, was " + megabytes);
            System.exit(1);
        }
        OutputStream output;
        if (args.length == 2) {
            output = new FileOutputStream(args[1], false);
        } else {
            output = System.out;
        }
        long dataSize = megabytes * MEGABYTE;
        long remainingBytes = dataSize;
        byte[] buffer = new byte[MEGABYTE];
        System.out.println("Initializing RNG...");
        Fortuna fortuna = Fortuna.createInstance();
        System.out.println("Generating data...");
        OutputStream outputStream = new BufferedOutputStream(output);
        try {
            while (remainingBytes > 0) {
                fortuna.nextBytes(buffer);
                outputStream.write(buffer);
                remainingBytes -= buffer.length;
                System.out.print((100 * (dataSize - remainingBytes) / dataSize) +  "%\r");
            }
        } finally {
            outputStream.close();
        }
        System.out.println("Done");
    }

    private static void usage() {
        System.out.println("Usage: " + Dump.class.getName() + " <megabytes> [<file>]");
        System.out.println("Will generate <megabytes> of data and output them either to <file> or stdout if <file> is not specified");
    }
}
