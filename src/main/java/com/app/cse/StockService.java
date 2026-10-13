package com.app.cse;

import com.app.cse.pojo.Summary;

import java.util.Map;

public interface StockService {
    Map<String, Summary> getSummary();
}
