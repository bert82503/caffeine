/*
 * Copyright 2018 Ben Manes. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.github.benmanes.caffeine.cache;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.Executor;
import java.util.function.BiFunction;
import java.util.function.Function;

import org.checkerframework.checker.nullness.qual.Nullable;

/**
 * A semi-persistent mapping from keys to values. Cache entries are manually added using
 * {@link #get(Object, Function)} or {@link #put(Object, CompletableFuture)}, and are stored in the
 * cache until either evicted or manually invalidated.
 * 异步缓存接口，从键到值的半持久映射。
 * 缓存条目使用{@link #get(Object, Function)}或{@link #put(Object, CompletableFuture)}手动添加，并存储在缓存中，直到被逐出或手动无效。
 * <p>
 * Implementations of this interface are expected to be thread-safe, and can be safely accessed by
 * multiple concurrent threads.
 * 该接口的实现应该是线程安全的，并且可以由多个并发线程安全地访问。
 *
 * @author ben.manes@gmail.com (Ben Manes)
 * @param <K> the type of keys maintained by this cache
 * @param <V> the type of mapped values
 */
public interface AsyncCache<K, V> {

  // 查询操作

  /**
   * Returns the future associated with {@code key} in this cache, or {@code null} if there is no
   * cached future for {@code key}.
   *
   * @param key key whose associated value is to be returned
   * @return the future value to which the specified key is mapped, or {@code null} if this cache
   *         does not contain a mapping for the key
   * @throws NullPointerException if the specified key is null
   */
  @Nullable
  CompletableFuture<V> getIfPresent(K key);

  /**
   * Returns the future associated with {@code key} in this cache, obtaining that value from
   * {@code mappingFunction} if necessary. This method provides a simple substitute for the
   * conventional "if cached, return; otherwise create, cache and return" pattern.
   * <p>
   * If the specified key is not already associated with a value, attempts to compute its value
   * asynchronously and enters it into this cache unless {@code null}. The entire method invocation
   * is performed atomically, so the function is applied at most once per key. If the asynchronous
   * computation fails, the entry will be automatically removed from this cache.
   * <p>
   * <b>Warning:</b> as with {@link CacheLoader#load}, {@code mappingFunction} <b>must not</b>
   * attempt to update any other mappings of this cache.
   *
   * @param key key with which the specified value is to be associated
   * @param mappingFunction the function to asynchronously compute a value
   * @return the current (existing or computed) future value associated with the specified key
   * @throws NullPointerException if the specified key or mappingFunction is null
   */
  CompletableFuture<V> get(K key, Function<? super K, ? extends V> mappingFunction);

  /**
   * Returns the future associated with {@code key} in this cache, obtaining that value from
   * {@code mappingFunction} if necessary. This method provides a simple substitute for the
   * conventional "if cached, return; otherwise create, cache and return" pattern. The instance
   * returned from the {@code mappingFunction} will be stored directly into the cache.
   * <p>
   * If the specified key is not already associated with a value, attempts to compute its value
   * asynchronously and enters it into this cache unless {@code null}. The entire method invocation
   * is performed atomically, so the function is applied at most once per key. If the asynchronous
   * computation fails, the entry will be automatically removed from this cache.
   * <p>
   * <b>Warning:</b> as with {@link CacheLoader#load}, {@code mappingFunction} <b>must not</b>
   * attempt to update any other mappings of this cache.
   *
   * @param key key with which the specified value is to be associated
   * @param mappingFunction the function to asynchronously compute a value, optionally using the
   *        given executor
   * @return the current (existing or computed) future value associated with the specified key
   * @throws NullPointerException if the specified key or mappingFunction is null, or if the
   *         future returned by the mappingFunction is null
   * @throws RuntimeException or Error if the mappingFunction does when constructing the future,
   *         in which case the mapping is left unestablished
   */
  CompletableFuture<V> get(K key, BiFunction<? super K, ? super Executor,
      ? extends CompletableFuture<? extends V>> mappingFunction);

