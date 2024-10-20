package kroko.options;

public enum StGigaIntro {

    NORMAL(0b00000000, Option.ST_GIGA_INTRO, "Play"),
    SKIP(0b10000000, Option.ST_GIGA_INTRO, "Skip")
    ;
    
    int value;
    public String label;
    public Option option;

    StGigaIntro(int value, Option option, String label) {
        this.value = value;
        this.option = option;
        this.label = label;
    }
    
    public boolean test(int b) {
        return (this.option.mask & b) == value;
    }

}
