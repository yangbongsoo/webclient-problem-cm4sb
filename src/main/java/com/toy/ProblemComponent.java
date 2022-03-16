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
		System.out.println("ProblemComponent : Targets afterPropertiesSet start");
		try {
			String responseBody = apiClient.call().block();
			System.out.println(responseBody);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
}
