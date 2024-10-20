package kroko.options;

public enum SoundlinkRadio {

    MUTED(0b00010000, Option.SOUNDLINK_RADIO, "Muted"),
    UNMUTED(0b00000000, Option.SOUNDLINK_RADIO, "Unmuted")
    ;
    
    int value;
    public String label;
    public Option option;

    SoundlinkRadio(int value, Option option, String label) {
        this.value = value;
        this.option = option;
        this.label = label;
    }
    
    public boolean test(int b) {
        return (this.option.mask & b) == value;
    }

}
