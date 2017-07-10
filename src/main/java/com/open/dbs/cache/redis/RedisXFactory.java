package com.open.dbs.cache.redis;

import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.I0Itec.zkclient.ZkClient;
import org.I0Itec.zkclient.exception.ZkMarshallingError;
import org.I0Itec.zkclient.serialize.ZkSerializer;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.google.gson.Gson;
import com.mangocity.zk.ConfigChangeListener;
import com.mangocity.zk.ConfigChangeSubscriber;
import com.mangocity.zk.ZkConfigChangeSubscriberImpl;
import com.open.dbs.cache.redis.cluster.JedisClusterImpl;
import com.open.dbs.cache.redis.single.JedisPoolImpl;
import com.open.env.finder.ZKFinder;

public class RedisXFactory {
	private static final Log logger = LogFactory.getLog(RedisXFactory.class);

	private static final ConcurrentMap<String, RedisX> redisMap = new ConcurrentHashMap<String, RedisX>();

	private static final Object INIT_REDISIMPL_MAP = new Object();

	public static RedisX loadRedisX(final String instanceName) {

		final String redisZKRoot = ZKFinder.findRedisZKRoot();
		RedisX instance = redisMap.get(instanceName);
		if (instance == null) {
			synchronized (INIT_REDISIMPL_MAP) {
				if (instance == null) {
					instance = redisMap.get(instanceName);
					if (instance == null) {
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

							ConfigChangeSubscriber sub = new ZkConfigChangeSubscriberImpl(zkClient, redisZKRoot);
							sub.subscribe(instanceName, new ConfigChangeListener() {

								@Override
								public void configChanged(String key, String value) {
									loadRedisX(key, value);
								}
							});

							loadRedisX(zkClient, redisZKRoot, instanceName);
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
		}
		if (redisMap != null && redisMap.size() > 0) {
			for (Entry<String, RedisX> entry : redisMap.entrySet()) {
				logger.debug("--RedisServiceImpl--" + entry.getKey() + "--" + entry.getValue());
			}
		}

		return redisMap.get(instanceName);
	}

	private static void loadRedisX(String instance, String jsonStr) {
		ZKRedisConfig redisConfig = loadZKRedisConfig(jsonStr);
		loadRedisX(instance, redisConfig);
	}

	private static void loadRedisX(ZkClient zkClient, String ssdbZkRoot, String instance) {
		String ssdbStr = zkClient.readData(ssdbZkRoot + "/" + instance);
		ZKRedisConfig redisConfig = loadZKRedisConfig(ssdbStr);
		loadRedisX(instance, redisConfig);
	}

	private static void loadRedisX(String instance, ZKRedisConfig redisConfig) {
		RedisX redisX = null;
		if (redisConfig.isCluster()) {
			redisX = new JedisPoolImpl(redisConfig);
		} else {
			redisX = new JedisClusterImpl(redisConfig);
		}

		redisX = (RedisX) new RedisProxy(redisX).getProxyInstance();
		redisMap.put(instance, redisX);
	}

	private static ZKRedisConfig loadZKRedisConfig(String jsonStr) {
		Gson gson = new Gson();
		ZKRedisConfig ssdbConfig = gson.fromJson(jsonStr, ZKRedisConfig.class);
		return ssdbConfig;
	}
}