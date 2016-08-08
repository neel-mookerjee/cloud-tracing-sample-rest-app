package cloud.tracing.demo;

public class ValueObject {
    private long id;
    private String content;

    public ValueObject() {
    }

    public ValueObject(long id, String content) {
        this.id = id;
        this.content = content;
    }

    public long getId() {
        return id;
    }

    public String getContent() {
        return content;
    }
}
