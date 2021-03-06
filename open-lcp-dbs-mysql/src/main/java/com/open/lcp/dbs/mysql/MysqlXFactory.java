package com.open.lcp.dbs.mysql;

import java.sql.SQLException;

import javax.sql.DataSource;

import org.I0Itec.zkclient.ZkClient;
import org.I0Itec.zkclient.exception.ZkMarshallingError;
import org.I0Itec.zkclient.serialize.ZkSerializer;
import org.apache.commons.dbcp2.BasicDataSource;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.google.gson.Gson;
import com.open.lcp.core.env.LcpResource;
import com.open.lcp.core.env.finder.EnvFinder;
import com.open.lcp.core.env.finder.ZKFinder;
import com.open.lcp.core.register.mangocity.zk.ConfigChangeListener;
import com.open.lcp.core.register.mangocity.zk.ConfigChangeSubscriber;
import com.open.lcp.core.register.mangocity.zk.ZkConfigChangeSubscriberImpl;
import com.open.lcp.orm.jade.dataaccess.DataSourceFactory;
import com.open.lcp.orm.jade.dataaccess.DataSourceHolder;
import com.open.lcp.orm.jade.dataaccess.datasource.HierarchicalDataSourceFactory;

public class MysqlXFactory {

	private static final Log logger = LogFactory.getLog(MysqlXFactory.class);

	private static HierarchicalDataSourceFactory hierarchicalDataSourceFactory = new HierarchicalDataSourceFactory();

	private static Gson gson = new Gson();

	private static final Object LOCK_OF_NEWPATH = new Object();

	public static DataSourceFactory loadMysqlX(final LcpResource lcpResource) {
		//String zkAbsolutePath = lcpResource.getAbsolutePath(EnvFinder.getProfile());
		DataSourceHolder dsHolder = hierarchicalDataSourceFactory.getHolder(lcpResource.getClassDeclaredDataSourceAnnotationName());
		if (dsHolder == null) {
			synchronized (LOCK_OF_NEWPATH) {
				dsHolder = hierarchicalDataSourceFactory.getHolder(lcpResource.getClassDeclaredDataSourceAnnotationName());
				if (dsHolder == null) {
					ZkClient zkClient = null;
					try {
						zkClient = new ZkClient(ZKFinder.findZKHosts(), 180000, 180000, new ZkSerializer() {

							@Override
							public byte[] serialize(Object paramObject) throws ZkMarshallingError {
								return paramObject == null ? null : paramObject.toString().getBytes();
							}

							@Override
							public Object deserialize(byte[] paramArrayOfByte) throws ZkMarshallingError {
								return new String(paramArrayOfByte);
							}
						});

						ConfigChangeSubscriber sub = new ZkConfigChangeSubscriberImpl(zkClient,
								lcpResource.getAbsoluteParentPath(EnvFinder.getProfile()));
						sub.subscribe(lcpResource.getNodeName(), new ConfigChangeListener() {

							@Override
							public void configChanged(String key, String value) {
								MySQLDBConfig dbconfig = loadDBConfig(value);
								DataSource newds = load(lcpResource, dbconfig);
								
								DataSourceHolder oldDSHolder = hierarchicalDataSourceFactory.getHolder(lcpResource.getClassDeclaredDataSourceAnnotationName());
								hierarchicalDataSourceFactory.replaceHolder(lcpResource.getClassDeclaredDataSourceAnnotationName(),
										newds);
								
								if(oldDSHolder!=null){
									BasicDataSource ds = (BasicDataSource)oldDSHolder.getDataSource();
									try {
										ds.close();
									} catch (SQLException e) {
										logger.error(e.getMessage(),e);
									}
								}
							}
						});

						MySQLDBConfig dbconfig = loadDBConfig(lcpResource, zkClient);
						DataSource ds = load(lcpResource, dbconfig);

						hierarchicalDataSourceFactory.registerDataSource(lcpResource.getClassDeclaredDataSourceAnnotationName(), ds);
					} catch (Exception e) {
						logger.error(e.getMessage(), e);
						System.exit(-1);
					} finally {
						if (zkClient != null) {
							zkClient.close();
						}
					}
				}
			}
		}
		return hierarchicalDataSourceFactory;
	}

	private static MySQLDBConfig loadDBConfig(LcpResource zkResourcePath, ZkClient zkClient) {
		String ssdbStr = zkClient.readData(zkResourcePath.getAbsolutePath(EnvFinder.getProfile()));
		return loadDBConfig(ssdbStr);
	}

	private static MySQLDBConfig loadDBConfig(String jsonStr) {
		MySQLDBConfig dbConfig = gson.fromJson(jsonStr, MySQLDBConfig.class);
		return dbConfig;
	}

	private static DataSource load(final LcpResource lcpResource, final MySQLDBConfig dbconfig) {
		BasicDataSource ds = new BasicDataSource();
		// ds.setDriverClassName("com.mysql.jdbc.Driver");
		// ds.setUrl("jdbc:mysql://123.57.204.187:3306/lcp?useUnicode=true&amp;characterEncoding=utf-8");
		// ds.setUsername("root");
		// ds.setPassword("111111");
		ds.setDriverClassName(dbconfig.getDriverClassName());
		ds.setUrl(dbconfig.getUrl());
		ds.setUsername(dbconfig.getUserName());
		ds.setPassword(dbconfig.getPassword());
		ds.setTimeBetweenEvictionRunsMillis(3600000);
		ds.setMinEvictableIdleTimeMillis(3600000);
		
		return ds;
	}

}
