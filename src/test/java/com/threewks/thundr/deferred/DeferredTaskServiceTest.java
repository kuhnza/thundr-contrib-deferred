/*
 * This file is a component of thundr, a software library from 3wks.
 * Read more: http://www.3wks.com.au/thundr
 * Copyright (C) 2013 3wks, <thundr@3wks.com.au>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.threewks.thundr.deferred;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.mockito.Mockito.*;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mockito.ArgumentCaptor;

import com.threewks.thundr.deferred.provider.InMemoryQueueProvider;
import com.threewks.thundr.deferred.provider.QueueProvider;
import com.threewks.thundr.deferred.serializer.JsonSerializer;
import com.threewks.thundr.deferred.serializer.TaskSerializer;
import com.threewks.thundr.deferred.test.MockDeferredTask;
import com.threewks.thundr.deferred.test.MockExceptionThrowingDeferredTask;
import com.threewks.thundr.deferred.test.MockRetryableDeferredTask;

public class DeferredTaskServiceTest {
	@Rule
	public ExpectedException thrown = ExpectedException.none();

	private QueueProvider queueProvider;
	private DeferredTaskService deferredTaskService;
	private TaskSerializer taskSerializer;

	@Before
	public void before() {
		taskSerializer = new TaskSerializer(new JsonSerializer());
		queueProvider = spy(new InMemoryQueueProvider());
		deferredTaskService = new DeferredTaskService(queueProvider);
	}

	@Test
	public void shouldSerializeTaskIntoMessage() {
		deferredTaskService.defer(new MockDeferredTask());

		ArgumentCaptor<String> captor = ArgumentCaptor.forClass(String.class);
		verify(queueProvider, times(1)).send(captor.capture());
		String message = taskSerializer.serialize(new MockDeferredTask());
		assertThat(captor.getValue(), equalTo(message));
	}

	@Test
	public void shouldThrowExceptionIfTaskClassNotFound() {
		thrown.expect(ThundrDeferredException.class);
		thrown.expectMessage("Unable to deserialize task from queue. Class foo.bar.MIA not found.");

		String message = "{\"taskClassName\":\"foo.bar.MIA\",\"taskData\":\"{\\\"field\\\":\\\"foo\\\"}\"}";
		queueProvider.send(message);
		deferredTaskService.processQueue();
	}

	@Test
	public void shouldWrapExceptionIfTaskThrowsUncheckedException() {
		thrown.expect(ThundrDeferredException.class);
		thrown.expectMessage("Running deferred task failed permanently. Reason: Expected baby!");

		String message = taskSerializer.serialize(new MockExceptionThrowingDeferredTask());
		queueProvider.send(message);
		deferredTaskService.processQueue();
	}

	@Test
	public void shouldRetryIfRetryableTaskThrowsUncheckedException() {
		int maxRetries = 5;
		String message = taskSerializer.serialize(new MockRetryableDeferredTask(maxRetries));
		queueProvider.send(message);
		for (int i = 0; i < maxRetries; i++) {
			deferredTaskService.processQueue();
		}
	}

	@Test
	public void shouldFailPermanentlyIfRetryableTaskExceedsMaxRetries() {
		thrown.expect(ThundrDeferredException.class);
		thrown.expectMessage("Max retries[2] exceeded. Giving up!");

		int maxRetries = 2;
		String message = taskSerializer.serialize(new MockRetryableDeferredTask(maxRetries));
		queueProvider.send(message);
		for (int i = 0; i < maxRetries; i++) {
			deferredTaskService.processQueue();
		}
	}
}
