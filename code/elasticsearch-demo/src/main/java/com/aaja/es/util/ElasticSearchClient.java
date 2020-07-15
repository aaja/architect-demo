///*
// * Licensed to the Apache Software Foundation (ASF) under one or more
// * contributor license agreements.  See the NOTICE file distributed with
// * this work for additional information regarding copyright ownership.
// * The ASF licenses this file to You under the Apache License, Version 2.0
// * (the "License"); you may not use this file except in compliance with
// * the License.  You may obtain a copy of the License at
// *
// *     http://www.apache.org/licenses/LICENSE-2.0
// *
// * Unless required by applicable law or agreed to in writing, software
// * distributed under the License is distributed on an "AS IS" BASIS,
// * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// * See the License for the specific language governing permissions and
// * limitations under the License.
// *
// */
//
//package com.aaja.es.util;
//
//
//import java.io.*;
//import java.util.*;
//import org.apache.log4j.Logger;
//
//public class ElasticSearchClient implements Client {
//
//    private static Logger logger = Logger.getLogger(ElasticSearchClient.class);
//
//    private final String TYPE;
//    private final String clusterNodes;
//    private final String namespace;
//    private final String user;
//    private final String password;
//    private RestHighLevelClient client;
//
//    public ElasticSearchClient(String clusterNodes, String namespace, String user, String password,String indexType) {
//        this.clusterNodes = clusterNodes;
//        this.namespace = namespace;
//        this.user = user;
//        this.password = password;
//        this.TYPE = indexType;
//    }
//
//    @Override public void connect() throws IOException {
//        List<HttpHost> pairsList = parseClusterNodes(clusterNodes);
//        RestClientBuilder builder;
//        if (StringUtils.isNotBlank(user) && StringUtils.isNotBlank(password)) {
//            final CredentialsProvider credentialsProvider = new BasicCredentialsProvider();
//            credentialsProvider.setCredentials(AuthScope.ANY, new UsernamePasswordCredentials(user, password));
//            builder = RestClient.builder(pairsList.toArray(new HttpHost[0]))
//                .setHttpClientConfigCallback(httpClientBuilder -> httpClientBuilder.setDefaultCredentialsProvider(credentialsProvider));
//        } else {
//            builder = RestClient.builder(pairsList.toArray(new HttpHost[0]));
//        }
//        client = new RestHighLevelClient(builder);
//        client.ping();
//    }
//
//    @Override public void shutdown() throws IOException {
//        if(client!=null) {
//        	client.close();
//        }
//    }
//
//    private List<HttpHost> parseClusterNodes(String nodes) {
//        List<HttpHost> httpHosts = new LinkedList<>();
//        logger.info("elasticsearch cluster nodes: {}", nodes);
//        String[] nodesSplit = nodes.split(",");
//        for (String node : nodesSplit) {
//            String host = node.split(":")[0];
//            String port = node.split(":")[1];
//            httpHosts.add(new HttpHost(host, Integer.valueOf(port)));
//        }
//
//        return httpHosts;
//    }
//
//    public boolean createIndex(String indexName, JsonObject settings, JsonObject mapping) throws IOException {
//        indexName = formatIndexName(indexName);
//        CreateIndexRequest request = new CreateIndexRequest(indexName);
//        request.settings(settings.toString(), XContentType.JSON);
//        request.mapping(TYPE, mapping.toString(), XContentType.JSON);
//        CreateIndexResponse response = client.indices().create(request);
//        logger.debug("create {} index finished, isAcknowledged: {}", indexName, response.isAcknowledged());
//        return response.isAcknowledged();
//    }
//
//    @SuppressWarnings("deprecation")
//	public JsonObject getIndex(String indexName) throws IOException {
//        indexName = formatIndexName(indexName);
//        GetIndexRequest request = new GetIndexRequest();
//        request.indices(indexName);
//        Response response = client.getLowLevelClient().performRequest(HttpGet.METHOD_NAME, "/" + indexName);
//        InputStreamReader reader = new InputStreamReader(response.getEntity().getContent());
//        Gson gson = new Gson();
//        return gson.fromJson(reader, JsonObject.class);
//    }
//
//    public boolean deleteIndex(String indexName) throws IOException {
//        indexName = formatIndexName(indexName);
//        DeleteIndexRequest request = new DeleteIndexRequest(indexName);
//        DeleteIndexResponse response = client.indices().delete(request);
//        logger.debug("delete {} index finished, isAcknowledged: {}", indexName, response.isAcknowledged());
//        return response.isAcknowledged();
//    }
//
//    public boolean isExistsIndex(String indexName) throws IOException {
//        indexName = formatIndexName(indexName);
//        GetIndexRequest request = new GetIndexRequest();
//        request.indices(indexName);
//        return client.indices().exists(request);
//    }
//
//    @SuppressWarnings("deprecation")
//	public boolean isExistsTemplate(String indexName) throws IOException {
//        indexName = formatIndexName(indexName);
//
//        Response response = client.getLowLevelClient().performRequest(HttpHead.METHOD_NAME, "/_template/" + indexName);
//
//        int statusCode = response.getStatusLine().getStatusCode();
//        if (statusCode == 200) {
//            return true;
//        } else if (statusCode == 404) {
//            return false;
//        } else {
//            throw new IOException("The response status code of template exists request should be 200 or 404, but it is " + statusCode);
//        }
//    }
//
//    @SuppressWarnings("deprecation")
//	public boolean createTemplate(String indexName, JsonObject settings, JsonObject mapping) throws IOException {
//        indexName = formatIndexName(indexName);
//
//        JsonArray patterns = new JsonArray();
//        patterns.add(indexName + "_*");
//
//        JsonObject template = new JsonObject();
//        template.add("index_patterns", patterns);
//        template.add("settings", settings);
//        template.add("mappings", mapping);
//
//        HttpEntity entity = new NStringEntity(template.toString(), ContentType.APPLICATION_JSON);
//
//        Response response = client.getLowLevelClient().performRequest(HttpPut.METHOD_NAME, "/_template/" + indexName, Collections.emptyMap(), entity);
//        return response.getStatusLine().getStatusCode() == 200;
//    }
//
//    @SuppressWarnings("deprecation")
//	public boolean deleteTemplate(String indexName) throws IOException {
//        indexName = formatIndexName(indexName);
//
//        Response response = client.getLowLevelClient().performRequest(HttpDelete.METHOD_NAME, "/_template/" + indexName);
//        return response.getStatusLine().getStatusCode() == 200;
//    }
//
//    public SearchResponse search(String indexName, SearchSourceBuilder searchSourceBuilder) throws IOException {
//        indexName = formatIndexName(indexName);
//        SearchRequest searchRequest = new SearchRequest(indexName);
//        searchRequest.types(TYPE);
//        searchRequest.source(searchSourceBuilder);
//        return client.search(searchRequest);
//    }
//
//    public GetResponse get(String indexName, String id) throws IOException {
//        indexName = formatIndexName(indexName);
//        GetRequest request = new GetRequest(indexName, TYPE, id);
//        return client.get(request);
//    }
//
//    public MultiGetResponse multiGet(String indexName, List<String> ids) throws IOException {
//        final String newIndexName = formatIndexName(indexName);
//        MultiGetRequest request = new MultiGetRequest();
//        ids.forEach(id -> request.add(newIndexName, TYPE, id));
//        return client.multiGet(request);
//    }
//
//    public void forceInsert(String indexName, String id, XContentBuilder source) throws IOException {
//        IndexRequest request = prepareInsert(indexName, id, source);
//        request.setRefreshPolicy(WriteRequest.RefreshPolicy.IMMEDIATE);
//        client.index(request);
//    }
//
//    public void forceUpdate(String indexName, String id, XContentBuilder source, long version) throws IOException {
//        UpdateRequest request = prepareUpdate(indexName, id, source);
//        request.version(version);
//        request.setRefreshPolicy(WriteRequest.RefreshPolicy.IMMEDIATE);
//        client.update(request);
//    }
//
//    public void forceUpdate(String indexName, String id, XContentBuilder source) throws IOException {
//        UpdateRequest request = prepareUpdate(indexName, id, source);
//        request.setRefreshPolicy(WriteRequest.RefreshPolicy.IMMEDIATE);
//        client.update(request);
//    }
//
//    public IndexRequest prepareInsert(String indexName, String id, XContentBuilder source) {
//        indexName = formatIndexName(indexName);
//        return new IndexRequest(indexName, TYPE, id).source(source);
//    }
//
//    public UpdateRequest prepareUpdate(String indexName, String id, XContentBuilder source) {
//        indexName = formatIndexName(indexName);
//        return new UpdateRequest(indexName, TYPE, id).doc(source);
//    }
//
//    @SuppressWarnings("deprecation")
//	public int delete(String indexName, String timeBucketColumnName, long endTimeBucket) throws IOException {
//        indexName = formatIndexName(indexName);
//        Map<String, String> params = Collections.singletonMap("conflicts", "proceed");
//        String jsonString = "{" +
//            "  \"query\": {" +
//            "    \"range\": {" +
//            "      \"" + timeBucketColumnName + "\": {" +
//            "        \"lte\": " + endTimeBucket +
//            "      }" +
//            "    }" +
//            "  }" +
//            "}";
//        HttpEntity entity = new NStringEntity(jsonString, ContentType.APPLICATION_JSON);
//        Response response = client.getLowLevelClient().performRequest(HttpPost.METHOD_NAME, "/" + indexName + "/_delete_by_query", params, entity);
//        logger.debug("delete indexName: {}, jsonString : {}", indexName, jsonString);
//        return response.getStatusLine().getStatusCode();
//    }
//
//    public String formatIndexName(String indexName) {
//        if (StringUtils.isNotEmpty(namespace)) {
//            return namespace + "_" + indexName;
//        }
//        return indexName;
//    }
//
//    public BulkProcessor createBulkProcessor(int bulkActions, int bulkSize, int flushInterval, int concurrentRequests) {
//        BulkProcessor.Listener listener = new BulkProcessor.Listener() {
//            @Override
//            public void beforeBulk(long executionId, BulkRequest request) {
//                int numberOfActions = request.numberOfActions();
//                logger.debug("Executing bulk [{}] with {} requests", executionId, numberOfActions);
//            }
//
//            @Override
//            public void afterBulk(long executionId, BulkRequest request, BulkResponse response) {
//                if (response.hasFailures()) {
//                    logger.warn("Bulk [{}] executed with failures", executionId);
//                } else {
//                    logger.info("Bulk [{}] completed in {} milliseconds", executionId, response.getTook().getMillis());
//                }
//            }
//
//            @Override
//            public void afterBulk(long executionId, BulkRequest request, Throwable failure) {
//                logger.error("Failed to execute bulk", failure);
//            }
//        };
//
//        return BulkProcessor.builder(client::bulkAsync, listener)
//            .setBulkActions(bulkActions)
//            .setBulkSize(new ByteSizeValue(bulkSize, ByteSizeUnit.MB))
//            .setFlushInterval(TimeValue.timeValueSeconds(flushInterval))
//            .setConcurrentRequests(concurrentRequests)
//            .setBackoffPolicy(BackoffPolicy.exponentialBackoff(TimeValue.timeValueMillis(100), 3))
//            .build();
//    }
//    //根据id删除缓存
//    @SuppressWarnings("deprecation")
//	public int deleteById(String indexName, String id ) throws IOException {
//        indexName = formatIndexName(indexName);
//        String jsonString = "{" +
//        		"	\"query\": {" +
//        		"		\"match\": {" +
//        		"			\"_id\": " +"\""+id+"\""+
//        		"		}" +
//        		"	}" +
//        		"}";
//        HttpEntity entity = new NStringEntity(jsonString, ContentType.APPLICATION_JSON);
//        Response response = client.getLowLevelClient().performRequest(HttpPost.METHOD_NAME,
//    			"/" + indexName + "/_delete_by_query", new HashMap<String, String>(), entity);
//        logger.debug("delete indexName: {}, jsonString : {}", indexName, jsonString);
//        return response.getStatusLine().getStatusCode();
//    }
//    //根据某一字段删除缓存
//    @SuppressWarnings("deprecation")
//	public int deleteBykey(String indexName, String key ,String value) throws IOException {
//        indexName = formatIndexName(indexName);
//        String jsonString = "{" +
//        		"	\"query\": {" +
//        		"		\"match\": {" +
//        		"			\""+key+"\": " +"\""+value+"\""+
//        		"		}" +
//        		"	}" +
//        		"}";
//        HttpEntity entity = new NStringEntity(jsonString, ContentType.APPLICATION_JSON);
//        Response response = client.getLowLevelClient().performRequest(HttpPost.METHOD_NAME,
//    			"/" + indexName + "/_delete_by_query", new HashMap<String, String>(), entity);
//        logger.debug("delete indexName: {}, jsonString : {}", indexName, jsonString);
//        return response.getStatusLine().getStatusCode();
//    }
//    //精确匹配某字段的值
//    @SuppressWarnings("deprecation")
//	public  List<JsonObject> querySearch (String indexName,String title,String value)throws IOException{
//    	indexName=formatIndexName(indexName);
//    	String jsonString="{" +
//    			"  \"query\": {" +
//    			"    \"match_phrase\": {" +
//    			"   	"+"\""+title+"\""+" : " +"\""+value+"\""+
//    			"    }" +
//    			"  }" +
//    			"}";
//    	HttpEntity entity = new NStringEntity(jsonString, ContentType.APPLICATION_JSON);
//        Response response = client.getLowLevelClient().performRequest(HttpPost.METHOD_NAME,
//    			"/" + indexName + "/_search", new HashMap<String, String>(), entity);
//        InputStreamReader reader = new InputStreamReader(response.getEntity().getContent());
//        Gson gson = new Gson();
//        JsonObject allObj = gson.fromJson(reader, JsonObject.class);
//        logger.info(allObj.toString());
//        JsonObject hits = allObj.getAsJsonObject("hits");
//        final int total = hits.get("total").getAsInt();
//        JsonArray ja = null;
//        List<JsonObject> resultList=new ArrayList<JsonObject>();
//        if (total > 0) {
//        	ja = hits.getAsJsonArray("hits");
//        	for(int i=0;i<total;i++) {
//        		JsonObject middle=ja.get(i).getAsJsonObject().get(EsConfigUtils.KEY_ES_SOURCE).getAsJsonObject();
//        		middle.add("uuid", ja.get(i).getAsJsonObject().get("_id"));
//        		resultList.add(middle);
//        	}
//        }
//        return resultList;
//    }
//    //精确匹配多字段的值
//    @SuppressWarnings("deprecation")
//	public  List<JsonObject> querySearch (String indexName, Map<String, String> kv)throws IOException{
//    	if (kv == null || kv.isEmpty()) {
//    		return null;
//    	}
//    	indexName=formatIndexName(indexName);
//    	StringBuffer prefix = new StringBuffer();
//    	prefix.append("{")
//    	.append("  \"query\": {")
//    	.append("\"bool\":{\"must\":[");
//    	boolean isFirst = true;
//    	for (Iterator<String> it = kv.keySet().iterator(); it.hasNext();) {
//    		final String key = it.next();
//    		final String value = kv.get(key);
//    		if (!isFirst) {
//    			prefix.append(",");
//    		} else {
//    			isFirst = false;
//    		}
//    		prefix.append("{    \"match_phrase\": {")
//    		.append("\"").append(key).append("\" : \"").append(value).append("\"")
//    		.append("    }}");
//    	}
//    	prefix.append("  ]}").append("}}");
//    	HttpEntity entity = new NStringEntity(prefix.toString(), ContentType.APPLICATION_JSON);
//    	Response response = client.getLowLevelClient().performRequest(HttpPost.METHOD_NAME,
//    			"/" + indexName + "/_search", new HashMap<String, String>(), entity);
//    	InputStreamReader reader = new InputStreamReader(response.getEntity().getContent());
//    	Gson gson = new Gson();
//    	JsonObject allObj = gson.fromJson(reader, JsonObject.class);
//    	logger.info(allObj.toString());
//    	JsonObject hits = allObj.getAsJsonObject("hits");
//    	final int total = hits.get("total").getAsInt();
//    	JsonArray ja = null;
//    	List<JsonObject> resultList=new ArrayList<JsonObject>();
//    	if (total > 0) {
//    		ja = hits.getAsJsonArray("hits");
//    		for(int i=0;i<total;i++) {
//    			JsonObject middle=ja.get(i).getAsJsonObject().get(EsConfigUtils.KEY_ES_SOURCE).getAsJsonObject();
//    			middle.add("uuid", ja.get(i).getAsJsonObject().get("_id"));
//    			resultList.add(middle);
//    		}
//    	} else {
//    		logger.error("未获取到数据");
//    	}
//    	return resultList;
//    }
//
//    @SuppressWarnings("deprecation")
//	public  List<JsonObject> querySearchMot (String indexName,String title,String value)throws IOException{
//    	indexName=formatIndexName(indexName);
//    	String jsonString="{" +
//    			"  \"query\": {" +
//    			"    \"match_phrase\": {" +
//    			"      \""+title+"\": {" +
//    			"        \"query\": \""+value+"\"" +
//    			"      }" +
//    			"    }" +
//    			"  }" +
//    			"}";
//    	HttpEntity entity = new NStringEntity(jsonString, ContentType.APPLICATION_JSON);
//        Response response = client.getLowLevelClient().performRequest(HttpPost.METHOD_NAME, "/" + indexName + "/_search?size=10000", new HashMap<String, String>(), entity);
//        InputStreamReader reader = new InputStreamReader(response.getEntity().getContent());
//        Gson gson = new Gson();
//        JsonObject allObj = gson.fromJson(reader, JsonObject.class);
//        logger.info(allObj.toString());
//        JsonObject hits = allObj.getAsJsonObject("hits");
//        final int total = hits.get("total").getAsInt();
//        JsonArray ja = null;
//        List<JsonObject> resultList=new ArrayList<JsonObject>();
//        if (total > 0) {
//        	ja = hits.getAsJsonArray("hits");
//        	for(int i=0;i<total;i++) {
//        		JsonObject middle=ja.get(i).getAsJsonObject().get(EsConfigUtils.KEY_ES_SOURCE).getAsJsonObject();
//        		middle.add("uuid", ja.get(i).getAsJsonObject().get("_id"));
//        		resultList.add(middle);
//        	}
//        }
//        return resultList;
//    }
//
//}
