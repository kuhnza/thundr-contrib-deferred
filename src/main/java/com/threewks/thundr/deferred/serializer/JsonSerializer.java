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

import com.google.gson.GsonBuilder;
import com.threewks.thundr.deferred.serializer.gson.DateTimeTypeConverter;
import org.joda.time.DateTime;

public class JsonSerializer implements Serializer {
	private final GsonBuilder builder;

	public JsonSerializer() {
		builder = new GsonBuilder();
		builder.registerTypeAdapter(DateTime.class, new DateTimeTypeConverter());
	}

	@Override
	public <T> String serialize(T object) {
		return builder.create().toJson(object);
	}

	@Override
	public <T> T deserialize(Class<T> type, String data) throws ClassNotFoundException {
		return builder.create().fromJson(data, type);
	}
}
