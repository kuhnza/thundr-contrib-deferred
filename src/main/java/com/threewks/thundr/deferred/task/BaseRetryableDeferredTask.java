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
package com.threewks.thundr.deferred.task;

public abstract class BaseRetryableDeferredTask implements RetryableDeferredTask {
	private transient int maxRetries = -1;
	private int retries = 0;

	@Override
	public boolean shouldRetry() {
		return (maxRetries() == -1) || retries() < maxRetries();
	}

	@Override
	public int retries() {
		return retries;
	}

	@Override
	public int maxRetries() {
		return maxRetries;
	}

	@Override
	public void run() {
		retries++;
		runInternal();
	}

	protected abstract void runInternal();
}
