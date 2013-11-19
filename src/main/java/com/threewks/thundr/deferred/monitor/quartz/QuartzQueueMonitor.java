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

import com.threewks.thundr.deferred.Defaults;
import com.threewks.thundr.deferred.monitor.QueueMonitor;
import com.threewks.thundr.quartz.QuartzScheduler;
import org.quartz.*;

public class QuartzQueueMonitor implements QueueMonitor {
	private QuartzScheduler scheduler;
	private int monitorInterval = Defaults.QueueMonitorInterval;

	public QuartzQueueMonitor(QuartzScheduler scheduler) {
		this.scheduler = scheduler;
	}

	public QuartzQueueMonitor(QuartzScheduler scheduler, String deferredQueueMonitorInterval) {
		this(scheduler);
		monitorInterval = Integer.parseInt(deferredQueueMonitorInterval);
	}

	@Override
	public void start() {
		JobDetail monitorJob = JobBuilder.newJob(QuartzQueueMonitorJob.class)
				.withIdentity("QueueMonitorJob")
				.build();

		Trigger trigger = TriggerBuilder
				.newTrigger()
				.withIdentity("QueueMonitorJobTrigger")
				.withSchedule(
						SimpleScheduleBuilder.simpleSchedule()
								.withIntervalInSeconds(monitorInterval)
								.repeatForever())
				.build();

		scheduler.scheduleJob(monitorJob, trigger);
	}
}
