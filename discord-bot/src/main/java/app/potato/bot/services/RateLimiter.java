package app.potato.bot.services;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.*;

public
class RateLimiter {

    private static final Logger                   logger
            = LoggerFactory.getLogger( RateLimiter.class );
    private final        int                      capacity;
    private final        long                     rechargePeriodMillis;
    private final        Semaphore                tokens;
    private final        ScheduledExecutorService executorService;
    private              ScheduledFuture<?>       resupplyTask;

    public
    RateLimiter( int capacity,
                 int rechargePeriodMillis ) throws InterruptedException
    {
        this.capacity             = capacity;
        this.rechargePeriodMillis = rechargePeriodMillis;
        this.tokens               = new Semaphore( capacity );
        this.executorService      = Executors.newScheduledThreadPool( 1 );
        this.resupplyTask         = resupplyTokens();
    }

    private
    ScheduledFuture<?> resupplyTokens() {
        return executorService.scheduleAtFixedRate( () -> {
                                                        int tokensToResupply
                                                                = capacity - tokens.availablePermits();

                                                        tokens.release( tokensToResupply );

                                                        try {
                                                            Thread.sleep( 150 );
                                                        }
                                                        catch ( InterruptedException e ) {
                                                            logger.info( "Rate Limiter Sleep exception : {}",
                                                                         e.getMessage() );
                                                        }
                                                    },
                                                    0,
                                                    rechargePeriodMillis,
                                                    TimeUnit.MILLISECONDS );
    }

    public
    void acquire() throws InterruptedException {
        tokens.acquire();
    }

    public
    void stop() {
        resupplyTask.cancel( true );
        executorService.shutdown();
    }
}
