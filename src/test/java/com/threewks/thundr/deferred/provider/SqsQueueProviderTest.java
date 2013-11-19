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
package com.threewks.thundr.deferred.provider;

import com.amazonaws.regions.Region;
import com.amazonaws.regions.RegionUtils;
import com.amazonaws.services.sqs.AmazonSQSClient;
import com.amazonaws.services.sqs.model.Message;
import com.amazonaws.services.sqs.model.ReceiveMessageRequest;
import com.amazonaws.services.sqs.model.SendMessageRequest;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.util.Arrays;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;

public class SqsQueueProviderTest {

	private SqsQueueProvider provider;
	private AmazonSQSClient sqsClient;

	@Before
	public void before() {
		sqsClient = mock(AmazonSQSClient.class, RETURNS_DEEP_STUBS);
		provider = new SqsQueueProvider(sqsClient);
	}

	@Test
	public void shouldSendMessage() {
		String message = "A message";
		provider.send(message);

		ArgumentCaptor<SendMessageRequest> argument = ArgumentCaptor.forClass(SendMessageRequest.class);
		verify(sqsClient, times(1)).sendMessage(argument.capture());

		SendMessageRequest request = argument.getValue();
		assertThat(request.getMessageBody(), equalTo(message));
	}

	@Test
	public void shouldReceiveMessages() {
		String body = "A message";
		when(sqsClient.receiveMessage(any(ReceiveMessageRequest.class)).getMessages())
				.thenReturn(Arrays.asList(new Message().withBody(body)));

		List<String> messages = provider.receive();
		assertThat(messages.size(), is(1));
		assertThat(messages, hasItem(body));
	}
}
