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
package com.threewks.thundr.deferred.serializer;

import com.threewks.thundr.deferred.provider.QueueMessage;
import com.threewks.thundr.deferred.task.DeferredTask;

public class TaskSerializer {
	private Serializer serializer;

	public TaskSerializer(Serializer serializer) {
		this.serializer = serializer;
	}

	public String serialize(DeferredTask deferredTask) {
		QueueMessage message = new QueueMessage(deferredTask);
		return serializer.serialize(message);
	}

	public DeferredTask deserialize(String data) throws ClassNotFoundException {
		QueueMessage taskMessage = serializer.deserialize(QueueMessage.class, data);
		Class<?> type = Class.forName(taskMessage.getTaskClassName());
		return (DeferredTask) serializer.deserialize(type, taskMessage.getTaskData());
	}
}