  /**
   * Returns the future of a map of the values associated with {@code keys}, creating or retrieving
   * those values if necessary. The returned map contains entries that were already cached, combined
   * with newly loaded entries; it will never contain null keys or values. If the any of the
   * asynchronous computations fail, those entries will be automatically removed from this cache.
   * <p>
   * A single request to the {@code mappingFunction} is performed for all keys which are not already
   * present in the cache. If another call to {@link #get} tries to load the value for a key in
   * {@code keys}, that thread retrieves a future that is completed by this bulk computation. Any
   * loaded values for keys that were not specifically requested will not be returned, but will be
   * stored in the cache. Note that multiple threads can concurrently load values for distinct keys.
   * <p>
   * Note that duplicate elements in {@code keys}, as determined by {@link Object#equals}, will be
   * ignored.
   *
   * @param keys the keys whose associated values are to be returned
   * @param mappingFunction the function to asynchronously compute the values
   * @return a future containing an unmodifiable mapping of keys to values for the specified keys in
   *         this cache
   * @throws NullPointerException if the specified collection is null or contains a null element, or
   *         if the future returned by the mappingFunction is null
   * @throws RuntimeException or Error if the mappingFunction does so, in which case the mapping is
   *         left unestablished
   */
  CompletableFuture<Map<K, V>> getAll(Iterable<? extends K> keys,
      Function<? super Set<? extends K>, ? extends Map<? extends K, ? extends V>> mappingFunction);

  /**
   * Returns the future of a map of the values associated with {@code keys}, creating or retrieving
   * those values if necessary. The returned map contains entries that were already cached, combined
   * with newly loaded entries; it will never contain null keys or values. If the any of the
   * asynchronous computations fail, those entries will be automatically removed from this cache.
   * The instances returned from the {@code mappingFunction} will be stored directly into the cache.
   * <p>
   * A single request to the {@code mappingFunction} is performed for all keys which are not already
   * present in the cache. If another call to {@link #get} tries to load the value for a key in
   * {@code keys}, that thread retrieves a future that is completed by this bulk computation. Any
   * loaded values for keys that were not specifically requested will not be returned, but will be
   * stored in the cache. Note that multiple threads can concurrently load values for distinct keys.
   * <p>
   * Note that duplicate elements in {@code keys}, as determined by {@link Object#equals}, will be
   * ignored.
   *
   * @param keys the keys whose associated values are to be returned
   * @param mappingFunction the function to asynchronously compute the values, optionally using the
   *        given executor
   * @return a future containing an unmodifiable mapping of keys to values for the specified keys in
   *         this cache
   * @throws NullPointerException if the specified collection is null or contains a null element, or
   *         if the future returned by the mappingFunction is null
   * @throws RuntimeException or Error if the mappingFunction does so, in which case the mapping is
   *         left unestablished
   */
  CompletableFuture<Map<K, V>> getAll(Iterable<? extends K> keys,
      BiFunction<? super Set<? extends K>, ? super Executor,
          ? extends CompletableFuture<? extends Map<? extends K, ? extends V>>> mappingFunction);

  // 插入操作

  /**
   * Associates {@code value} with {@code key} in this cache. If the cache previously contained a
   * value associated with {@code key}, the old value is replaced by {@code value}. If the
   * asynchronous computation fails, the entry will be automatically removed.
   * <p>
   * Prefer {@link #get(Object, Function)} when using the conventional "if cached, return; otherwise
   * create, cache and return" pattern.
   *
   * @param key key with which the specified value is to be associated
   * @param valueFuture value to be associated with the specified key
   * @throws NullPointerException if the specified key or value is null
   */
  void put(K key, CompletableFuture<? extends V> valueFuture);

  // 并发映射表，视图转换

  /**
   * Returns a view of the entries stored in this cache as a thread-safe map. Modifications made to
   * the map directly affect the cache.
   * <p>
   * A computation operation, such as {@link ConcurrentMap#compute}, performs the entire method
   * invocation atomically, so the function is applied at most once per key. Some attempted update
   * operations by other threads may be blocked while computation is in progress. The computation
   * must not attempt to update any other mappings of this cache.
   * <p>
   * Iterators from the returned map are at least <i>weakly consistent</i>: they are safe for
   * concurrent use, but if the cache is modified (including by eviction) after the iterator is
   * created, it is undefined which of the changes (if any) will be reflected in that iterator.
   *
   * @return a thread-safe view of this cache supporting all of the optional {@link Map} operations
   */
  ConcurrentMap<K, CompletableFuture<V>> asMap();

  /**
   * Returns a view of the entries stored in this cache as a synchronous {@link Cache}. A mapping is
   * not present if the value is currently being loaded. Modifications made to the synchronous cache
   * directly affect the asynchronous cache. If a modification is made to a mapping that is
   * currently loading, the operation blocks until the computation completes.
   * 返回作为同步缓存存储在此缓存中的条目的视图。
   * 如果当前正在加载值，则映射不存在。
   * 对同步缓存所做的修改会直接影响异步缓存。
   * 如果对当前正在加载的映射进行了修改，则操作将阻塞，直到计算完成。
   *
   * @return a thread-safe synchronous view of this cache
   */
  Cache<K, V> synchronous();
}
