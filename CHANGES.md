### Apache MetaModel (work-in-progress)

 * [METAMODEL-212] - New module for ElasticSearch via REST client.
 * [METAMODEL-99] - New module for Neo4j connectivity with MetaModel.
 * [METAMODEL-207] - Ensured the serializability of the SingleLineCsvRow class.
 * [METAMODEL-211] - Fixed a bug related to lookup by primary key (_id) on MongoDB.
 * [METAMODEL-216] - Added new aggregate functions: FIRST, LAST and RANDOM.
 * [METAMODEL-195] - Added new function MAP_VALUE which allows extracting a nested value from within a key/value map field.
 * [METAMODEL-15] - Query parser support for table names with space. Delimitters can be double quote or square brackets. 
 * [METAMODEL-215] - Improved the capability of NumberComparator to support Integer, Long, Double, BigInteger and other built-in Number classes.
 * [METAMODEL-218] - Fixed conversion of STRING and NUMBER types to database-specific types in JDBC module.
 * [METAMODEL-205] - Added validation of Excel sheet name before attempting to create table (sheet).
 * [METAMODEL-219] - Made HdfsResource capable of incorporating Hadoop configuration files core-site.xml and hdfs-site.xml

### Apache MetaModel 4.4.1

 * [METAMODEL-198] - Fixed support for JDBC TIMESTAMP precision to match the underlying database's precision.
 * [METAMODEL-200] - Added optional "APPROXIMATE" keyword to query syntax for aggregate functions.
 * [METAMODEL-144] - Automated binary packaging of the MetaModel project.
 * [METAMODEL-197] - ElasticSearch schema update/change after CREATE TABLE statements.
 * [METAMODEL-199] - Fixed a bug in query parser when parsing two consecutive WHERE items with parentheses around them.
 * [METAMODEL-203] - Upgraded MongoDB dependency version and API to the 3.x line.

### Apache MetaModel 4.4.0

 * [METAMODEL-192] - Added support for Scalar functions. We have a basic set of datatype conversion functions as well as support for UDF via implementing the ScalarFunction interface.
 * [METAMODEL-194] - Added support for setting the "Max rows" flag of a query to 0. This will always return an empty dataset.
 * [METAMODEL-173] - Improved CSV writing to non-file destinations. Added .write() and .append() methods to Resource interface.
 * [METAMODEL-170] - Dropped support for Java 6.
 * [METAMODEL-176] - Trimmed the transient dependencies of the JDBC module.
 * [METAMODEL-178] - Added AggregateFunction and ScalarFunction interfaces. Changed FunctionType enum to be super-interface of those. Compatibility is retained but a recompile of code using FunctionType is needed.
 * [METAMODEL-188] - Changed OperatorType enum to be an interface. Compatibility is retained but a recompile of code is needed.
 * [METAMODEL-179] - Ensured that HdfsResource is not closing a shared HDFS file system reference.
 * [METAMODEL-171] - Made integration tests for Cassandra module function properly in all environments.
 * [METAMODEL-177] - Fixed a bug pertaining to the serializability of HdfsResource.
 * [METAMODEL-172] - ElasticSearch Date types should be converted properly.
 * [METAMODEL-184] - ElasticSearch querying with "IS NULL" and "IS NOT NULL" now uses MissingFilter and ExistsFilter.
 * [METAMODEL-190] - Improved decimal number support in Excel module.
 * [METAMODEL-187] - Improved memory consumption of Excel module by passing random-access-file handles to POI when possible.
 * [METAMODEL-191] - Resolved a number of dependency conflicts/overlaps when combining multiple MetaModel modules.
 * [METAMODEL-157] - Fixed an issue in DELETE FROM statements with WHERE clauses requiring client-side data type conversion on JDBC databases.
 * [METAMODEL-182] - Improved HdfsResource and FileResource directory-based implementations by adding also getSize() and getLastModified() directory-based implementations.

