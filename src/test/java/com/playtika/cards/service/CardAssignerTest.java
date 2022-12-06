package com.playtika.cards.service;

import com.playtika.cards.domain.Album;
import com.playtika.cards.domain.Card;
import com.playtika.cards.event.Event;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.Collection;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static com.google.common.collect.Sets.newHashSet;
import static com.playtika.cards.event.Event.Type.ALBUM_FINISHED;
import static java.util.stream.Collectors.toList;
import static java.util.stream.LongStream.range;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class CardAssignerTest {

    @Mock
    private ConfigurationProvider configurationProvider;

    private CardAssigner cardAssigner;

    @Before
    public void configure() {
        when(configurationProvider.get()).thenReturn(
                new Album(1L, "Animals", newHashSet(
                        new Card(1L, "Eagle"),
                        new Card(2L, "Cormorant"),
                        new Card(3L, "Sparrow"),
                        new Card(4L, "Raven"),
                        new Card(5L, "Salmon"),
                        new Card(6L, "Mullet"),
                        new Card(7L, "Bream"),
                        new Card(8L, "Marline"))
                ));

        cardAssigner = new DefaultCardAssigner();
    }

    //<editor-fold defaultstate="collapsed" desc="pre-made acceptance scenarios">
    @Test
    public void assignAllCardsTo10000UsersOnce() throws InterruptedException {
        assignAllCardsTo10000Users(1);
    }

    @Test
    public void assignAllCardsTo10000UsersTwice() throws InterruptedException {
        assignAllCardsTo10000Users(2);
    }

    private void assignAllCardsTo10000Users(int times) throws InterruptedException {
        List<Long> userIds = generateUserIds(10000L);
        Album album = configurationProvider.get();

        Collection<Event> expectedEvents = generateExpectedEvents(userIds);

        Collection<Event> actualEvents = subscribeEventListener(cardAssigner);

        for (int i = 0; i < times; i++) {
            assignAllCardsToUsersOnceConcurrently(cardAssigner, userIds, album);
        }

        Assert.assertTrue("Expected " + expectedEvents.size() + " events, got " + actualEvents.size(),
                expectedEvents.size() == actualEvents.size());
    }

    private List<Long> generateUserIds(long size) {
        return range(0L, size)
                .boxed()
                .collect(toList());
    }

    /**
     * Generate output expected to be caught by {@link #subscribeEventListener(CardAssigner) event listener},
     * when each card from {@code album}'s {@code cards} is assigned to each user from {@code userIds} once.
     *
     * @return collection of expected events; for X users and Y cards in album,
     * this will contain X {@code Event.Type.ALBUM_FINISHED}
     * @see Event.Type
     */
    private Collection<Event> generateExpectedEvents(Collection<Long> userIds) {
        return userIds
                .stream()
                .map(userId -> new Event(userId, ALBUM_FINISHED))
                .collect(Collectors.toList());
    }

    /**
     * Subscribes a listener to {@code cardAssigner} that counts events it receives. The listener is thread-safe.
     *
     * @return listener's live storage, a mapping of received events to number of times this event is received
     */
    private Collection<Event> subscribeEventListener(CardAssigner cardAssigner) {
        Collection<Event> container = new ConcurrentLinkedDeque<>();
        cardAssigner.subscribe(container::add);
        return container;
    }

    /**
     * Assigns all cards from {@code album} to each user from {@code userIds} once concurrently, and waits for execution to finish
     */
    private void assignAllCardsToUsersOnceConcurrently(CardAssigner cardAssigner, Collection<Long> userIds, Album album) throws InterruptedException {
        ScheduledThreadPoolExecutor threadPoolExecutor = new ScheduledThreadPoolExecutor(10);
        threadPoolExecutor.prestartAllCoreThreads();
        CountDownLatch readySteadyGo = new CountDownLatch(1);

        userIds.parallelStream().forEach(
                userId -> album.cards.parallelStream().forEach(
                        card -> threadPoolExecutor.submit(
                                (Callable<Void>) () -> {
                                    readySteadyGo.await();
                                    cardAssigner.assignCard(userId, card.id);
                                    return null;
                                }
                        )
                )
        );

        readySteadyGo.countDown();

        threadPoolExecutor.shutdown();
        threadPoolExecutor.awaitTermination(20L, TimeUnit.SECONDS);
    }
    //</editor-fold>

}