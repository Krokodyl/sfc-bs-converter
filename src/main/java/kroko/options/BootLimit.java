package kroko.options;


/**
 * 0xFC (11111100): 5 boots
 * 0xBC (10111100): 4 boots
 * 0x9C (10011100): 3 boots
 * 0x8C (10001100): 2 boots
 * 0x84 (10000100): 1 boot
 * 0x80 (10000000): 0 boots (Game will not detect on BS-X)
 * 0x00 (00000000): Unlimited Boots
 */
public enum BootLimit {

    UNLIMITED(0x00, Option.BOOT_LIMIT, "Unlimited"),
    BOOT_0(0x80, Option.BOOT_LIMIT, "0 boot"),
    BOOT_1(0x84, Option.BOOT_LIMIT, "1 boot"),
    BOOT_2(0x8C, Option.BOOT_LIMIT, "2 boots"),
    BOOT_3(0x9C, Option.BOOT_LIMIT, "3 boots"),
    BOOT_4(0xBC, Option.BOOT_LIMIT, "4 boots"),
    BOOT_5(0xFC, Option.BOOT_LIMIT, "5 boots")
    ;
    
    int value;
    public String label;
    public Option option;

    BootLimit(int value, Option option, String label) {
        this.value = value;
        this.option = option;
        this.label = label;
    }
    
    public boolean test(int b) {
        return (this.option.mask & b) == value;
    }

}
