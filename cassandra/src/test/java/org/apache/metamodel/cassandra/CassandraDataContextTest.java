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
package org.apache.metamodel.cassandra;

import java.util.Arrays;
import java.util.List;
import javax.swing.table.TableModel;

import org.apache.metamodel.data.DataSet;
import org.apache.metamodel.data.DataSetTableModel;
import org.apache.metamodel.data.FilteredDataSet;
import org.apache.metamodel.query.Query;
import org.apache.metamodel.schema.ColumnType;
import org.apache.metamodel.schema.Table;

import com.datastax.driver.core.Cluster;
import com.datastax.driver.core.Session;

public class CassandraDataContextTest extends CassandraTestCase {

    private CassandraSimpleClient client = new CassandraSimpleClient();
    private Cluster cluster;
    private CassandraDataContext dc;
    private String testTableName = "songs";
    private String testCounterTableName = "counter";
    private String firstRowId = "756716f7-2e54-4715-9f00-91dcbea6cf51";
    private String secondRowId = "756716f7-2e54-4715-9f00-91dcbea6cf52";
    private String thirdRowId = "756716f7-2e54-4715-9f00-91dcbea6cf53";
    private String firstRowTitle = "My first song";
    private String secondRowTitle = "My second song";
    private String thirdRowTitle = "My third song";
    private String urlName = "my_url";

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        if (isConfigured()) {
            client.connect(getHostname(), getPort());
            cluster = client.getCluster();
            Session session = cluster.connect();
            dc = new CassandraDataContext(cluster, getKeyspaceName());
            createCassandraKeySpaceAndTables(session);
            populateCassandraTableWithSomeData(session);
            populateCassandraCounterTableWithSomeData(session);
        }
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
        if (isConfigured()) {
            client.close();
        }
    }

    public void testSchemaAndSimpleQuery() throws Exception {
        if (!isConfigured()) {
            System.err.println(getInvalidConfigurationMessage());
            return;
        }

        assertEquals("[" + testCounterTableName +", "+testTableName + "]", Arrays.toString(dc.getDefaultSchema().getTableNames()));

        Table table = dc.getDefaultSchema().getTableByName(testTableName);

        assertEquals(ColumnType.UUID, table.getColumnByName("id").getType());
        assertEquals(ColumnType.STRING, table.getColumnByName("title").getType());
        assertEquals(ColumnType.BOOLEAN, table.getColumnByName("hit").getType());
        assertEquals(ColumnType.FLOAT, table.getColumnByName("duration").getType());
        assertEquals(ColumnType.INTEGER, table.getColumnByName("position").getType());
        assertEquals(ColumnType.TIMESTAMP, table.getColumnByName("creationtime").getType());

        DataSet ds = dc.query().from(testTableName).select("id").and("title").execute();
        assertEquals(CassandraDataSet.class, ds.getClass());

        try {
            assertTrue(ds.next());
            assertEquals("Row[values=[" + secondRowId + ", " + secondRowTitle + "]]", ds.getRow().toString());
            assertTrue(ds.next());
            assertEquals("Row[values=[" + thirdRowId + ", " + thirdRowTitle + "]]", ds.getRow().toString());
            assertTrue(ds.next());
            assertEquals("Row[values=[" + firstRowId + ", " + firstRowTitle + "]]", ds.getRow().toString());
            assertFalse(ds.next());
        } finally {
            ds.close();
        }
    }

    public void testWhereColumnEqualsValues() throws Exception {
        if (!isConfigured()) {
            System.err.println(getInvalidConfigurationMessage());
            return;
        }
        DataSet ds = dc.query().from(testTableName).select("id").and("title").where("id").isEquals(firstRowId)
                .execute();
        assertEquals(FilteredDataSet.class, ds.getClass());

        try {
            assertTrue(ds.next());
            assertEquals("Row[values=[" + firstRowId + ", " + firstRowTitle + "]]", ds.getRow().toString());
            assertFalse(ds.next());
        } finally {
            ds.close();
        }
    }

    public void testWhereColumnInValues() throws Exception {
        if (!isConfigured()) {
            System.err.println(getInvalidConfigurationMessage());
            return;
        }
        DataSet ds = dc.query().from(testTableName).select("id").and("title").where("title")
                .in(firstRowTitle, secondRowTitle).orderBy("id").execute();

        try {
            assertTrue(ds.next());
            assertEquals("Row[values=[" + firstRowId + ", " + firstRowTitle + "]]", ds.getRow().toString());
            assertTrue(ds.next());
            assertEquals("Row[values=[" + secondRowId + ", " + secondRowTitle + "]]", ds.getRow().toString());
            assertFalse(ds.next());
        } finally {
            ds.close();
        }
    }

    public void testMaxRows() throws Exception {
        if (!isConfigured()) {
            System.err.println(getInvalidConfigurationMessage());
            return;
        }
        Table table = dc.getDefaultSchema().getTableByName(testTableName);
        Query query = new Query().from(table).select(table.getColumns()).setMaxRows(2);
        DataSet dataSet = dc.executeQuery(query);

        TableModel tableModel = new DataSetTableModel(dataSet);
        assertEquals(2, tableModel.getRowCount());
    }

    public void testCountQuery() throws Exception {
        if (!isConfigured()) {
            System.err.println(getInvalidConfigurationMessage());
            return;
        }
        Table table = dc.getDefaultSchema().getTableByName(testTableName);
        Query q = new Query().selectCount().from(table);

        List<Object[]> data = dc.executeQuery(q).toObjectArrays();
        assertEquals(1, data.size());
        Object[] row = data.get(0);
        assertEquals(1, row.length);
        assertEquals("[3]", Arrays.toString(row));
    }

    public void testQueryForANonExistingTable() throws Exception {
        if (!isConfigured()) {
            System.err.println(getInvalidConfigurationMessage());
            return;
        }
        boolean thrown = false;
        try {
            dc.query().from("nonExistingTable").select("user").and("message").execute();
        } catch (IllegalArgumentException IAex) {
            thrown = true;
        } finally {
            // ds.close();
        }
        assertTrue(thrown);
    }

    public void testQueryForAnExistingTableAndNonExistingField() throws Exception {
        if (!isConfigured()) {
            System.err.println(getInvalidConfigurationMessage());
            return;
        }
        boolean thrown = false;
        try {
            dc.query().from(testTableName).select("nonExistingField").execute();
        } catch (IllegalArgumentException IAex) {
            thrown = true;
        } finally {
            // ds.close();
        }
        assertTrue(thrown);
    }

    public void testNonExistentKeystore() {
        if (!isConfigured()) {
            System.err.println(getInvalidConfigurationMessage());
            return;
        }
        try {
            new CassandraDataContext(cluster, "nonExistentKeyspace");
            fail();
        } catch (IllegalArgumentException e) {
            assertNotNull(e);
        }
    }

    public void testCounterDataType() throws Exception {
        if (!isConfigured()) {
            System.err.println(getInvalidConfigurationMessage());
            return;
        }
        Table table = dc.getDefaultSchema().getTableByName(testCounterTableName);

        assertEquals(ColumnType.BIGINT, table.getColumnByName("counter_value").getType());
        assertEquals(ColumnType.STRING, table.getColumnByName("url_name").getType());

        DataSet ds = dc.query().from(testCounterTableName).select("counter_value").and("url_name").execute();
        assertEquals(CassandraDataSet.class, ds.getClass());

        try {
            assertTrue(ds.next());
            assertEquals("Row[values=[" + 1 + ", " + urlName + "]]", ds.getRow().toString());
            assertFalse(ds.next());
        } finally {
            ds.close();
        }
    }

    private void createCassandraKeySpaceAndTables(Session session) {
        session.execute("CREATE KEYSPACE IF NOT EXISTS " + getKeyspaceName() + " WITH replication "
                + "= {'class':'SimpleStrategy', 'replication_factor':1};");
        session.execute("DROP TABLE IF EXISTS " + getKeyspaceName() + "." + testTableName + ";");
        session.execute("CREATE TABLE IF NOT EXISTS " + getKeyspaceName() + "." + testTableName + " ("
                + "id uuid PRIMARY KEY," + "title text," + "hit boolean," + "duration float," + "position int,"
                + "creationtime timestamp" + ");");
        session.execute("DROP TABLE IF EXISTS " + getKeyspaceName() + "." + testCounterTableName + ";");
        session.execute("CREATE TABLE IF NOT EXISTS " + getKeyspaceName() + "." + testCounterTableName + " ("
                + "counter_value counter, url_name varchar, PRIMARY KEY (url_name)" + ");");
    }

    private void populateCassandraTableWithSomeData(Session session) {

        // create 1 record
        session.execute("INSERT INTO " + getKeyspaceName() + "." + testTableName
                + " (id, title, hit, duration, position, creationtime) " + "VALUES (" + firstRowId + ","
                + "'My first song'," + "false," + "2.15," + "1," + "dateof(now()))" + ";");

        // create 1 record
        session.execute("INSERT INTO " + getKeyspaceName() + "." + testTableName
                + " (id, title, hit, duration, position, creationtime) " + "VALUES (" + secondRowId + ","
                + "'My second song'," + "true," + "2.55," + "2," + "dateof(now()))" + ";");

        // create 1 record
        session.execute("INSERT INTO " + getKeyspaceName() + "." + testTableName
                + " (id, title, hit, duration, position, creationtime) " + "VALUES (" + thirdRowId + ","
                + "'My third song'," + "false," + "3.15," + "3," + "dateof(now()))" + ";");
    }

    private void populateCassandraCounterTableWithSomeData(Session session) {

        // create 1 record
        session.execute("UPDATE " + getKeyspaceName() + "." + testCounterTableName
                + " SET counter_value = counter_value + 1 WHERE url_name='" + urlName + "';");
    }
}