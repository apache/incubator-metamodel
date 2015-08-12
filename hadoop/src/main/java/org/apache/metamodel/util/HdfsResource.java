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
package org.apache.metamodel.util;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.util.Arrays;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FSDataOutputStream;
import org.apache.hadoop.fs.FileStatus;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.fs.PathFilter;
import org.apache.metamodel.MetaModelException;

/**
 * A {@link Resource} implementation that connects to Apache Hadoop's HDFS
 * distributed file system.
 */
public class HdfsResource implements Resource, Serializable {
    private static class HdfsFileInputStream extends InputStream {
        private final InputStream _in;
        private final FileSystem _fs;

        public HdfsFileInputStream(final InputStream in, final FileSystem fs) {
            _in = in;
            _fs = fs;
        }

        @Override
        public int read() throws IOException {
            return _in.read();
        }

        @Override
        public int read(byte[] b, int off, int len) throws IOException {
            return _in.read(b, off, len);
        }

        @Override
        public int read(byte[] b) throws IOException {
            return _in.read(b);
        }

        @Override
        public boolean markSupported() {
            return _in.markSupported();
        }

        @Override
        public synchronized void mark(int readLimit) {
            _in.mark(readLimit);
        }

        @Override
        public int available() throws IOException {
            return _in.available();
        }

        @Override
        public synchronized void reset() throws IOException {
            _in.reset();
        }

        @Override
        public long skip(long n) throws IOException {
            return _in.skip(n);
        }

        @Override
        public void close() throws IOException {
            super.close();
            // need to close 'fs' when input stream is closed
            FileHelper.safeClose(_fs);
        }
    }

    private class HdfsDirectoryInputStream extends AbstractDirectoryInputStream<FileStatus> {
        private final Path _hadoopPath;
        private final FileSystem _fs;

        public HdfsDirectoryInputStream(final Path hadoopPath,
                final FileSystem fs) {
            _hadoopPath = hadoopPath;
            _fs = fs;
            FileStatus[] fileStatuses;
            try {
                fileStatuses = _fs.listStatus(_hadoopPath, new PathFilter() {
                    @Override
                    public boolean accept(final Path path) {
                        try {
                            return _fs.isFile(path);
                        } catch (IOException e) {
                            return false;
                        }
                    }
                });
                // Natural ordering is the URL
                Arrays.sort(fileStatuses);

            } catch (IOException e) {
                fileStatuses = new FileStatus[0];
            }
            _files = fileStatuses;
        }

        @Override
        InputStream openStream(final int index) throws IOException {
            final Path nextPath = _files[index].getPath();
            return _fs.open(nextPath);
        }

        @Override
        public void close() throws IOException {
            super.close();
            FileHelper.safeClose(_fs);
        }
    }

    private static final long serialVersionUID = 1L;

    private static final Pattern URL_PATTERN = Pattern.compile("hdfs://(.+):([0-9]+)/(.*)");

    private final String _hostname;
    private final int _port;
    private final String _filepath;
    private Path _path;
    private boolean _overwriteIfExists = false;
    private Configuration _hadoopConfiguration;

    /**
     * Creates a {@link HdfsResource}
     * 
     * @param url
     *            a URL of the form: hdfs://hostname:port/path/to/file
     * @param overwriteIfExits
     *            If true overwrites the file if exists. Default is false
     */
    public HdfsResource(String url, boolean overwriteIfExists) {
        this(url);
        _overwriteIfExists = overwriteIfExists;
    }

    /**
     * Creates a {@link HdfsResource}
     *
     * @param url
     *            a URL of the form: hdfs://hostname:port/path/to/file
     */
    public HdfsResource(String url) {
        if (url == null) {
            throw new IllegalArgumentException("Url cannot be null");
        }
        final Matcher matcher = URL_PATTERN.matcher(url);
        if (!matcher.find()) {
            throw new IllegalArgumentException("Cannot parse url '" + url
                    + "'. Must follow pattern: hdfs://hostname:port/path/to/file");
        }
        _hostname = matcher.group(1);
        _port = Integer.parseInt(matcher.group(2));
        _filepath = '/' + matcher.group(3);
    }

    /**
     * Creates a {@link HdfsResource}
     *
     * @param hostname
     *            the HDFS (namenode) hostname
     * @param port
     *            the HDFS (namenode) port number
     * @param filepath
     *            the path on HDFS to the file, starting with slash ('/')
     * 
     * @param overwriteIfExits
     *            If true overwrites the file if exists. Default is false
     */
    public HdfsResource(String hostname, int port, String filepath, boolean overwriteIfExists) {
        this(hostname, port, filepath);
        _overwriteIfExists = overwriteIfExists;
    }

    /**
     * Creates a {@link HdfsResource}
     * 
     * @param hostname
     *            the HDFS (namenode) hostname
     * @param port
     *            the HDFS (namenode) port number
     * @param filepath
     *            the path on HDFS to the file, starting with slash ('/')
     */
    public HdfsResource(String hostname, int port, String filepath) {
        _hostname = hostname;
        _port = port;
        _filepath = filepath;
    }

    public String getFilepath() {
        return _filepath;
    }

    public String getHostname() {
        return _hostname;
    }

    public int getPort() {
        return _port;
    }

    @Override
    public String getName() {
        final int lastSlash = _filepath.lastIndexOf('/');
        if (lastSlash != -1) {
            return _filepath.substring(lastSlash + 1);
        }
        return _filepath;
    }

    @Override
    public String getQualifiedPath() {
        return "hdfs://" + _hostname + ":" + _port + _filepath;
    }

    @Override
    public boolean isReadOnly() {
        // We assume it is not read-only
        return false;
    }

