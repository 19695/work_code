package org.colm.code;

import java.util.concurrent.*;

public class ThreadPoolUtil {

    private static final int PROCESSORS_NUM = Runtime.getRuntime().availableProcessors();

//    public static final ExecutorService FIXED_POOL = Executors.newFixedThreadPool(PROCESSORS_NUM * 2);

    public static final ThreadPoolExecutor EXECUTOR = new ThreadPoolExecutor(PROCESSORS_NUM, PROCESSORS_NUM * 2,
            1L, TimeUnit.MILLISECONDS, new LinkedBlockingQueue(1024),
            Executors.defaultThreadFactory(), new ThreadPoolExecutor.AbortPolicy());

}
