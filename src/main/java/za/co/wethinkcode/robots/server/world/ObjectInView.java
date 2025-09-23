package za.co.wethinkcode.robots.server.world;

public class ObjectInView {
    public final String direction;
    public final String type;
    public final int distance;
    public ObjectInView(String direction, String type, int distance) {
        this.direction = direction;
        this.type = type;
        this.distance = distance;
    }
}