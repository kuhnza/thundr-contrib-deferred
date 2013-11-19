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
package com.threewks.thundr.deferred.monitor.quartz;

import com.threewks.thundr.deferred.DeferredTaskService;
import com.threewks.thundr.deferred.serializer.JsonSerializer;
import com.threewks.thundr.deferred.serializer.TaskSerializer;
import com.threewks.thundr.deferred.task.RetryableDeferredTask;
import com.threewks.thundr.deferred.task.DeferredTask;
import com.threewks.thundr.injection.InjectionContext;
import com.threewks.thundr.logger.Logger;
import com.threewks.thundr.quartz.BaseQuartzJob;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

import java.util.List;

public class QuartzQueueMonitorJob extends BaseQuartzJob {
	private DeferredTaskService deferredTaskService;

	@Override
	public void setInjectionContext(InjectionContext injectionContext) {
		super.setInjectionContext(injectionContext);
		deferredTaskService = injectionContext.get(DeferredTaskService.class);
	}

	@Override
	public void execute(JobExecutionContext context) throws JobExecutionException {
		try {
			deferredTaskService.processQueue();
		} catch (Exception e) {
			String msg = String.format("An unexpected error occurred running deferred tasks. Reason: %s", e.getMessage());
			throw new JobExecutionException(msg, e);
		}
	}
}
