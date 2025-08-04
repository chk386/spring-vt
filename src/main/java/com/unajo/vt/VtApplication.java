package com.unajo.vt;

import static org.springframework.web.servlet.function.RouterFunctions.route;

import java.util.Map;
import java.util.concurrent.Executors;

import javax.sql.DataSource;

import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.config.environment.Environment;
import org.springframework.cloud.config.server.EnableConfigServer;
import org.springframework.context.annotation.Bean;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.servlet.function.RouterFunction;
import org.springframework.web.servlet.function.ServerResponse;

import com.google.protobuf.Empty;
import com.unajo.adoptions.AdoptionsGrpc;
import com.unajo.adoptions.DelayRequest;
import com.unajo.adoptions.DelayResponse;
import com.unajo.adoptions.Dog;
import com.unajo.adoptions.DogsResponse;

import io.grpc.stub.StreamObserver;

@Service
class DogAdoptionsGrpcService extends AdoptionsGrpc.AdoptionsImplBase {

	private final RestClient restClient;

	public DogAdoptionsGrpcService(RestClient restClient) {
		this.restClient = restClient;
	}

	@Override
	public void all(Empty request, StreamObserver<DogsResponse> responseObserver) {

		var dog = com.unajo.adoptions.Dog.newBuilder()
				.setId(1)
				.setName("bark")
				.setDescription("정말 귀엽고 사랑스럽다")
				.build();

		var response = DogsResponse.newBuilder()
				.addDogs(dog)
				.build();

		responseObserver.onNext(response);
		responseObserver.onCompleted();
	}

	@Override
	public void delay(com.unajo.adoptions.DelayRequest request,
			StreamObserver<com.unajo.adoptions.DelayResponse> responseObserver) {

		var seconds = request.getSeconds();
		var log = org.slf4j.LoggerFactory.getLogger(getClass());

		try {
			log.info("gRPC delay request for {} seconds", seconds);

			// REST API 호출과 동일한 로직
			var requestToHttpBin = restClient
					.get()
					.uri("/delay/" + seconds)
					.retrieve()
					.toEntity(String.class);

			log.info("gRPC delay completed with status: {} on thread: {}",
					requestToHttpBin.getStatusCode(), Thread.currentThread());

			var response = com.unajo.adoptions.DelayResponse.newBuilder()
					.setDone(true)
					.setMessage("Delay of " + seconds + " seconds completed successfully")
					.build();

			responseObserver.onNext(response);
			responseObserver.onCompleted();

		} catch (Exception e) {
			log.error("Error in gRPC delay: ", e);

			var errorResponse = com.unajo.adoptions.DelayResponse.newBuilder()
					.setDone(false)
					.setMessage("Error: " + e.getMessage())
					.build();

			responseObserver.onNext(errorResponse);
			responseObserver.onCompleted();
		}
	}
}

@SpringBootApplication
@EnableConfigServer
public class VtApplication {

	@Bean
	RestClient restClient(RestClient.Builder builder, @Value("${httpbin.url}") String url) {

		return builder.baseUrl(url).build();
	}

	// @Bean
	// ApplicationRunner runner(Environment environment) {
	// return _ -> {

	// System.out.println(environment.toString());
	// if (environment instanceof ConfigurableEnvironment environment1) {
	// environment1.getPropertySources().stream().iterator().forEachRemaining(System.out::println);
	// }
	// };
	// }

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
				.build();
	}

	// @Bean
	ApplicationRunner runner() {
		return _ -> {
			var executorService = Executors.newVirtualThreadPerTaskExecutor();
			executorService
					.execute(() -> System.out.println("Hello from thread " + Thread.currentThread().toString()));
		};
	}

	public static void main(String[] args) throws Exception {
		var c = new MyConfig();
		var customerService = c.customerService();

		SpringApplication.run(VtApplication.class, args);
	}
}

class CustomerService {
	private final DataSource dataSource;

	CustomerService(DataSource dataSource) {
		this.dataSource = dataSource;
	}

	public DataSource getDataSource() {
		return this.dataSource;
	}
}

class MyConfig {

	CustomerService customerService() {
		var ds = this.dataSource();
		return new CustomerService(ds);
	}

	DataSource dataSource() {
		return null;
	}
}
