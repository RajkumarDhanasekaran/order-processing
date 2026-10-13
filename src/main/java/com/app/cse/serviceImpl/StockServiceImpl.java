package com.app.cse.serviceImpl;

import com.app.cse.StockService;
import com.app.cse.pojo.OrderEvent;
import com.app.cse.pojo.Summary;
import com.app.cse.process.OrderProcesser;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicLong;

@Service
public class StockServiceImpl implements StockService {

    private Map<String, Summary> summeries = new ConcurrentHashMap<>();
    AtomicLong orderId = new AtomicLong();
    String[] symbols = {"AAPL", "XXYZ", "UEJW", "EOWE"};
    String[] sources = {"5685", "7859", "3658", "2147"};
    Random random = new Random();
    ExecutorService service = Executors.newFixedThreadPool(4);
    ObjectMapper mapper = new ObjectMapper();
    List<OrderEvent> orderEvents = new ArrayList<>();

    @Scheduled(fixedRate = 100)
    public void generateOrder() {
        String json = "{\"orderId\":" + (orderId.incrementAndGet() + 1)
                + ",\"symbol\":\"" + symbols[random.nextInt(symbols.length)]
                + "\",\"type\":\"" + (random.nextBoolean() ? "BUY" : "SELL")
                + "\",\"quantity\":" + random.nextInt(500)
                + ",\"price\":" + random.nextInt(100)
                + ",\"timeStamp\":" + System.currentTimeMillis()
                + ",\"source\":\"" + sources[random.nextInt(sources.length)] + "\"}";

        OrderProcesser orderProcessor = new OrderProcesser();
        service.submit(() -> orderProcessor.process(json));
    }

    @Scheduled(fixedRate = 5000)
    public void printTop5() {
        System.out.println("----- Print Top 5 summary based on Source");
        summeries.entrySet().stream().sorted(Comparator.comparingLong((Map.Entry<String, Summary> sum) ->
                sum.getValue().buyQuantity() + sum.getValue().sellQuantity()).reversed()).limit(5)
                .forEach(obj -> System.out.println(obj.getKey() + " ----  "+ obj.getValue()));


    }

    /*public void getBySource() {
        summeries.entrySet().stream().filter(f -> f.getValue())
    }*/


    @Override
    public Map<String, Summary> getSummary() {
        return summeries;
    }
}
