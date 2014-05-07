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
import static org.hamcrest.Matchers.*;
import static org.hamcrest.core.Is.is;
import static org.mockito.Mockito.mock;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import com.threewks.thundr.deferred.monitor.MockQueueMonitor;
import com.threewks.thundr.deferred.monitor.QueueMonitor;
import com.threewks.thundr.deferred.monitor.quartz.QuartzQueueMonitor;
import com.threewks.thundr.deferred.provider.InMemoryQueueProvider;
import com.threewks.thundr.deferred.provider.MockQueueProvider;
import com.threewks.thundr.deferred.provider.QueueProvider;
import com.threewks.thundr.injection.InjectionContextImpl;
import com.threewks.thundr.injection.UpdatableInjectionContext;
import com.threewks.thundr.quartz.QuartzScheduler;

public class DeferredModuleTest {
	@Rule
	public ExpectedException thrown = ExpectedException.none();

	private DeferredModule module;
	private UpdatableInjectionContext injectionContext;

	@Before
	public void before() {
		injectionContext = new InjectionContextImpl();
		injectionContext.inject(mock(QuartzScheduler.class)).as(QuartzScheduler.class);
		module = new DeferredModule();
	}

	@Test
	public void shouldConfigureDefaultQueueProvider() {
		module.configure(injectionContext);

		QueueProvider queueProvider = injectionContext.get(QueueProvider.class);
		assertThat(queueProvider, is(instanceOf(InMemoryQueueProvider.class)));
	}

	@Test
	public void shouldConfigureDefaultQueueMonitor() {
		module.configure(injectionContext);

		QueueMonitor queueMonitor = injectionContext.get(QueueMonitor.class);
		assertThat(queueMonitor, is(instanceOf(QuartzQueueMonitor.class)));
	}

	@Test
	public void shouldOverrideDefaultQueueProvider() {
		injectionContext.inject("com.threewks.thundr.deferred.provider.MockQueueProvider").named("deferredQueueProvider").as(String.class);
		module.configure(injectionContext);

		QueueProvider queueProvider = injectionContext.get(QueueProvider.class);
		assertThat(queueProvider, is(instanceOf(MockQueueProvider.class)));
	}

	@Test
	public void shouldFailWhenOverriddenQueueProviderClassDoesNotExist() {
		thrown.expect(ThundrDeferredException.class);
		thrown.expectMessage("No such queue provider: com.threewks.thundr.deferred.provider.FakeQueueProvider. Is it on the classpath?");

		injectionContext.inject("com.threewks.thundr.deferred.provider.FakeQueueProvider").named("deferredQueueProvider").as(String.class);
		module.configure(injectionContext);
	}

	@Test
	public void shouldFailWhenOverriddenQueueProviderIsNotSubclassOfQueueProvider() {
		thrown.expect(ThundrDeferredException.class);
		thrown.expectMessage("Queue provider must implement " + QueueProvider.class.getName());

		injectionContext.inject("com.threewks.thundr.deferred.monitor.MockQueueMonitor").named("deferredQueueProvider").as(String.class);
		module.configure(injectionContext);
	}

	@Test
	public void shouldOverrideDefaultQueueMonitor() {
		injectionContext.inject("com.threewks.thundr.deferred.monitor.MockQueueMonitor").named("deferredQueueMonitor").as(String.class);
		module.configure(injectionContext);

		QueueMonitor queueMonitor = injectionContext.get(QueueMonitor.class);
		assertThat(queueMonitor, is(instanceOf(MockQueueMonitor.class)));
	}

	@Test
	public void shouldFailWhenOverriddenQueueMonitorClassDoesNotExist() {
		thrown.expect(ThundrDeferredException.class);
		thrown.expectMessage("No such queue monitor: com.threewks.thundr.deferred.monitor.FakeQueueMonitor. Is it on the classpath?");

		injectionContext.inject("com.threewks.thundr.deferred.monitor.FakeQueueMonitor").named("deferredQueueMonitor").as(String.class);
		module.configure(injectionContext);
	}

	@Test
	public void shouldFailWhenOverriddenQueueMonitorIsNotSubclassOfQueueMonitor() {
		thrown.expect(ThundrDeferredException.class);
		thrown.expectMessage("Queue monitor must implement " + QueueMonitor.class.getName());

		injectionContext.inject("com.threewks.thundr.deferred.provider.MockQueueProvider").named("deferredQueueMonitor").as(String.class);
		module.configure(injectionContext);
	}

	@Test
	public void shouldInjectDeferredTaskService() {
		module.configure(injectionContext);

		DeferredTaskService service = injectionContext.get(DeferredTaskService.class);
		assertThat(service, is(notNullValue()));
	}

	@Test
	public void shouldStartQueueMonitor() {
		injectionContext.inject(MockQueueMonitor.class.getName()).named("deferredQueueMonitor").as(String.class);
		module.configure(injectionContext);
		module.start(injectionContext);

		MockQueueMonitor monitor = (MockQueueMonitor) injectionContext.get(QueueMonitor.class);
		assertThat(monitor.startCalled, is(true));
	}
}
