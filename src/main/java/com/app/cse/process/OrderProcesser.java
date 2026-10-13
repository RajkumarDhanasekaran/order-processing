package com.app.cse.process;

import com.app.cse.pojo.OrderEvent;
import com.app.cse.pojo.Summary;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.List;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.LinkedBlockingQueue;

public class OrderProcesser {

    private final ObjectMapper mapper = new ObjectMapper();

    private final Map<String, BlockingQueue<OrderEvent>> queues =
            new ConcurrentHashMap<>();

    private final Map<String, Summary> summaries =
            new ConcurrentHashMap<>();


    // PRODUCER
    public void process(String json) {
        try {
            // Parse JSON to identify the source
            OrderEvent event = mapper.readValue(json, OrderEvent.class);

            String source = event.source();
            String symbols = event.source();

            // Create one queue and consumer for each source
            BlockingQueue<OrderEvent> queue =
                    queues.computeIfAbsent(symbols, s -> {
                        BlockingQueue<OrderEvent> q =
                                new LinkedBlockingQueue<>();

                        Thread consumer = new Thread(
                                () -> consume(s, q),
                                "consumer-" + s
                        );

                        consumer.setDaemon(true);
                        consumer.start();

                        return q;
                    });

            // Publish the event to the source-specific queue
            queue.put(event);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // CONSUMER
    private void consume(String symbols,
                         BlockingQueue<OrderEvent> queue) {

        try {
            while (true) {
                // Wait until an event is available
                OrderEvent event = queue.take();

                boolean buy = event.type().equals("BUY");

                Summary newSummary = new Summary(
                        buy ? event.quantity() : 0,
                        buy ? 0 : event.quantity(),
                        event.price()
                );

                summaries.merge(symbols, newSummary,
                        (old, next) -> new Summary(
                                old.buyQuantity()
                                        + next.buyQuantity(),
                                old.sellQuantity()
                                        + next.sellQuantity(),
                                next.lastPrice()
                        ));

                System.out.println(
                        Thread.currentThread().getName()
                                + " processed " + event.type()
                                + " from " + symbols
                );
            }

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    public Map<String, Summary> getSummaries() {
        return summaries;
    }

}
