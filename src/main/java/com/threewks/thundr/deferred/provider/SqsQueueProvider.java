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

import java.util.ArrayList;
import java.util.List;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.regions.Region;
import com.amazonaws.regions.RegionUtils;
import com.amazonaws.services.sqs.AmazonSQSClient;
import com.amazonaws.services.sqs.model.*;
import com.threewks.thundr.configuration.Environment;

public class SqsQueueProvider implements QueueProvider {
	public static final String DefaultDeferredSqsQueueName = "thundr-deferred";

	private AmazonSQSClient sqs;
	private String queueUrl;

	public SqsQueueProvider(AmazonSQSClient client) {
		init(client, getDefaultQueueName());
	}

	public SqsQueueProvider(AmazonSQSClient client, String deferredSqsQueueName) {
		init(client, deferredSqsQueueName);
	}

	public SqsQueueProvider(AWSCredentials credentials, Region region) {
		this(credentials, region, getDefaultQueueName());
	}

	public SqsQueueProvider(String deferredSqsAccessKey, String deferredSqsSecretKey, String deferredSqsRegion) {
		this(deferredSqsAccessKey, deferredSqsSecretKey, deferredSqsRegion, getDefaultQueueName());
	}

	public SqsQueueProvider(String deferredSqsAccessKey, String deferredSqsSecretKey, String deferredSqsRegion, String deferredSqsQueueName) {
		this(new BasicAWSCredentials(deferredSqsAccessKey, deferredSqsSecretKey),
				RegionUtils.getRegion(deferredSqsRegion), deferredSqsQueueName);
	}

	public SqsQueueProvider(AWSCredentials credentials, Region region, String deferredSqsQueueName) {
		AmazonSQSClient client = new AmazonSQSClient(credentials);
		client.setRegion(region);
		init(client, deferredSqsQueueName);
	}

	@Override
	public void send(String message) {
		SendMessageResult result = sqs.sendMessage(new SendMessageRequest(queueUrl, message));
	}

	@Override
	public List<String> receive() {
		ReceiveMessageRequest receiveMessageRequest = new ReceiveMessageRequest(queueUrl);
		List<Message> messages = sqs.receiveMessage(receiveMessageRequest).getMessages();

		List<String> received = new ArrayList<String>();
		for (Message message : messages) {
			received.add(message.getBody());

			// Delete message once read off queue
			sqs.deleteMessage(new DeleteMessageRequest(queueUrl, message.getReceiptHandle()));
		}

		return received;
	}

	private void init(AmazonSQSClient client, String deferredSqsQueueName) {
		sqs = client;
		GetQueueUrlRequest queueUrlRequest = new GetQueueUrlRequest(deferredSqsQueueName);
		queueUrl = sqs.getQueueUrl(queueUrlRequest).getQueueUrl();
	}

	private static String getDefaultQueueName() {
		String queueName = DefaultDeferredSqsQueueName;
		if (Environment.get() != null) {
			queueName += "-" + Environment.get();
		}
		return queueName;
	}
}
