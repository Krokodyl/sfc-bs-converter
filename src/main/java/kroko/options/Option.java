package kroko.options;

public enum Option {

    BOOT_LIMIT("Boot Limit", 0xFF),
    ROM_SPEED("Rom Speed", 0b00010000),
    MAP_MODE("Map Mode", 0b00001111),
    SOUNDLINK_RADIO("Soundlink Radio", 0b00010000),
    EXECUTION("Execution", 0b01100000),
    ST_GIGA_INTRO("St.GIGA Intro", 0b10000000)
    ;
    
    public String label;
    int mask;

    Option(String label, int mask) {
        this.label = label;
        this.mask = mask;
    }
}
