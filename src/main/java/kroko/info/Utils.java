package kroko.info;

import kroko.RomType;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

public class Utils {

    public static String h2(byte b) {
        return String.format("%2s", Integer.toHexString(b & 0xFF)).replace(" ","0").toUpperCase();
    }
    
    public static String h5(int b) {
        return String.format("%5s", Integer.toHexString(b & 0xFFFFFF)).replace(" ","0").toUpperCase();
    }

    public static String b8(byte b) {
        return String.format("%8s", Integer.toBinaryString(b & 0xFF)).replace(" ","0");
    }

    public static int[] getSfcHeader(byte[] data, RomType type) {
        
        int[] header = new int[type.getHeaderLength()];
        for (int i=0;i<header.length;i++) {
            header[i] = data[type.getOffsetHeader()+i];
        }
        return header;
    }

    public static int[] getBsHeader(byte[] data, RomType type) {
        int[] header = new int[type.getHeaderLength()];
        for (int i=0;i<header.length;i++) {
            header[i] = data[type.getOffsetHeader()+i];
        }
        return header;
    }
    
    /**
     * Converts the day into BS format
     * @param day Day of the month (1-31)
     * @return 
     */
    public static byte getDay(String day) {
        try {
            int i = Integer.parseInt(day);
            if (i<=31) {
                byte b = (byte) (i << 3);
                return b;
            } else System.err.println("Invalid value for -day: "+day);
        } catch (NumberFormatException e) {
            System.err.println("Invalid value for -day: "+day);
        }
        return (byte) 0xE0;
    }

    /**
     * Converts the month into BS format
     * @param month Month (1-12)
     * @return
     */
    public static byte getMonth(String month) {
        try {
            int i = Integer.parseInt(month);
            if (i<=12) {
                byte b = (byte) (i << 4);
                return b;
            } else System.err.println("Invalid value for -month: "+month);
        } catch (NumberFormatException e) {
            System.err.println("Invalid value for -month: "+month);
        }
        return (byte) 0xC0;
    }

    public static int getDay(byte b) {
        return ((b & 0xFF) >> 3);
    }

    public static int getMonth(byte b) {
        return ((b & 0xFF) >> 4);
    }

    public static byte getBlockFlag(int romSize) {
        int blockCount = romSize/0x20000;
        if (blockCount*0x20000<romSize) blockCount++;
        byte res = 1;
        while (--blockCount>0) {
            res = (byte) (((res << 1) + 1) & 0xFF);
        }
        return res;
    }

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

    public static void writeBytes(int[] target, byte[] bytes, int offset) {
        for (byte b : bytes) {
            target[offset++] = b;
        }
    }

    public static void writeByte(int[] target, byte b, int offset) {
        target[offset] = b;
    }
    
    public static int readWord(byte[] data, int offset) {
        int word = (data[offset+1] & 0xFF)*0x100;
        word += (data[offset] & 0xFF);
        return word;
    }
    
    public static byte[] readRom(String inputRom) {
        byte[] data = new byte[0];
        try {
            data = Files.readAllBytes(new File(inputRom).toPath());
        } catch (IOException ex) {
            System.err.println(ex);
        }
        return data;
    }
}
