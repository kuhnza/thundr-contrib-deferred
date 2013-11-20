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

import com.threewks.thundr.deferred.provider.QueueProvider;
import com.threewks.thundr.deferred.serializer.JsonSerializer;
import com.threewks.thundr.deferred.serializer.TaskSerializer;
import com.threewks.thundr.deferred.task.DeferredTask;
import com.threewks.thundr.deferred.task.RetryableDeferredTask;
import com.threewks.thundr.logger.Logger;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

import java.util.List;

public class DeferredTaskService {
	private final QueueProvider queueProvider;
	private final TaskSerializer serializer = new TaskSerializer(new JsonSerializer());

	public DeferredTaskService(QueueProvider queueProvider) {
		this.queueProvider = queueProvider;
	}

	public QueueProvider getQueueProvider() {
		return queueProvider;
	}

	public void defer(DeferredTask deferredTask) {
		String serialized = serializer.serialize(deferredTask);
		queueProvider.send(serialized);
	}

	public void processQueue() {
		List<String> messages = queueProvider.receive();
		for (String message : messages) {
			run(message);
		}
	}

	private void run(String message) {
		DeferredTask deferredTask = null;
		try {
			deferredTask = serializer.deserialize(message);
			deferredTask.run();
		} catch (ClassNotFoundException e) {
			String errorMessage = "Unable to deserialize task from queue. Class %s not found.";
			Logger.error(errorMessage, e.getMessage());
			throw new ThundrDeferredException(e, errorMessage, e.getMessage());
		} catch (Exception e) {
			Logger.error("Running deferred task failed. Cause: %s", ExceptionUtils.getStackTrace(e));
			if (deferredTask != null && deferredTask instanceof RetryableDeferredTask) {
				Logger.info("Task is retryable, attempting to schedule retry...", e.getMessage());
				attemptRetry(((RetryableDeferredTask) deferredTask));
			} else {
				Logger.warn("Task is not retryable. Giving up!");
				throw new ThundrDeferredException(e, "Running deferred task failed permanently.");
			}
		}
	}

	private void attemptRetry(RetryableDeferredTask task) {
		if (task.shouldRetry()) {
			Logger.info("Scheduling retry #%s for task.", task.retries() + 1);
			defer(task);
		} else {
			String message = "Max retries[%s] exceeded. Giving up!";
			Logger.error(message, task.maxRetries());
			throw new ThundrDeferredException(message, task.maxRetries());
		}
	}
}
