package com.toy;

import java.time.Duration;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClient.RequestBodySpec;

import io.netty.channel.ChannelOption;
import reactor.core.publisher.Mono;
import reactor.netty.http.client.HttpClient;
import reactor.netty.resources.ConnectionProvider;
import reactor.netty.tcp.TcpClient;

@Component
public class ApiClient {

	private final WebClient webClient;

	public ApiClient(@Qualifier("targetWebClient") WebClient webClient) {
		this.webClient = webClient;
	}

	public Mono<String> call() {
		return webClient
			.post()
			.uri("/go")
			.body(BodyInserters.fromValue("hi"))
			.retrieve()
			.bodyToMono(String.class)
			.onErrorResume(throwable -> Mono.error(new RuntimeException(throwable)));
	}
}
