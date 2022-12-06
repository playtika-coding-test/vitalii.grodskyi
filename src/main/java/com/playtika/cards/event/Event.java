package com.playtika.cards.event;

import java.util.Objects;

import static org.apache.commons.lang3.builder.ToStringBuilder.reflectionToString;
import static org.apache.commons.lang3.builder.ToStringStyle.SHORT_PREFIX_STYLE;

public class Event {

    public final long userId;
    public final Type type;

    public enum Type {
        ALBUM_FINISHED
    }

    public Event(long userId, Type type) {
        this.userId = userId;
        this.type = type;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Event event = (Event) o;
        return userId == event.userId &&
                type == event.type;
    }

    @Override
    public int hashCode() {
        return Objects.hash(userId, type);
    }

    @Override
    public String toString() {
        return reflectionToString(this, SHORT_PREFIX_STYLE);
    }

}
