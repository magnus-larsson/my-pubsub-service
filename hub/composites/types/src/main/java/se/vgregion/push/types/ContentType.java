package se.vgregion.push.types;

public enum ContentType {
    ATOM("application/atom+xm"),
    RSS("application/rss+xml");
    
    private String value;
    
    ContentType(String value) {
        this.value = value;
    }

    public static ContentType fromValue(String value) {
        if("application/atom+xm".equals(value)) return ATOM;
        else if("application/rss+xml".equals(value)) return RSS;
        else throw new IllegalArgumentException("Unknown content type: " + value);
    }
    
    @Override
    public String toString() {
        return value;
    }
    
    
}
