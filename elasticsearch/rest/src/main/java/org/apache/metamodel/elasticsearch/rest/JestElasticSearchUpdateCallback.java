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
package org.apache.metamodel.elasticsearch.rest;

import io.searchbox.indices.Refresh;
import org.apache.metamodel.AbstractUpdateCallback;
import org.apache.metamodel.UpdateCallback;
import org.apache.metamodel.create.TableCreationBuilder;
import org.apache.metamodel.delete.RowDeletionBuilder;
import org.apache.metamodel.drop.TableDropBuilder;
import org.apache.metamodel.insert.RowInsertionBuilder;
import org.apache.metamodel.schema.Schema;
import org.apache.metamodel.schema.Table;

/**
 * {@link UpdateCallback} implementation for {@link ElasticSearchRestDataContext}.
 */
final class JestElasticSearchUpdateCallback extends AbstractUpdateCallback {
    public JestElasticSearchUpdateCallback(ElasticSearchRestDataContext dataContext) {
        super(dataContext);
    }

    @Override
    public ElasticSearchRestDataContext getDataContext() {
        return (ElasticSearchRestDataContext) super.getDataContext();
    }

    @Override
    public TableCreationBuilder createTable(Schema schema, String name) throws IllegalArgumentException,
            IllegalStateException {
        return new JestElasticSearchCreateTableBuilder(this, schema, name);
    }

    @Override
    public boolean isDropTableSupported() {
        return true;
    }

    @Override
    public TableDropBuilder dropTable(Table table) throws IllegalArgumentException, IllegalStateException,
            UnsupportedOperationException {
        return new JestElasticSearchDropTableBuilder(this, table);
    }

    @Override
    public RowInsertionBuilder insertInto(Table table) throws IllegalArgumentException, IllegalStateException,
            UnsupportedOperationException {
        return new JestElasticSearchInsertBuilder(this, table);
    }

    @Override
    public boolean isDeleteSupported() {
        return true;
    }

    @Override
    public RowDeletionBuilder deleteFrom(Table table) throws IllegalArgumentException, IllegalStateException,
            UnsupportedOperationException {
        return new JestElasticSearchDeleteBuilder(this, table);
    }

    public void onExecuteUpdateFinished() {
        final String indexName = getDataContext().getIndexName();
        Refresh refresh = new Refresh.Builder().addIndex(indexName).build();

        JestClientExecutor.execute(getDataContext().getElasticSearchClient(), refresh, false);
    }
}
