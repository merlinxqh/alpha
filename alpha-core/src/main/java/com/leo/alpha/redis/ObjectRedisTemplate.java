package com.leo.alpha.redis;

import com.leo.alpha.utils.serializer.ProtoStuffSerializerUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.connection.RedisZSetCommands;
import org.springframework.data.redis.core.ListOperations;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

@Component
public class ObjectRedisTemplate {
    
	@Autowired
	private RedisTemplate<String, String> redisTemplate;

	/**
	 * 保存string到redis
	 * @param key
	 * @param value
	 */
	public void setString(String key,String value){
		redisTemplate.opsForValue().set(key,value);
	}

	/**
	 *
	 * @param key
	 * @param value
	 * @param seconds
	 */
	public void setString(String key,String value,long seconds){
		redisTemplate.opsForValue().set(key,value,seconds, TimeUnit.SECONDS);
	}

	/**
	 * get string
	 * @param key
	 * @return
	 */
	public String getString(String key){
		return redisTemplate.opsForValue().get(key);
	}


	/**
	 * 保存对象到redis
	 * @param key
	 * @param obj
	 */
	public <T> boolean setObj(String key,T obj){
		final byte[] bkey = key.getBytes();
		final byte[] bvalue = ProtoStuffSerializerUtils.serialize(obj);
		boolean result=redisTemplate.execute(new RedisCallback<Boolean>() {
			@Override
			public Boolean doInRedis(RedisConnection connection) throws DataAccessException {
				connection.set(bkey, bvalue);
				return true;
			}
		});

		return result;
	}

	/**
	 * 保存list 对象到redis
	 * @param key
	 * @param list
	 * @return
	 */
	public <T> boolean setListObj(String key, List<T> list){
		final byte[] bkey = key.getBytes();
		final byte[] bvalue = ProtoStuffSerializerUtils.serializeList(list);
		boolean result=redisTemplate.execute(new RedisCallback<Boolean>() {
			@Override
			public Boolean doInRedis(RedisConnection connection) throws DataAccessException {
				connection.set(bkey, bvalue);
				return true;
			}
		});

		return result;
	}

	/**
	 * 保存list 对象到redis
	 * 设置过期时间
	 * @param key
	 * @param list
	 * @return
	 */
	public <T> boolean setListObj(String key, List<T> list,final long expireTime){
		final byte[] bkey = key.getBytes();
		final byte[] bvalue = ProtoStuffSerializerUtils.serializeList(list);
		boolean result=redisTemplate.execute(new RedisCallback<Boolean>() {
			@Override
			public Boolean doInRedis(RedisConnection connection) throws DataAccessException {
				connection.setEx(bkey, expireTime, bvalue);
				return true;
			}
		});

		return result;
	}


	public <T> boolean setObj(String key,T obj,final long expireTime){
		final byte[] bkey = key.getBytes();
		final byte[] bvalue = ProtoStuffSerializerUtils.serialize(obj);
		boolean result=redisTemplate.execute(new RedisCallback<Boolean>() {
			@Override
			public Boolean doInRedis(RedisConnection connection) throws DataAccessException {
				connection.setEx(bkey, expireTime, bvalue);
				return true;
			}
		});

		return result;
	}

	/**
	 * 从redis中读取对象
	 * @param key
	 * @param targetClass
	 * @return
	 */
	public <T> T getObj(String key, Class<T> targetClass){
		byte[] result = redisTemplate.execute(new RedisCallback<byte[]>() {
			@Override
			public byte[] doInRedis(RedisConnection connection) throws DataAccessException {
				return connection.get(key.getBytes());
			}
		});
		if (result == null) {
			return null;
		}
		return ProtoStuffSerializerUtils.deserialize(result, targetClass);
	}

	/**
	 * 从redis中读取list对象
	 * @param key
	 * @param targetClass
	 * @return
	 */
	public <T> List<T> getListObj(String key, Class<T> targetClass){
		byte[] result = redisTemplate.execute(new RedisCallback<byte[]>() {
			@Override
			public byte[] doInRedis(RedisConnection connection) throws DataAccessException {
				return connection.get(key.getBytes());
			}
		});
		if (result == null) {
			return null;
		}
		return ProtoStuffSerializerUtils.deserializeList(result, targetClass);
	}

	/**
	 * ----------------------------hashMap start---------------------------------
	 */
	/**
	 * 保存到hash集合中
	 * @param hName  集合名称
	 * @param key
	 * @param value
	 */
	public void hashSet(String hName, String key, String value){
       redisTemplate.opsForHash().put(hName, key, value);
	}

