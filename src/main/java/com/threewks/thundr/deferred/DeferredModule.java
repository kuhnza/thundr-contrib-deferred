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

import com.threewks.thundr.deferred.monitor.QueueMonitor;
import com.threewks.thundr.deferred.provider.QueueProvider;
import com.threewks.thundr.injection.InjectionContext;
import com.threewks.thundr.injection.Module;
import com.threewks.thundr.injection.UpdatableInjectionContext;
import com.threewks.thundr.logger.Logger;
import com.threewks.thundr.module.DependencyRegistry;
import com.threewks.thundr.quartz.QuartzModule;

public class DeferredModule implements Module {
	@Override
	public void requires(DependencyRegistry dependencyRegistry) {
		dependencyRegistry.addDependency(QuartzModule.class);
	}

	@Override
	public void initialise(UpdatableInjectionContext injectionContext) {
	}

	@Override
	public void configure(UpdatableInjectionContext injectionContext) {
		addQueueProvider(injectionContext);
		addQueueMonitor(injectionContext);

		injectionContext.inject(DeferredTaskService.class).as(DeferredTaskService.class);
	}

	@Override
	public void start(UpdatableInjectionContext injectionContext) {
		Logger.info("Starting queue monitor...");
		injectionContext.get(QueueMonitor.class).start();
		Logger.info("Queue monitor started OK.");
	}

	@Override
	public void stop(InjectionContext injectionContext) {
	}

	@SuppressWarnings("unchecked")
	private void addQueueProvider(UpdatableInjectionContext injectionContext) {
		String queueProviderClassName = getProperty(String.class, injectionContext, "deferredQueueProvider", Defaults.QueueProvider);
		try {
			Class<?> type = Class.forName(queueProviderClassName);
			if (QueueProvider.class.isAssignableFrom(type)) {
				Logger.info("Set deferred queue provider to %s", queueProviderClassName);
				injectionContext.inject((Class<QueueProvider>) type).as(QueueProvider.class);
			} else {
				String message = "Queue provider must implement %s";
				String className = QueueProvider.class.getName();
				Logger.error(message, className);
				throw new ThundrDeferredException(message, className);
			}
		} catch (ClassNotFoundException e) {
			String message = "No such queue provider: %s. Is it on the classpath?";
			Logger.error(message, queueProviderClassName);
			throw new ThundrDeferredException(e, message, queueProviderClassName);
		}
	}

	@SuppressWarnings("unchecked")
	private void addQueueMonitor(UpdatableInjectionContext injectionContext) {
		String queueMonitorClassName = getProperty(
				String.class, injectionContext, "deferredQueueMonitor", Defaults.QueueMonitor);
		try {
			Class<?> type = Class.forName(queueMonitorClassName);
			if (QueueMonitor.class.isAssignableFrom(type)) {
				Logger.info("Set deferred queue monitor to %s", queueMonitorClassName);
				injectionContext.inject((Class<QueueMonitor>) type).as(QueueMonitor.class);
			} else {
				String message = "Queue monitor must implement %s.";
				String className = QueueMonitor.class.getName();
				Logger.error(message, className);
				throw new ThundrDeferredException(message, className);
			}
		} catch (ClassNotFoundException e) {
			String message = "No such queue monitor: %s. Is it on the classpath?";
			Logger.error(message, queueMonitorClassName);
			throw new ThundrDeferredException(e, message, queueMonitorClassName);
		}
	}

	@SuppressWarnings("unchecked")
	private <T> T getProperty(Class<T> type, InjectionContext injectionContext, String name, Object defaultTo) {
		T value = (T) injectionContext.get(String.class, name);
		if (value == null) {
			Logger.warn("Property %s missing from injection context. Defaulting to: %s", name, String.valueOf(defaultTo));
			value = (T) defaultTo;
		}
		return value;
	}
}
