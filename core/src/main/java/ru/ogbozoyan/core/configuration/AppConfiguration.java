package ru.ogbozoyan.core.configuration;

import io.micrometer.context.ContextExecutorService;
import io.micrometer.context.ContextSnapshot;
import io.micrometer.context.ContextSnapshotFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.reactive.function.client.ExchangeStrategies;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.servlet.config.annotation.AsyncSupportConfigurer;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.time.Duration;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;

@Configuration
public class AppConfiguration {

    @Value("${app.descew.api-url}")
    private String descewApiUrl;

    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplateBuilder()
            .connectTimeout(Duration.ofMinutes(1))
            .build();
    }

    @Bean(name = "descewWebClient")
    public WebClient descewWebClient() {
        return WebClient.builder()
            .baseUrl(descewApiUrl)
            .exchangeStrategies(ExchangeStrategies.builder()
                .codecs(configurer -> configurer
                    .defaultCodecs()
                    .maxInMemorySize(1024 * 1024 * 1024))
                .build())
            .build();
    }

    @Bean(name = "uploadExecutor")
    public Executor uploadExecutor() {
        ThreadFactory factory = Thread.ofVirtual()
            .name("virtual-upload-", 1)
            .factory();
        ExecutorService executorService = Executors.newThreadPerTaskExecutor(factory);
        return wrap(executorService);
    }

    public static ExecutorService wrap(ExecutorService executorService) {
        executorService = ContextExecutorService.wrap(executorService, ContextSnapshotFactory.builder().build());
        return executorService;
    }
}
