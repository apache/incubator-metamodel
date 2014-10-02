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


import org.apache.metamodel.data.AbstractDataSet;
import org.apache.metamodel.data.DataSetHeader;
import org.apache.metamodel.data.Row;
import org.apache.metamodel.schema.Column;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Iterator;

final class CassandraDataSet extends AbstractDataSet {

    private static final Logger logger = LoggerFactory.getLogger(CassandraDataSet.class);

    private final Iterator<com.datastax.driver.core.Row> _cursor;
    private final boolean _queryPostProcessed;

    private boolean _closed;
    private volatile com.datastax.driver.core.Row _dbObject;

    public CassandraDataSet(Iterator<com.datastax.driver.core.Row> cursor, Column[] columns, boolean queryPostProcessed) {
        super(columns);
        _cursor = cursor;
        _queryPostProcessed = queryPostProcessed;
        _closed = false;
    }

    public boolean isQueryPostProcessed() {
        return _queryPostProcessed;
    }

    @Override
    public void close() {
        super.close();
        _closed = true;
    }

    @Override
    protected void finalize() throws Throwable {
        super.finalize();
        if (!_closed) {
            logger.warn("finalize() invoked, but DataSet is not closed. Invoking close() on {}", this);
            close();
        }
    }

    @Override
    public boolean next() {
        if (_cursor.hasNext()) {
            _dbObject = _cursor.next();
            return true;
        } else {
            _dbObject = null;
            return false;
        }
    }

    @Override
    public Row getRow() {
        return CassandraDBUtils.toRow(_dbObject, getHeader());
    }

}
