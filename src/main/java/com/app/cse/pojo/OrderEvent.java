package com.app.cse.pojo;

public record OrderEvent(long orderId, String symbol, String type, long quantity, double price, long timeStamp, String source) {

}
