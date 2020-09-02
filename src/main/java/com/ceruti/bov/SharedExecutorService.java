package com.ceruti.bov;

import lombok.Data;
import org.springframework.stereotype.Component;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Component
public class SharedExecutorService {

    ExecutorService executorService = Executors.newFixedThreadPool(50);

    public ExecutorService getExecutorService() {
        return executorService;
    }
}