	/**
	 * 判断hashmap中是否包含key
	 * @param hashName
	 * @param key
	 * @return
	 */
	public boolean hashExists(String hashName,String key){
		return redisTemplate.execute((RedisCallback<Boolean>) connection -> {
			return connection.hExists(hashName.getBytes(), key.getBytes());
		});
	}

	/**
	 * 根据集合名称  获取所有的值
	 * @param mapName
	 * @return
	 */
	public Map<Object, Object> hashGetAll(String mapName){
		return redisTemplate.opsForHash().entries(mapName);
	}

	/**
	 * 从hash map中取得复杂类型数据
	 *
	 * @param key
	 * @param field
	 * @param clazz
	 */
	public  <T> T getObjFromMap(String key, String field, Class<T> clazz) {

		byte[] input = redisTemplate.execute((RedisCallback<byte[]>) connection -> {
			return connection.hGet(key.getBytes(), field.getBytes());
		});
		return ProtoStuffSerializerUtils.deserialize(input,clazz);
	}

	/**
	 * set对象到map
	 * @param mapKey
	 * @param filed
	 * @param obj
	 * @param <T>
	 */
	public <T> void setObjToMap(String mapKey,String filed,T obj){
		final byte[] bmapKey = mapKey.getBytes();
		final byte[] bfiled=filed.getBytes();
		final byte[] bvalue = ProtoStuffSerializerUtils.serialize(obj);
		boolean result=redisTemplate.execute(new RedisCallback<Boolean>() {
			@Override
			public Boolean doInRedis(RedisConnection connection) throws DataAccessException {
				connection.hSet(bmapKey,bfiled, bvalue);
				return true;
			}
		});
	}

	/**
	 * 从hashmap中删除一个值
	 *
	 * @param key
	 *            map名
	 * @param field
	 *            成员名称
	 */
	public  void delFromMap(String key, String field) {
		redisTemplate.execute((RedisCallback<Long>) connection -> connection.hDel(key.getBytes(), field.getBytes()));
	}
	/**
	 * ----------------------------hashMap end---------------------------------
	 */

	/**
	 * ----------------------------set start---------------------------------
	 */

	/**
	 * 保存数据到 set集合中
	 * @param key
	 * @param value
	 */
	public void saveToSet(String key,String value){
		redisTemplate.opsForSet().add(key,value);
	}


	/**
	 * 验证set中是否包含
	 * @param key
	 * @param val
	 * @return
	 */
	public boolean existsInSet(String key, String val) {
		return redisTemplate.execute((RedisCallback<Boolean>) connection -> {
			return connection.sIsMember(key.getBytes(), val.getBytes());
		});
	}

	/**
	 * 列出set中所有成员
	 * @param setName
	 * @return
	 */
	public  Set<String> listSet(String setName) {
		return redisTemplate.opsForSet().members(setName);
	}


	/**
	 * 从set中获取一个
	 * @param setName
	 * @return
	 */
	public String popSet(String setName){
		return redisTemplate.opsForSet().pop(setName);
	}

	/**
	 * 逆序列出sorted set包括分数的set列表
	 *
	 * @param key
	 *            set名
	 * @param start
	 *            开始位置
	 * @param end
	 *            结束位置
	 * @return 列表
	 */
	public  Set<RedisZSetCommands.Tuple> listSortedsetRev(String key, int start, int end) {
		return redisTemplate.execute((RedisCallback<Set<RedisZSetCommands.Tuple>>) connection -> {
			return connection.zRevRangeWithScores(key.getBytes(), start, end);
		});
	}

	/**
	 * 逆序取得sorted sort排名
	 *
	 * @param key
	 *            set名
	 * @param member
	 *            成员名
	 * @return 排名
	 */
	public  Long getRankRev(String key, String member) {

		return redisTemplate.execute((RedisCallback<Long>) connection -> {
			return connection.zRevRank(key.getBytes(), member.getBytes());
		});

	}

	/**
	 * 根据成员名取得sorted sort分数
	 *
	 * @param key
	 *            set名
	 * @param member
	 *            成员名
	 * @return 分数
	 */
	public Double getMemberScore(String key, String member) {

		return redisTemplate.execute((RedisCallback<Double>) connection -> {
			return connection.zScore(key.getBytes(), member.getBytes());
		});
	}

