package com.toy.config;

import java.util.concurrent.TimeUnit;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.http.client.reactive.ReactorResourceFactory;
import org.springframework.web.reactive.function.client.WebClient;

import com.toy.chaos.AssaultInjectionService;

import io.netty.channel.ChannelOption;
import io.netty.handler.timeout.ReadTimeoutHandler;
import lombok.RequiredArgsConstructor;
import reactor.netty.http.client.HttpClient;

@Configuration
@RequiredArgsConstructor
public class WebClientConfig {

	private final AssaultInjectionService assaultInjectionService;

	@Bean
	WebClient targetWebClient(
		ReactorResourceFactory reactorResourceFactory,
		WebClient.Builder webClientBuilder
	) {
		return webClientBuilder.baseUrl("http://localhost:8082")
			.clientConnector(
				this.customizeReactorClientHttpConnector(
					reactorResourceFactory,
					2_000,
					2_000,
					// () -> {
					// 	System.out.println("monkey");
					// }
					assaultInjectionService::assaultlambdaBetaUsersWebClient
				)
			)
			.build();
	}

	private ReactorClientHttpConnector customizeReactorClientHttpConnector(
		ReactorResourceFactory reactorResourceFactory,
		int connectionTimeoutMillis,
		int readTimeoutMillis,
		Runnable assaultWebClient
	) {
		return new ReactorClientHttpConnector(reactorResourceFactory, defaultHttpClient -> {
			HttpClient httpClient = defaultHttpClient
				.option(ChannelOption.CONNECT_TIMEOUT_MILLIS, connectionTimeoutMillis)
				.doOnConnected(connection -> {
					connection.addHandlerLast(
						new ReadTimeoutHandler(readTimeoutMillis, TimeUnit.MILLISECONDS)
					);
					assaultWebClient.run();
					this.assaultInjectionService.assaultAllWebClient();
				});
			return httpClient;
		});
	}
}
