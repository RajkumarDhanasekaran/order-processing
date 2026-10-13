package com.app.cse.controller;

import com.app.cse.StockService;
import com.app.cse.pojo.Summary;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
public class StockController {

    @Autowired
    StockService service;

    @GetMapping("/summary")
    public Map<String, Summary> summary() {
        return service.getSummary();
    }

}
