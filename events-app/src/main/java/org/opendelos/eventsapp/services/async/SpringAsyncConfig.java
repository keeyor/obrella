/* 
     Author: Michael Gatzonis - 12/5/2018 
     OpenDelosDAC
*/
package org.opendelos.eventsapp.services.async;

import java.util.concurrent.Executor;

import org.springframework.aop.interceptor.AsyncUncaughtExceptionHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.AsyncConfigurer;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

@EnableAsync()
public class SpringAsyncConfig implements AsyncConfigurer {

    @Bean(name = "threadPoolQueryWatcher")
    public Executor threadPoolQueryWatcher() {

        ThreadPoolTaskExecutor threadPoolTaskExecutor = new ThreadPoolTaskExecutor();
        threadPoolTaskExecutor.setCorePoolSize(0);
        threadPoolTaskExecutor.setMaxPoolSize(250);
        threadPoolTaskExecutor.setQueueCapacity(10);
        threadPoolTaskExecutor.setKeepAliveSeconds(60);
        threadPoolTaskExecutor.setThreadNamePrefix("QueryWatcher-");
        threadPoolTaskExecutor.initialize();
        return threadPoolTaskExecutor;
    }

    @Bean(name = "threadPoolQueryWorker")
    public Executor threadPoolQueryWorker() {

        ThreadPoolTaskExecutor threadPoolTaskExecutor = new ThreadPoolTaskExecutor();
        threadPoolTaskExecutor.setCorePoolSize(0);
        threadPoolTaskExecutor.setMaxPoolSize(250);
        threadPoolTaskExecutor.setQueueCapacity(10);
        threadPoolTaskExecutor.setKeepAliveSeconds(60);
        threadPoolTaskExecutor.setThreadNamePrefix("QueryWorker-");
        threadPoolTaskExecutor.initialize();
        return threadPoolTaskExecutor;
    }
    @Bean(name = "threadPoolFilterLookUpExecutor")
    public Executor threadPoolFilterLookUpExecutor() {

        ThreadPoolTaskExecutor threadPoolTaskExecutor = new ThreadPoolTaskExecutor();
        threadPoolTaskExecutor.setCorePoolSize(0);
        threadPoolTaskExecutor.setMaxPoolSize(250);
        threadPoolTaskExecutor.setQueueCapacity(10);
        threadPoolTaskExecutor.setKeepAliveSeconds(60);
        threadPoolTaskExecutor.setThreadNamePrefix("FilterLookup-");
        threadPoolTaskExecutor.initialize();
        return threadPoolTaskExecutor;

    }
    @Bean(name = "taskExecutor")
    public Executor taskExecutor()
    {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(5);
        executor.setMaxPoolSize(5);
        executor.setQueueCapacity(100);
        executor.setThreadNamePrefix("AsyncThread-");
        executor.initialize();
        return executor;
    }
    @Bean(name = "asyncAnalyzeExecutor")
    public Executor asyncAnalyzeExecutor()
    {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(5);
        executor.setMaxPoolSize(5);
        executor.setQueueCapacity(100);
        executor.setThreadNamePrefix("AsyncAnalyzeThread-");
        executor.initialize();
        return executor;
    }
    @Override
    public AsyncUncaughtExceptionHandler getAsyncUncaughtExceptionHandler() {
        return new CustomAsyncExceptionHandler();
    }
}