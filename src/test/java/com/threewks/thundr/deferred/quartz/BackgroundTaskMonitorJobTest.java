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
package com.threewks.thundr.deferred.quartz;

import com.threewks.thundr.deferred.DeferredTaskService;
import com.threewks.thundr.deferred.monitor.quartz.QuartzQueueMonitorJob;
import com.threewks.thundr.deferred.provider.QueueProvider;
import com.threewks.thundr.injection.InjectionContextImpl;
import com.threewks.thundr.injection.UpdatableInjectionContext;
import org.junit.Before;
import org.junit.Test;
import org.quartz.JobExecutionException;

import java.util.Arrays;

import static org.mockito.Mockito.*;

public class BackgroundTaskMonitorJobTest {
	private QuartzQueueMonitorJob monitorJob;

	@Before
	public void before() {
		QueueProvider queueProvider = mock(QueueProvider.class);
		doReturn(Arrays.asList("{\"taskClassName\":\"com.threewks.thundr.deferred.test.MockDeferredTask\",\"taskData\":\"{\\\"field\\\":\\\"foo\\\"}\"}"))
				.when(queueProvider).receive();
		DeferredTaskService deferredTaskService = new DeferredTaskService(queueProvider);

		UpdatableInjectionContext injectionContext = new InjectionContextImpl();
		injectionContext.inject(deferredTaskService).as(DeferredTaskService.class);

		monitorJob = new QuartzQueueMonitorJob();
		monitorJob.setInjectionContext(injectionContext);
	}

	@Test
	public void shouldDeserializeTask() throws JobExecutionException {
		monitorJob.execute(null);  // Assertions inside
	}
}