    @Override
    public boolean isExists() {
        final FileSystem fs = getHadoopFileSystem();
        try {
            return fs.exists(getHadoopPath());
        } catch (Exception e) {
            throw wrapException(e);
        } finally {
            FileHelper.safeClose(fs);
        }
    }

    @Override
    public long getSize() {
        final FileSystem fs = getHadoopFileSystem();
        try {
            return fs.getFileStatus(getHadoopPath()).getLen();
        } catch (Exception e) {
            throw wrapException(e);
        } finally {
            FileHelper.safeClose(fs);
        }
    }

    @Override
    public long getLastModified() {
        final FileSystem fs = getHadoopFileSystem();
        try {
            return fs.getFileStatus(getHadoopPath()).getModificationTime();
        } catch (Exception e) {
            throw wrapException(e);
        } finally {
            FileHelper.safeClose(fs);
        }
    }

    /**
     * Creates a HDFS file with 1 replica. If the file exists it can be
     * overwritten if the {@link _overwriteIfExists} is set to true
     */
    @Override
    public void write(final Action<OutputStream> writeCallback) throws ResourceException {
        final FileSystem fs = getHadoopFileSystem();
        try {
            final FSDataOutputStream out = fs.create(getHadoopPath(), getOverwriteIfExits());
            try {
                writeCallback.run(out);
            } finally {
                FileHelper.safeClose(out);
            }
        } catch (Exception e) {
            throw wrapException(e);
        } finally {
            FileHelper.safeClose(fs);
        }
    }

    /**
     * Can append only if the number of replicas is 1 (Hadoop condition).
     */
    @Override
    public void append(Action<OutputStream> appendCallback) throws ResourceException {
        final FileSystem fs = getHadoopFileSystem();
        try {
            final FSDataOutputStream out = fs.append(getHadoopPath());
            try {
                appendCallback.run(out);
            } finally {
                FileHelper.safeClose(out);
            }
        } catch (Exception e) {
            throw wrapException(e);
        } finally {
            FileHelper.safeClose(fs);
        }
    }

    @Override
    public InputStream read() throws ResourceException {
        final FileSystem fs = getHadoopFileSystem();
        final InputStream in;
        try {
            final Path hadoopPath = getHadoopPath();
            // return a wrapper InputStream which manages the 'fs' closeable
            if (fs.isFile(hadoopPath)) {
                in = fs.open(hadoopPath);
                return new HdfsFileInputStream(in, fs);
            } else {
                return new HdfsDirectoryInputStream(hadoopPath, fs);
            }
        } catch (Exception e) {
            // we can close 'fs' in case of an exception
            FileHelper.safeClose(fs);
            throw wrapException(e);
        }

    }

    @Override
    public void read(Action<InputStream> readCallback) throws ResourceException {
        final FileSystem fs = getHadoopFileSystem();
        try {
            final InputStream in = read();
            try {
                readCallback.run(in);
            } finally {
                FileHelper.safeClose(in);
            }
        } catch (Exception e) {
            throw wrapException(e);
        } finally {
            FileHelper.safeClose(fs);
        }
    }

    @Override
    public <E> E read(Func<InputStream, E> readCallback) throws ResourceException {
        final FileSystem fs = getHadoopFileSystem();
        try {
            final InputStream in = read();
            try {
                return readCallback.eval(in);
            } finally {
                FileHelper.safeClose(in);
            }
        } catch (Exception e) {
            throw wrapException(e);
        } finally {
            FileHelper.safeClose(fs);
        }
    }

    private RuntimeException wrapException(Exception e) {
        if (e instanceof RuntimeException) {
            return (RuntimeException) e;
        }
        return new MetaModelException(e);
    }

    /**
     * The number of replicas is set to 1 so that we can append to the file that
     * is created. If we do not specify the number of replicas the default is 3
     */
    public Configuration getHadoopConfiguration() {
        if (_hadoopConfiguration == null) {
            final Configuration conf = new Configuration();
            conf.set("fs.defaultFS", "hdfs://" + _hostname + ":" + _port);
            conf.set("dfs.replication", "1");
            _hadoopConfiguration = conf;
        }
        return _hadoopConfiguration;
    }

    public FileSystem getHadoopFileSystem() {
        try {
            return FileSystem.get(getHadoopConfiguration());
        } catch (IOException e) {
            throw new MetaModelException("Could not connect to HDFS: " + e.getMessage(), e);
        }
    }

    public Path getHadoopPath() {
        if (_path == null) {
            _path = new Path(_filepath);
        }
        return _path;
    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(new Object[] { _filepath, _hostname, _port });
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        HdfsResource other = (HdfsResource) obj;
        if (_filepath == null) {
            if (other._filepath != null)
                return false;
        } else if (!_filepath.equals(other._filepath))
            return false;
        if (_hostname == null) {
            if (other._hostname != null)
                return false;
        } else if (!_hostname.equals(other._hostname))
            return false;
        if (_port != other._port)
            return false;
        return true;
    }

    public boolean getOverwriteIfExits() {
        return _overwriteIfExists;
    }

    public void setOverwriteIfExits(boolean overwriteIfExits) {
        _overwriteIfExists = overwriteIfExits;
    }

    public void setHadoopConfiguration(Configuration hadoopConfiguration) {
        _hadoopConfiguration = hadoopConfiguration;
    }

    public void replicateFile(int replicationFactor) {
        try {
            final FileSystem fs = getHadoopFileSystem();
            fs.setReplication(getHadoopPath(), (short) replicationFactor);
        } catch (IOException e) {
            throw new MetaModelException("Could not create replicas for Hdfs file  : " + getHadoopFileSystem() + " "
                    + e.getMessage(), e);
        }
    }
}
