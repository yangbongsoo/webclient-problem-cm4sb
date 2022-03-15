package com.toy;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class ProblemComponent implements InitializingBean {

	private final ApiClient apiClient;

	// TODO: problem
	@Override
	public void afterPropertiesSet() {
		System.out.println(Thread.currentThread().getName() +" : Targets afterPropertiesSet");
		try {
			callTarget();
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	private void callTarget() {
		String responseBody = apiClient.call().block();
		System.out.println(responseBody);
	}
}
