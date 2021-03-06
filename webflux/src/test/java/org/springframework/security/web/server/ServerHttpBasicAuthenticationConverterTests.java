/*
 * Copyright 2002-2017 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.security.web.server;

import org.junit.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import reactor.core.publisher.Mono;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author Rob Winch
 * @since 5.0
 */
public class ServerHttpBasicAuthenticationConverterTests {

	ServerHttpBasicAuthenticationConverter converter = new ServerHttpBasicAuthenticationConverter();
	MockServerHttpRequest.BaseBuilder<?> request = MockServerHttpRequest.get("/");

	@Test
	public void applyWhenNoAuthorizationHeaderThenEmpty() {
		Mono<Authentication> result = converter.apply(request.toExchange());

		assertThat(result.block()).isNull();
	}

	@Test
	public void applyWhenEmptyAuthorizationHeaderThenEmpty() {
		Mono<Authentication> result = converter.apply(request.header(HttpHeaders.AUTHORIZATION, "").toExchange());

		assertThat(result.block()).isNull();
	}

	@Test
	public void applyWhenOnlyBasicAuthorizationHeaderThenEmpty() {
		Mono<Authentication> result = converter.apply(request.header(HttpHeaders.AUTHORIZATION, "Basic ").toExchange());

		assertThat(result.block()).isNull();
	}

	@Test
	public void applyWhenNotBase64ThenEmpty() {
		Mono<Authentication> result = converter.apply(request.header(HttpHeaders.AUTHORIZATION, "Basic z").toExchange());

		assertThat(result.block()).isNull();
	}

	@Test
	public void applyWhenNoSemicolonThenEmpty() {
		Mono<Authentication> result = converter.apply(request.header(HttpHeaders.AUTHORIZATION, "Basic dXNlcg==").toExchange());

		assertThat(result.block()).isNull();
	}

	@Test
	public void applyWhenUserPasswordThenAuthentication() {
		Mono<Authentication> result = converter.apply(request.header(HttpHeaders.AUTHORIZATION, "Basic dXNlcjpwYXNzd29yZA==").toExchange());

		UsernamePasswordAuthenticationToken authentication = result.cast(UsernamePasswordAuthenticationToken.class).block();
		assertThat(authentication.getPrincipal()).isEqualTo("user");
		assertThat(authentication.getCredentials()).isEqualTo("password");
	}
}
