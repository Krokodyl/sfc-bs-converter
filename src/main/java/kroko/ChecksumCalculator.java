package kroko;

public class ChecksumCalculator {

    /**
     * From header start
     */
    public final static int CHECKSUM_OFFSET = 0x2C;
    public final static int BLOCK_FLAGS_OFFSET = 0x20;

    public static void updateChecksumSFC(byte[] data, RomType romType) {
        updateChecksumSFC(data, romType.getOffsetHeader());
    }

    public static void updateChecksumBS(byte[] data, RomType romType) {
        updateChecksumBS(data, romType.getOffsetHeader());
    }

    public static void updateChecksumSFC(byte[] data, int headerOffset) {

        int checksum = 0;

        data[headerOffset+ CHECKSUM_OFFSET] = (byte) 0x00;
        data[headerOffset+ CHECKSUM_OFFSET +1] = (byte) 0x00;
        data[headerOffset+ CHECKSUM_OFFSET +2] = (byte) 0xFF;
        data[headerOffset+ CHECKSUM_OFFSET +3] = (byte) 0xFF;

        for (int i = 0; i < data.length; i = i+1) {
            if ((i<headerOffset || i>(headerOffset+0x2F)) && i<0x80000)
                checksum += (data[i] & 0xFF);
            if (checksum>=0x10000) checksum-=0x10000;
        }

        int complement = 0xFFFF-checksum;

        data[headerOffset+ CHECKSUM_OFFSET] = (byte) (complement%0x100);
        data[headerOffset+ CHECKSUM_OFFSET +1] = (byte) (complement/0x100);
        data[headerOffset+ CHECKSUM_OFFSET +2] = (byte) (checksum%0x100);
        data[headerOffset+ CHECKSUM_OFFSET +3] = (byte) (checksum/0x100);

    }
    
    public static void updateChecksumLowRom(byte[] data, int headerOffset) {

        int checksum = 0;

        data[headerOffset+ CHECKSUM_OFFSET] = (byte) 0x00;
        data[headerOffset+ CHECKSUM_OFFSET +1] = (byte) 0x00;
        data[headerOffset+ CHECKSUM_OFFSET +2] = (byte) 0xFF;
        data[headerOffset+ CHECKSUM_OFFSET +3] = (byte) 0xFF;

        for (int i = 0; i < data.length; i = i+1) {
            if ((i<headerOffset || i>(headerOffset+0x2F)) && i<0x80000)
                checksum += (data[i] & 0xFF);
            if (checksum>=0x10000) checksum-=0x10000;
        }

        int complement = 0xFFFF-checksum;

        data[headerOffset+ CHECKSUM_OFFSET] = (byte) (complement%0x100);
        data[headerOffset+ CHECKSUM_OFFSET +1] = (byte) (complement/0x100);
        data[headerOffset+ CHECKSUM_OFFSET +2] = (byte) (checksum%0x100);
        data[headerOffset+ CHECKSUM_OFFSET +3] = (byte) (checksum/0x100);

    }

    /**
     * LowRom Satellaview Checksum Calculator
     */
    public static void updateChecksumBS(byte[] data, int headerOffset) {

        int checksum = 0;

        data[headerOffset+ CHECKSUM_OFFSET] = (byte) 0x00;
        data[headerOffset+ CHECKSUM_OFFSET +1] = (byte) 0x00;
        data[headerOffset+ CHECKSUM_OFFSET +2] = (byte) 0xFF;
        data[headerOffset+ CHECKSUM_OFFSET +3] = (byte) 0xFF;

        int bits = 8;
        byte allocationBlocks = data[headerOffset+ BLOCK_FLAGS_OFFSET];
        
        int offsetBlock = 0;
        while (bits > 0) {
            if ((allocationBlocks & 0x01) == 0x01) {
                for (int i = offsetBlock; i < offsetBlock + 0x20000; i = i+1) {
                    if ((i<headerOffset || i>(headerOffset+0x2F)))
                        checksum += (data[i] & 0xFF);
                    if (checksum>=0x10000) checksum-=0x10000;
                }
            }
            allocationBlocks = (byte) ((allocationBlocks & 0xFF) >>> 1);
            offsetBlock += 0x20000;
            bits--;
        }

        int complement = 0xFFFF-checksum;

        data[headerOffset+ CHECKSUM_OFFSET] = (byte) (complement%0x100);
        data[headerOffset+ CHECKSUM_OFFSET +1] = (byte) (complement/0x100);
        data[headerOffset+ CHECKSUM_OFFSET +2] = (byte) (checksum%0x100);
        data[headerOffset+ CHECKSUM_OFFSET +3] = (byte) (checksum/0x100);

    }

    public static void updateChecksumLowRomBSFlash(byte[] data, int headerOffset) {
        int checksum = 0;
        data[headerOffset + 44] = 0;
        data[headerOffset + 44 + 1] = 0;
        data[headerOffset + 44 + 2] = -1;
        data[headerOffset + 44 + 3] = -1;
        int bits = 8;
        byte allocationBlocks = data[headerOffset + 32];

        for (int i = 0; i < data.length; i++) {
            if ((i<headerOffset || i>(headerOffset+0x2F)))
            checksum += data[i] & 255;
            if (checksum >= 65536) {
                checksum -= 65536;
            }
        }


        int i;
        i = '\uffff' - checksum;
        data[headerOffset + 44] = (byte)(i % 256);
        data[headerOffset + 44 + 1] = (byte)(i / 256);
        data[headerOffset + 44 + 2] = (byte)(checksum % 256);
        data[headerOffset + 44 + 3] = (byte)(checksum / 256);
    }
}