### Apache MetaModel 4.3.6

 * [METAMODEL-161] - Upgraded HBase client API to version 1.1.1
 * [METAMODEL-160] - Added support for Apache Hive via the JDBC module of MetaModel.
 * [METAMODEL-162] - Made HdfsResource Serializable and added property getters
 * [METAMODEL-163] - Made FileResource and HdfsResource work with folders containing file chunks instead of only single files
 * [METAMODEL-104] - Added 'hadoop' and 'hbase' modules to dependencies of 'MetaModel-full'.
 * [METAMODEL-164] - Fixed a bug in data type parsing of ElasticSearch mapping document.

### Apache MetaModel 4.3.5

 * [METAMODEL-148] - Added a 'hadoop' module with a HdfsResource class to allow CSV, Excel and Fixed-width file access on HDFS.
 * [METAMODEL-152] - Fixed an issue of not clearing schema cache when refreshSchemas() is invoked.
 * [METAMODEL-149] - Added support for COUNTER data type in Cassandra.
 * [METAMODEL-151] - Added support for DOUBLE data type mapping in PostgreSQL
 * [METAMODEL-154] - Use embedded Cassandra server for integration tests.

### Apache MetaModel 4.3.4

 * [METAMODEL-136] - Added LIKE operator native support (using conversion to regex) for MongoDB.
 * [METAMODEL-138] - Allow empty characters before AS keyword in query parsing.
 * [METAMODEL-141] - Improved mapping of ColumnType to SQL data types for Oracle, SQL Server, MySQL, DB2 and PostgreSQL
 * [METAMODEL-142] - Ensured that JDBC schema refreshes in an UpdateScript is using same Connection/Transaction as rest of operations
 * [METAMODEL-133] - Improved query parser support for multiple JOINs in same query.
 * [METAMODEL-140] - Fixed support for ElasticSearch mappings with additional property attributes.

### Apache MetaModel 4.3.3

 * [METAMODEL-123] - Added compatibility with ElasticSearch version 1.4.x
 * [METAMODEL-93] - Added compatibility with Apache HBase version 1.0.0
 * [METAMODEL-124] - Invoked ElasticSearch cross-version incompatible methods via reflection
 * [METAMODEL-125] - Added support for comma-separated select items in Query.select(String) method argument.
 * [METAMODEL-128] - Fixed bug in DataSet ordering when aggregation functions are applied to non-JDBC modules.
 * [METAMODEL-131] - Added support for composite primary keys in JDBC CREATE TABLE statements.

### Apache MetaModel 4.3.2

 * [METAMODEL-78] - Fixed a bug that caused SELECT DISTINCT to sometimes return duplicates records on certain DataContext implementations.
 * [METAMODEL-106] - Improved handling of invalid or non-existing index names passed to ElasticSearchDataContext
 * [METAMODEL-79] - Added update execution support on ElasticSearch module. Increased capability of pushing down WHERE items to ElasticSearch searches.
 * [METAMODEL-115] - Improved query parsing to allow lower-case function names, operators etc.

### Apache MetaModel 4.3.1

 * [METAMODEL-100] - Fixed bug when having multiple columns of same name. Added column no. comparison when calling Column.equals(...).

### Apache MetaModel 4.3.0-incubating

 * [METAMODEL-77] - New module 'elasticsearch' for connecting and modeling ElasticSearch indexes through MetaModel.
 * [METAMODEL-18] - New module 'cassandra' for connecting and modelling Apache Cassandra databases through MetaModel.
 * [METAMODEL-83] - Added new operator types: GREATER_THAN_OR_EQUAL ('>=') and LESS_THAN_OR_EQUAL ('<=').
 * [METAMODEL-92] - For JSON, MongoDB and CouchDB: Made it possible to specify column names referring nested fields such as "name.first" or "addresses[0].city".
 * [METAMODEL-76] - Query parser improved to handle filters without spaces inbetween operator and operands.
 * [METAMODEL-95] - Fixed a critical bug in the Salesforce.com module which caused all number values to be interpreted as '1'.
 * [METAMODEL-74] - Fixed a bug related to skipping blank values when applying an aggregate function (SUM, AVG etc.)
 * [METAMODEL-85] - Fixed a bug that caused NULL values to be evaluated with equal-sign in JDBC update and delete statements instead of 'IS NULL'.
 
