package kroko;

public class ChecksumCalculator {

    /**
     * From header start
     */
    //public final static int CHECKSUM_OFFSET = 0x2C;
    public final static int BLOCK_FLAGS_OFFSET = 0x20;

    public static void updateChecksum(byte[] data, RomType romType, int offsetHeader) {
        switch (romType) {
            case BS_HIROM:
            case BS_LOROM:
                updateChecksumBS(data, romType, offsetHeader);
                return;
            case SFC_HIROM:
            case SFC_LOROM:
                updateChecksumSFC(data, romType, offsetHeader);
        }
    }

    public static void updateChecksumSFC(byte[] data, RomType romType, int headerOffset) {
        // Not implemented
    }

    /**
     * Satellaview Checksum Calculator
     */
    public static void updateChecksumBS(byte[] data, RomType type, int headerOffset) {

        int checksum = 0;
        
        int complementOffset = type.getOffsetChecksum()-2;

        data[headerOffset+ complementOffset] = (byte) 0x00;
        data[headerOffset+ complementOffset +1] = (byte) 0x00;
        data[headerOffset+ complementOffset +2] = (byte) 0xFF;
        data[headerOffset+ complementOffset +3] = (byte) 0xFF;

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

        data[headerOffset+ complementOffset] = (byte) (complement%0x100);
        data[headerOffset+ complementOffset +1] = (byte) (complement/0x100);
        data[headerOffset+ complementOffset +2] = (byte) (checksum%0x100);
        data[headerOffset+ complementOffset +3] = (byte) (checksum/0x100);

    }

}
