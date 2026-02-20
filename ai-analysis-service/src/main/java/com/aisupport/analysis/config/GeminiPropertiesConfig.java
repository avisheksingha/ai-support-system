package com.aisupport.analysis.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.validation.annotation.Validated;

import io.netty.channel.ChannelOption;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.timeout.ReadTimeoutHandler;
import io.netty.handler.timeout.WriteTimeoutHandler;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.NoArgsConstructor;
import reactor.netty.http.client.HttpClient;
import reactor.netty.transport.logging.AdvancedByteBufFormat;

@Data
@NoArgsConstructor
@Validated
@Configuration
@ConfigurationProperties(prefix = "gemini")
public class GeminiPropertiesConfig {

    @NotBlank
    private String apiKey;

    @NotBlank
    private String apiUrl;

    @Min(1000)
    private int connectTimeoutMs = 10000;

    @Min(1000)
    private int responseTimeoutMs = 60000;

    @Min(1)
    private int maxConnections = 50;

    // Generation defaults
    private double temperature = 0.1;
    private int maxOutputTokens = 1500;
    private double topP = 0.8;
    private int topK = 40;

    /**
     * Configure the HttpClient with connection and response timeouts, and add logging handlers
     * This method can be used to apply the same configuration to multiple HttpClient instances if needed.
     * For example, if you have other WebClients or services that also need to call Gemini, you can reuse this method to ensure consistent timeout and logging settings.
     * 
     * Note: The timeouts are set in milliseconds, but the ReadTimeoutHandler and WriteTimeoutHandler expect seconds, so we divide by 1000.
     * @param httpClient
     * @return
     */
    public HttpClient configure(HttpClient httpClient) {
        return httpClient
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, connectTimeoutMs)
                .doOnConnected(conn -> conn
                        .addHandlerLast(new ReadTimeoutHandler(responseTimeoutMs / 1000))
                        .addHandlerLast(new WriteTimeoutHandler(responseTimeoutMs / 1000))
                )
                .wiretap("reactor.netty.http.client.HttpClient", LogLevel.INFO, AdvancedByteBufFormat.TEXTUAL);
    }
}