### Apache MetaModel 4.2.0-incubating

 * [METAMODEL-38] - New module 'json' for handling json files (containing JSON arrays of documents or line-delimited JSON documents)
 * [METAMODEL-54] - ColumnType converted from enum to interface to allow for further specialization in modules.
 * [METAMODEL-57] - Changed the column type VARCHAR into STRING for the modules: CSV, Fixedwidth, Excel and XML.
 * [METAMODEL-56] - Made separate column types for converted JDBC LOBs - "CLOB as String" and "BLOB as bytes".
 * [METAMODEL-46] - Improved row-lookup by primary key (ID) in CouchDB
 * [METAMODEL-58] - Fixed a bug related to using CreateTable class and primary keys not getting created.
 * [METAMODEL-3]  - Improved writing of Byte-Order-Mark (BOM) for various encoding spelling variants.
 * [METAMODEL-70] - Made the build compatible with both JDK versions 6 and 7.
 * [METAMODEL-59] - Fixed a bug related to handling of date/time literals in MS SQL Server queries.
 * [METAMODEL-60] - Fixed a bug related to DISTINCT and TOP keywords in MS SQL Server queries.
 * [METAMODEL-45] - Improved and standardized way of handling integration test connection information towards external databases.
 * [METAMODEL-62] - Fixed a bug related to fault-tolerant handling of malformed CSV lines when reading CSVs in single-line mode
 * [METAMODEL-68] - Made it possible to create a CSV table without a header line in the file, if the user configures it.
 * [METAMODEL-67] - Upgraded Jackson (JSON library) dependency from org.codehaus namespace to the newer com.fasterxml namespace.
 * [METAMODEL-69] - Fixed issue with deserialization of ColumnType into the new interface instead of the old enum.

### Apache MetaModel 4.1.0-incubating

 * [METAMODEL-13] - Added support for Apache HBase via the new module "MetaModel-hbase"
 * [METAMODEL-41] - Added a parser for SimpleTableDef objects (SimpleTableDefParser). It parses statements similar to CREATE TABLE statements, although without the "CREATE TABLE" prefix. For example: foo (bar INTEGER, baz VARCHAR)
 * [METAMODEL-11] - New module "MetaModel-spring" which adds a convenient FactoryBean to produce various types of DataContext objects based on externalizable parameters, for Spring framework users.
 * [METAMODEL-32] - Fixed thread-safety issue in Excel module when tables (sheets) metadata is updated.
 * [METAMODEL-47] - Fixed issue in Excel of loading schema if query is fired based on metadata from a previous DataContext instance.
 * [METAMODEL-35] - Improved query rewriting for DB2 when paged queries contain ORDER BY clause.
 * [METAMODEL-44] - Added an optional method for QueryPostprocessDataContext implementations to do a row-lookup by primary key value.
 * [METAMODEL-43] - Made CSV datastores skip empty lines in file instead of treating them of rows with null values.
 * [METAMODEL-39] - Added pooling of active/used Connections and PreparedStatements in JDBC compiled queries.
 * [METAMODEL-34] - Updated LICENSE file to not include bundled dependencies' licenses.
 * [METAMODEL-33] - Ensured that Apache Rat plugin for Maven is properly activated.
 * [METAMODEL-37] - Removed old site sources from project.

### Apache MetaModel 4.0.0-incubating

 * [METAMODEL-9] - SalesforceDataSet is throwing exception for insert sql of record having date/time.
 * [METAMODEL-4] - Use folder name as schema name for file based DataContexts
 * [METAMODEL-5] - Faster CsvDataContext implementation for single-line values
 * [METAMODEL-26] - Provide endpoint URL with SalesforceDataContext
 * Upgraded Apache POI dependency to v. 3.9
 * Improved fluent Query builder API by adding string parameter based methods for joining tables
 * Added a utility ObjectInputStream for deserializing legacy MetaModel objects
 * Performance improvement to CSV reading when values are only single line based
 * Setting up the project on apache infrastructure
 * [METAMODEL-10] - Exclude Jackcess dependency (Access module) from MetaModel
 * Renaming the package hierarchy from org.eobjects.metamodel to org.apache.metamodel
 * [METAMODEL-29] - Fixed issue in CreateTable builder class, causing it to only support a single column definition
 * [METAMODEL-30] - Fixed issue with count(*) queries on CSV resources that does not provide a byte stream length 
