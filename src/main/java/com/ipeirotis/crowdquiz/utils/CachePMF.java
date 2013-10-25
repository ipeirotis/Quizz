package com.ipeirotis.crowdquiz.utils;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Collections;

import net.sf.jsr107cache.Cache;
import net.sf.jsr107cache.CacheException;
import net.sf.jsr107cache.CacheFactory;
import net.sf.jsr107cache.CacheManager;

public class CachePMF {

	public static <T> void put(String key, T obj) {

		Cache cache;

		try {
			CacheFactory cacheFactory = CacheManager.getInstance().getCacheFactory();
			cache = cacheFactory.createCache(Collections.emptyMap());
		} catch (CacheException e) {
			cache = null;
			return;
		}

		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		ObjectOutputStream out;
		try {
			out = new ObjectOutputStream(bos);
			out.writeObject(obj);
		} catch (IOException e) {
			e.printStackTrace();
		}

		cache.put(key, bos.toByteArray());

	}
	
	public static <T> T get(String key, Class<T> type) {

		Cache cache = null;

		try {
			CacheFactory cacheFactory = CacheManager.getInstance()
					.getCacheFactory();
			cache = cacheFactory.createCache(Collections.emptyMap());
		} catch (CacheException e) {
			return null;
		}


		T result = null;
		if (cache != null && cache.containsKey(key)) {
			byte[] value = (byte[]) cache.get(key);
			ObjectInputStream in;
			try {
				in = new ObjectInputStream(new ByteArrayInputStream(value));
				result = type.cast(in.readObject());
			} catch (Exception e) {
				e.printStackTrace();
			}

		} 
		return result;
	}

}
