package com.unajo.vt;

import java.util.ArrayList;
import java.util.Map;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.commons.logging.LogFactory;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.actuate.autoconfigure.wavefront.WavefrontProperties.Application;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.web.client.RestClient;
import org.springframework.web.servlet.function.HandlerFunction;
import org.springframework.web.servlet.function.RouterFunction;
import org.springframework.web.servlet.function.ServerResponse;
import static org.springframework.web.servlet.function.RouterFunctions.route;

@SpringBootApplication
public class VtApplication {

	@Bean
	RestClient restClient(RestClient.Builder builder, @Value("${httpbin.url}") String url) {

		return builder.baseUrl(url).build();
	}

	@Bean
	RouterFunction<ServerResponse> httpEndpoints(RestClient restClient) {

		var log = LoggerFactory.getLogger(getClass());

		return route()
				.GET("/delay/{seconds}", request -> {
					var seconds = request.pathVariable("seconds");
					log.info("seconds: {}", seconds);
					var requestToHttpBin = restClient
							.get()
							.uri("/delay/" + seconds)
							.retrieve()
							.toEntity(String.class);

					log.info("{} on {}}" + requestToHttpBin.getStatusCode(),
							Thread.currentThread());

					return ServerResponse.ok().body(Map.of("done", true));
				})
				.GET("/test", request -> {

					return ServerResponse.ok().body(Map.of("Test", true));
				})
				.build();
	}

	// @Bean
	ApplicationRunner runner() {
		return args -> {
			var executorService = Executors.newVirtualThreadPerTaskExecutor();
			executorService.execute(() -> System.out.println("Hello from thread " + Thread.currentThread().toString()));
		};
	}

	public static void main(String[] args) throws Exception {

		SpringApplication.run(VtApplication.class, args);
	}
}