	/**
	 * 向sorted set中追加一个值
	 *
	 * @param key
	 *            set名
	 * @param score
	 *            分数
	 * @param member
	 *            成员名称
	 */
	public void saveToSortedset(String key, Double score, String member) {

		redisTemplate.execute((RedisCallback<Boolean>) connection -> connection.zAdd(key.getBytes(), score, member.getBytes()));
	}

	/**
	 * 从sorted set删除一个值
	 *
	 * @param key
	 *            set名
	 * @param member
	 *            成员名称
	 */
	public  void delFromSortedset(String key, String member) {
		redisTemplate.execute((RedisCallback<Long>) connection -> connection.zRem(key.getBytes(), member.getBytes()));

	}

	/**
	 * ----------------------------set end---------------------------------
	 */


	/**
	 * ----------------------------queue start---------------------------------
	 */

	/**
	 * 存到指定的队列中
	 * 左进右出
	 * @param key
	 * @param value
	 * @param size 队列大小限制 0 为不限制
	 */
	public void setToQueue(String key,String value, long size){
		ListOperations<String, String> lo=redisTemplate.opsForList();
        if(size > 0 && lo.size(key) >= size){
        	lo.rightPop(key);
		}
		lo.leftPush(key,value);
	}

	/**
	 * 从队列中取得数据
	 * @param key
	 * @param size
	 * @return
	 */
	public List<String> getFromQueue(String key,long size){
		boolean flag=redisTemplate.execute((RedisCallback<Boolean>) connection -> {
           return connection.exists(key.getBytes());
		});
		if(!flag){
			return new ArrayList<String>();
		}
		ListOperations<String, String> lo=redisTemplate.opsForList();
		if (size > 0) {
			return lo.range(key, 0, size - 1);
		} else {
			return lo.range(key, 0, lo.size(key) - 1);
		}
	}

	/**
	 * 从指定的队列里取得数据
	 * @param key
	 * @return
	 */
	public String popQueue(String key){
		return redisTemplate.opsForList().rightPop(key);
	}

	/**
	 * ----------------------------queue end---------------------------------
	 */


	/**
	 * ----------------------------increment start---------------------------------
	 */
	/**
	 * 将自增变量存到redis
	 * @param key
	 * @param value
	 */
	public void setIncrement(String key,long value){
        redisTemplate.opsForValue().increment(key,value);
	}

	/**
	 *  自增
	 * @param key
	 * @return
	 */
	public Long increment(String key) {

		return redisTemplate.execute((RedisCallback<Long>) connection -> {

			return connection.incr(key.getBytes());

		});
	}

	/**
	 * 自增by value
	 *
	 * @param key
	 * @return
	 */
	public  Long incrementBy(String key, long value) {
		return redisTemplate.execute((RedisCallback<Long>) connection -> {

			return connection.incrBy(key.getBytes(), value);
		});

	}

	/**
	 * 将序列值回退一个
	 *
	 * @param key
	 * @return
	 */
	public void incrementBack(String key) {
		redisTemplate.execute((RedisCallback<Long>) connection -> connection.decr(key.getBytes()));
	}

	/**
	 * ----------------------------increment end---------------------------------
	 */

	/**
	 * 将 key的值保存为 value ，当且仅当 key 不存在。 若给定的 key 已经存在，则 SETNX 不做任何动作。 SETNX 是『SET
	 * if Not eXists』(如果不存在，则 SET)的简写。 <br>
	 * 保存成功，返回 true <br>
	 * 保存失败，返回 false
	 */
	public boolean setNx(String key,String value){
		/** 设置成功，返回 1 设置失败，返回 0 **/
		return redisTemplate.execute((RedisCallback<Boolean>) connection -> {
			return connection.setNX(key.getBytes(), value.getBytes());
		});
	}

	/**
	 * 判断key是否存在
	 * @param key
	 * @return
	 */
	public boolean exists(String key){
		return redisTemplate.execute((RedisCallback<Boolean>) connection -> {
			return connection.exists(key.getBytes());
		});
	}

	/**
	 * 删除数据
	 * @param key
	 */
	public void delKey(String key){
		redisTemplate.execute((RedisCallback<Long>) connection -> connection.del(key.getBytes()));
	}

	/**
	 * 设置超时时间
	 * @param key
	 * @param seconds
	 */
	public void expire(String key, int seconds){
		redisTemplate.execute((RedisCallback<Boolean>) connection -> connection.expire(key.getBytes(), seconds));
	}

}
