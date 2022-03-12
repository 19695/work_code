package org.colm.code;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ThreadPoolUtil {

    public static final ExecutorService FIXED_POOL = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors() * 2);

}
