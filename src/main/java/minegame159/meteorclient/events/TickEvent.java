package minegame159.meteorclient.events;

import lombok.Getter;

public class TickEvent {
    @Getter private final Type type;

    public TickEvent(Type type) {
        this.type = type;
    }

    public enum Type {
        PRE, POST
    }
}
