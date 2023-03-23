package app.potato.bot.services;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.*;

public
class RateLimiter {

    private static final Logger                   logger
            = LoggerFactory.getLogger( RateLimiter.class );
    private final        int                      capacity;
    private final        long                     refillPeriodMillis;
    private final        Semaphore                tokens;
    private final        ScheduledExecutorService executorService;
    private final        ScheduledFuture<?>       resupplyTask;

    public
    RateLimiter( int capacity,
                 int refillPeriodMillis ) throws InterruptedException
    {
        this.capacity           = capacity;
        this.refillPeriodMillis = refillPeriodMillis;
        this.tokens             = new Semaphore( capacity );
        this.executorService    = Executors.newScheduledThreadPool( 1 );
        this.resupplyTask       = resupplyTokens();
    }

    private
    ScheduledFuture<?> resupplyTokens() {
        return executorService.scheduleAtFixedRate( () -> {
                                                        int tokensToRefill = capacity - tokens.availablePermits();

                                                        tokens.release( tokensToRefill );
                                                    },
                                                    0,
                                                    refillPeriodMillis,
                                                    TimeUnit.MILLISECONDS );
    }

    public
    void acquire() throws InterruptedException {
        while ( !this.tokens.tryAcquire() ) {
            Thread.sleep( 100 );
        }
        this.tokens.acquire();
    }

    public
    void stop() {
        resupplyTask.cancel( true );
        executorService.shutdown();
    }
}
