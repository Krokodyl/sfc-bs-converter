package kroko.info;

import kroko.RomType;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;

public class Utils {

    /**
     * Hexadecimal representation of a byte, with leading zeros (2 digits)
     */
    public static String h2(byte b) {
        return String.format("%2s", Integer.toHexString(b & 0xFF)).replace(" ","0").toUpperCase();
    }

    /**
     * Hexadecimal representation of an integer , with leading zeros (5 digits)
     */
    public static String h5(int b) {
        return String.format("%5s", Integer.toHexString(b & 0xFFFFFF)).replace(" ","0").toUpperCase();
    }

    /**
     * Binary representation of a byte, with leading zeros (8 digits)
     */
    public static String b8(byte b) {
        return String.format("%8s", Integer.toBinaryString(b & 0xFF)).replace(" ","0");
    }

    /**
     * Extract the header from the rom data based on the rom type
     */
    public static int[] getSfcHeader(byte[] data, RomType type) {
        int[] header = new int[type.getHeaderLength()];
        for (int i=0;i<header.length;i++) {
            header[i] = data[type.getOffsetHeader()+i];
        }
        return header;
    }

    /**
     * Converts the day into BS format
     * @param day Day of the month (1-31)
     */
    public static byte getDay(String day) {
        try {
            int i = Integer.parseInt(day);
            if (i<=31) {
                return (byte) (i << 3);
            } else System.err.println("Invalid value for -day: "+day);
        } catch (NumberFormatException e) {
            System.err.println("Invalid value for -day: "+day);
        }
        return (byte) 0xE0;
    }

    /**
     * Converts the month into BS format
     * @param month Month (1-12)
     */
    public static byte getMonth(String month) {
        try {
            int i = Integer.parseInt(month);
            if (i<=12) {
                return (byte) (i << 4);
            } else System.err.println("Invalid value for -month: "+month);
        } catch (NumberFormatException e) {
            System.err.println("Invalid value for -month: "+month);
        }
        return (byte) 0xC0;
    }

    /**
     * Converts the BS header day byte into regular number 1..31
     */
    public static int getDay(byte b) {
        return ((b & 0xFF) >> 3);
    }

    /**
     * Converts the BS header month byte into regular number 1..12
     */
    public static int getMonth(byte b) {
        return ((b & 0xFF) >> 4);
    }

    /**
     * Returns the BS header block allocation byte based on the given size (in bytes)
     */
    public static byte getBlockFlag(int romSize) {
        int blockCount = romSize/0x20000;
        if (blockCount*0x20000<romSize) blockCount++;
        byte res = 1;
        while (--blockCount>0) {
            res = (byte) (((res << 1) + 1) & 0xFF);
        }
        return res;
    }

    /**
     * Returns the number of blocks (1..8) based on the given BS header block allocation byte
     */
    public static int getBlockCount(byte b) {
        int bit = 8;
        int count = 0;
        while (bit>0) {
            if ((b & 0x01) == 1) {
                count++;
            }
            b = (byte) (b >> 1);
            bit--;
        }
        return count;
    }

    /**
     * Writes the byte array (@bytes) into the byte array (@target) at (@offset)
     */
    public static void writeBytes(int[] target, byte[] bytes, int offset) {
        for (byte b : bytes) {
            target[offset++] = b;
        }
    }

    /**
     * Writes the byte (@b) into the byte array (@target) at (@offset)
     */
    public static void writeByte(int[] target, byte b, int offset) {
        target[offset] = b;
    }

    /**
     * Reads a 2-byte word (little endian)
     */
    public static int readWord(byte[] data, int offset) {
        int word = (data[offset+1] & 0xFF)*0x100;
        word += (data[offset] & 0xFF);
        return word;
    }

    /**
     * Reads a binary file as a byte array
     */
    public static byte[] readRom(String inputRom) {
        byte[] data = new byte[0];
        try {
            data = Files.readAllBytes(new File(inputRom).toPath());
        } catch (NoSuchFileException ex) {
            System.err.printf("File not found: %s\n", inputRom);
            System.exit(1);
        } catch (IOException e) {
            System.err.println(e.getMessage());
            System.exit(1);
        }
        return data;
    }
}
