package com.toy.config;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;

import org.springframework.boot.actuate.health.AbstractHealthIndicator;
import org.springframework.boot.actuate.health.Health.Builder;
import org.springframework.boot.context.event.ApplicationStartedEvent;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationListener;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import reactor.core.publisher.Mono;

@Configuration
public class MyHealthIndicatorConfig {

	@Bean("warmUp")
	MyHealthIndicator myHealthIndicator(ApplicationContext context) {
		MyChecker myChecker = new MyChecker(warmUpSupplier(context));
		return new MyHealthIndicator(myChecker, (ConfigurableApplicationContext)context);
	}

	private Supplier<Boolean> warmUpSupplier(ApplicationContext context) {
		return () -> {
			Boolean enabled = context.getEnvironment().getProperty("warmup.enabled", Boolean.class, false);
			System.out.println("warmUpSupplier enabled : " + enabled);
			if (!enabled) {
				return true;
			}

			System.out.println(Thread.currentThread().getName() + ": start warm up");

			callDB();

			try {
				callExternalAPI()
					.toFuture()
					.get();
			} catch (Exception ex) {
				// just log and ignore
			}

			System.out.println(Thread.currentThread().getName() + " : end warm up");
			return true;
		};
	}

	private Mono<String> callExternalAPI() {
		// TODO
		return Mono.just("");
	}

	private void callDB() {
		// TODO
	}

	private static class MyHealthIndicator extends AbstractHealthIndicator {
		private final MyChecker checker;

		public MyHealthIndicator(MyChecker checker, ConfigurableApplicationContext context) {
			this.checker = checker;
			context.addApplicationListener(checker);
		}

		@Override
		protected void doHealthCheck(Builder builder) {
			if (this.checker.isOk()) {
				builder.up();
			} else {
				builder.outOfService();
			}
		}
	}

	private static class MyChecker implements ApplicationListener<ApplicationStartedEvent> {
		private final Object lockObject = new Object();
		private final Supplier<Boolean> supplier;
		private final AtomicBoolean checked = new AtomicBoolean(false);
		private final ExecutorService executorService;

		public MyChecker(Supplier<Boolean> supplier) {
			this.supplier = supplier;
			this.executorService = Executors.newFixedThreadPool(5);
		}

		@Override
		public void onApplicationEvent(ApplicationStartedEvent event) {
			executorService.submit(this::check);
		}

		public boolean isOk() {
			executorService.submit(this::check);
			return this.isAlreadyChecked();
		}

		private void check() {
			synchronized (this.lockObject) {
				if (isAlreadyChecked()) {
					return;
				}

				try {
					if (this.supplier.get()) {
						this.checked.set(true);
						return;
					}
				} catch (Exception ex) {
					this.checked.set(false);
					return;
				}

				this.checked.set(false);
			}
		}

		private boolean isAlreadyChecked() {
			return this.checked.get();
		}
	}
}
