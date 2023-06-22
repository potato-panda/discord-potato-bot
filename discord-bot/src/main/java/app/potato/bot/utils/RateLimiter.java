package app.potato.bot.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.*;

public final
class RateLimiter {

    private static final Logger                   logger
            = LoggerFactory.getLogger( RateLimiter.class );
    private final        int                      capacity;
    private final        long                     refillPeriodMillis;
    private final        Semaphore                tokens;
    private final        ScheduledExecutorService executorService;
    private              ScheduledFuture<?>       resupplyTask;

    public
    RateLimiter( int capacity,
                 int refillPeriodMillis )
    {
        this.capacity           = capacity;
        this.refillPeriodMillis = refillPeriodMillis;
        this.tokens             = new Semaphore( capacity );
        this.executorService    = Executors.newScheduledThreadPool( 1 );
        this.resupplyTask       = resupplyTokens();
    }

    private
    ScheduledFuture<?> resupplyTokens() {
        return executorService.scheduleAtFixedRate( this::refillingTask,
                                                    0,
                                                    refillPeriodMillis,
                                                    TimeUnit.MILLISECONDS );
    }

    private
    void refillingTask() {
        int tokensToRefill = capacity - tokens.availablePermits();

        tokens.release( tokensToRefill );
    }

    public
    void acquire() throws InterruptedException {
        while ( !this.tokens.tryAcquire() ) {
            Thread.sleep( 100 );
            this.tokens.acquire();
        }
//        acquire( 1 );
    }

//    public
//    void acquire( int requests ) throws InterruptedException {
//        if ( requests < 1 ) {
//            requests = 1;
//        }
//        if ( tokens.tryAcquire( requests ) ) {
//            resupplyTask.cancel( false );
//            tokens.acquire( requests );
//            resupplyTask = resupplyTokens();
//        } else {
//            Thread.sleep( 100 );
//            acquire( requests );
//        }
//    }

    public
    void stop() {
        resupplyTask.cancel( true );
        executorService.shutdown();
    }
}
