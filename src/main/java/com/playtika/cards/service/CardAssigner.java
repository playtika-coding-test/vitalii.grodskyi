package com.playtika.cards.service;

import com.playtika.cards.event.Event;

import java.util.function.Consumer;

public interface CardAssigner {

    void assignCard(long userId, long cardId);

    void subscribe(Consumer<Event> consumer);
}
