/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.metamodel.neo4j;

import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpHost;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.metamodel.MetaModelException;
import org.apache.metamodel.MetaModelHelper;
import org.apache.metamodel.QueryPostprocessDataContext;
import org.apache.metamodel.UpdateScript;
import org.apache.metamodel.UpdateableDataContext;
import org.apache.metamodel.data.DataSet;
import org.apache.metamodel.data.DocumentSource;
import org.apache.metamodel.query.FilterItem;
import org.apache.metamodel.query.SelectItem;
import org.apache.metamodel.schema.Column;
import org.apache.metamodel.schema.MutableSchema;
import org.apache.metamodel.schema.MutableTable;
import org.apache.metamodel.schema.Schema;
import org.apache.metamodel.schema.Table;
import org.apache.metamodel.schema.builder.DocumentSourceProvider;
import org.apache.metamodel.util.SimpleTableDef;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * DataContext implementation for Neo4j
 */
public class Neo4jDataContext extends QueryPostprocessDataContext implements UpdateableDataContext,
        DocumentSourceProvider {

    public static final Logger logger = LoggerFactory.getLogger(Neo4jDataContext.class);

    public static final String SCHEMA_NAME = "neo4j";

    public static final int DEFAULT_PORT = 7474;

    private final SimpleTableDef[] _tableDefs;

    private final Neo4jRequestWrapper _requestWrapper;

    private final HttpHost _httpHost;

    public Neo4jDataContext(String hostname, int port, SimpleTableDef... tableDefs) {
        _httpHost = new HttpHost(hostname, port);
        CloseableHttpClient httpClient = HttpClientBuilder.create().build();
        _requestWrapper = new Neo4jRequestWrapper(httpClient, _httpHost);
        _tableDefs = tableDefs;
    }

    public Neo4jDataContext(String hostname, int port) {
        _httpHost = new HttpHost(hostname, port);
        CloseableHttpClient httpClient = HttpClientBuilder.create().build();
        _requestWrapper = new Neo4jRequestWrapper(httpClient, _httpHost);
        _tableDefs = detectTableDefs();
    }

    public Neo4jDataContext(String hostname, int port, CloseableHttpClient httpClient) {
        _httpHost = new HttpHost(hostname, port);
        _requestWrapper = new Neo4jRequestWrapper(httpClient, _httpHost);
        _tableDefs = detectTableDefs();
    }

    public Neo4jDataContext(String hostname, int port, CloseableHttpClient httpClient, SimpleTableDef... tableDefs) {
        _httpHost = new HttpHost(hostname, port);
        _requestWrapper = new Neo4jRequestWrapper(httpClient, _httpHost);
        _tableDefs = tableDefs;
    }

    @Override
    protected String getDefaultSchemaName() throws MetaModelException {
        return SCHEMA_NAME;
    }

    @Override
    protected Schema getMainSchema() throws MetaModelException {
        MutableSchema schema = new MutableSchema(getMainSchemaName());
        for (SimpleTableDef tableDef : _tableDefs) {
            MutableTable table = tableDef.toTable().setSchema(schema);
            schema.addTable(table);
        }
        return schema;
    }

    @Override
    protected String getMainSchemaName() throws MetaModelException {
        return SCHEMA_NAME;
    }

    public SimpleTableDef[] detectTableDefs() {
        List<SimpleTableDef> tableDefs = new ArrayList<SimpleTableDef>();

        String allLabelsJsonString = _requestWrapper.executeRestRequest(new HttpGet("/db/data/labels"));

        JSONArray allLabelsJsonArray;
        try {
            allLabelsJsonArray = new JSONArray(allLabelsJsonString);
            for (int i = 0; i < allLabelsJsonArray.length(); i++) {
                String label = allLabelsJsonArray.getString(i);

                List<JSONObject> allNodesPerLabel = getAllNodesPerLabel(label);

                List<String> allPropertiesPerLabel = new ArrayList<String>();
                for (JSONObject node : allNodesPerLabel) {
                    List<String> allPropertiesPerNode = getAllPropertiesPerNode(node);
                    for (String property : allPropertiesPerNode) {
                        if (!allPropertiesPerLabel.contains(property)) {
                            allPropertiesPerLabel.add(property);
                        }
                    }
                }

                if (!allNodesPerLabel.isEmpty()) {
                    SimpleTableDef tableDef = new SimpleTableDef(label,
                            allPropertiesPerLabel.toArray(new String[allPropertiesPerLabel.size()]));
                    tableDefs.add(tableDef);
                }
            }
            return tableDefs.toArray(new SimpleTableDef[tableDefs.size()]);
        } catch (JSONException e) {
            logger.error("Error occured in parsing JSON while detecting the schema: ", e);
            throw new IllegalStateException(e);
        }
    }

    private List<JSONObject> getAllNodesPerLabel(String label) {
        List<JSONObject> allNodesPerLabel = new ArrayList<JSONObject>();

        String allNodesForLabelJsonString = _requestWrapper.executeRestRequest(new HttpGet("/db/data/label/" + label
                + "/nodes"));

        JSONArray allNodesForLabelJsonArray;
        try {
            allNodesForLabelJsonArray = new JSONArray(allNodesForLabelJsonString);
            for (int i = 0; i < allNodesForLabelJsonArray.length(); i++) {
                JSONObject node = allNodesForLabelJsonArray.getJSONObject(i);
                allNodesPerLabel.add(node);
            }
            return allNodesPerLabel;
        } catch (JSONException e) {
            logger.error("Error occured in parsing JSON while detecting the nodes for a label: " + label, e);
            throw new IllegalStateException(e);
        }
    }

    private List<String> getAllPropertiesPerNode(JSONObject node) {
        List<String> properties = new ArrayList<String>();

        String propertiesEndpoint;
        try {
            propertiesEndpoint = node.getString("properties");

            String allPropertiesPerNodeJsonString = _requestWrapper.executeRestRequest(new HttpGet(propertiesEndpoint));

            JSONObject allPropertiesPerNodeJsonObject = new JSONObject(allPropertiesPerNodeJsonString);
            for (int j = 0; j < allPropertiesPerNodeJsonObject.length(); j++) {
                JSONArray propertiesJsonArray = allPropertiesPerNodeJsonObject.names();
                for (int k = 0; k < propertiesJsonArray.length(); k++) {
                    String property = propertiesJsonArray.getString(k);
                    properties.add(property);
                }
            }
            return properties;
        } catch (JSONException e) {
            logger.error("Error occured in parsing JSON while detecting the properties of a node: " + node, e);
            throw new IllegalStateException(e);
        }
    }

    @Override
    protected DataSet materializeMainSchemaTable(Table table, Column[] columns, int firstRow, int maxRows) {
        if ((columns != null) && (columns.length > 0)) {
            try {
                String selectQuery = Neo4jCypherQueryBuilder.buildSelectQuery(table, columns, firstRow, maxRows);
                String responseJSONString = _requestWrapper.executeCypherQuery(selectQuery);
                JSONObject resultJSONObject = new JSONObject(responseJSONString);
                final SelectItem[] selectItems = MetaModelHelper.createSelectItems(columns);
                Neo4jDataSet neo4jDataSet = new Neo4jDataSet(selectItems, resultJSONObject);
                return neo4jDataSet;
            } catch (JSONException e) {
                logger.error("Error occured in parsing JSON while materializing the schema: ", e);
                throw new IllegalStateException(e);
            }
        } else {
            logger.error("Encountered null or empty columns array for materializing main schema table.");
            throw new IllegalArgumentException("Columns cannot be null or empty array");
        }
    }

    @Override
    protected DataSet materializeMainSchemaTable(Table table, Column[] columns, int maxRows) {
        return materializeMainSchemaTable(table, columns, 1, maxRows);
    }

    @Override
    protected org.apache.metamodel.data.Row executePrimaryKeyLookupQuery(Table table, List<SelectItem> selectItems,
            Column primaryKeyColumn, Object keyValue) {
        return null;
    }

    @Override
    protected Number executeCountQuery(Table table, List<FilterItem> whereItems, boolean functionApproximationAllowed) {
        return -1;
    }

    @Override
    public void executeUpdate(UpdateScript script) {
        throw new UnsupportedOperationException("MetaModel does not currently support write operations on Neo4j databases.");
    }

    @Override
    public DocumentSource getMixedDocumentSourceForSampling() {
        return null;
    }

    @Override
    public DocumentSource getDocumentSourceForTable(String sourceCollectionName) {
        return null;
    }
}