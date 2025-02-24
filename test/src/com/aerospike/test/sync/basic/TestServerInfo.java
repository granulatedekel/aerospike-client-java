/*
 * Copyright 2012-2024 Aerospike, Inc.
 *
 * Portions may be licensed to Aerospike, Inc. under one or more contributor
 * license agreements WHICH ARE COMPATIBLE WITH THE APACHE LICENSE, VERSION 2.0.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package com.aerospike.test.sync.basic;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertEquals;

import java.util.Map;

import com.aerospike.client.ResultCode;
import org.junit.Test;

import com.aerospike.client.Info;
import com.aerospike.client.cluster.Node;
import com.aerospike.test.sync.TestSync;

public class TestServerInfo extends TestSync {
	@Test
	public void serverInfo() {
		Node node = client.getNodes()[0];
		GetServerConfig(node);
		GetNamespaceConfig(node);
	}

	/**
	 * Query server configuration and cluster status.
	 */
	private void GetServerConfig(Node node) {
		Map<String,String> map = Info.request(null, node);
		assertNotNull(map);

		for (Map.Entry<String,String> entry : map.entrySet()) {
			String key = entry.getKey();

			if (key.equals("statistics") || key.equals("query-stat")) {
				LogNameValueTokens(entry.getValue());
			}
			else {
				if (! (key.equals("services-alumni") || key.equals("services") || key.equals("dcs") || key.equals("build_ee_sha"))) {
					assertNotNull(entry.getValue());
				}
			}
		}
	}

	/**
	 * Query namespace configuration.
	 */
	private void GetNamespaceConfig(Node node) {
		String filter = "namespace/" + args.namespace;
		String tokens = Info.request(null, node, filter);
		assertNotNull(tokens);
		LogNameValueTokens(tokens);
	}

	private void LogNameValueTokens(String tokens) {
		String[] values = tokens.split(";");

		for (String value : values) {
			assertNotNull(value);
		}
	}

	@Test
	public void errorResponse() {
		Info.Error error;

		error = new Info.Error("FaIL:201:index not found");
		assertEquals(error.code, 201);
		assertEquals(error.message, "index not found");

		error = new Info.Error("ERRor:201:index not found");
		assertEquals(error.code, 201);
		assertEquals(error.message, "index not found");

		error = new Info.Error("error::index not found ");
		assertEquals(error.code, ResultCode.CLIENT_ERROR);
		assertEquals(error.message, "index not found");

		error = new Info.Error("error: index not found ");
		assertEquals(error.code, ResultCode.CLIENT_ERROR);
		assertEquals(error.message, "index not found");

		error = new Info.Error("error:99");
		assertEquals(error.code, 99);
		assertEquals(error.message, "error:99");

		error = new Info.Error("generic message");
		assertEquals(error.code, ResultCode.CLIENT_ERROR);
		assertEquals(error.message, "generic message");
	}
}
