/*
 * Copyright 2014 Ben Manes. All Rights Reserved.
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
import java.util.concurrent.ConcurrentMap;
import java.util.function.Function;

import org.checkerframework.checker.index.qual.NonNegative;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.checkerframework.checker.nullness.qual.PolyNull;

import com.github.benmanes.caffeine.cache.stats.CacheStats;

/**
 * A semi-persistent mapping from keys to values. Cache entries are manually added using
 * {@link #get(Object, Function)} or {@link #put(Object, Object)}, and are stored in the cache until
 * either evicted or manually invalidated.
 * 缓存接口，从键到值的半持久映射。
 * 缓存条目是使用{@link #get(Object, Function)}或{@link #put(Object, Object)}手动添加的，
 * 并存储在缓存中，直到被逐出或手动无效。
 * <p>
 * Implementations of this interface are expected to be thread-safe, and can be safely accessed by
 * multiple concurrent threads.
 * 本接口的实现应该是线程安全的，并且可以由多个并发线程安全地访问。
 *
 * @author ben.manes@gmail.com (Ben Manes)
 * @param <K> the type of keys maintained by this cache
 * @param <V> the type of mapped values
 */
public interface Cache<K, V> {

  // 查询操作

  /**
   * Returns the value associated with the {@code key} in this cache, or {@code null} if there is no
   * cached value for the {@code key}.
   * 返回与此缓存中的键关联的值，如果没有该键的缓存值，则返回null。
   *
   * @param key the key whose associated value is to be returned
   * @return the value to which the specified key is mapped, or {@code null} if this cache does not
   *         contain a mapping for the key
   * @throws NullPointerException if the specified key is null
   */
  @Nullable
  V getIfPresent(K key);

  /**
   * Returns the value associated with the {@code key} in this cache, obtaining that value from the
   * {@code mappingFunction} if necessary. This method provides a simple substitute for the
   * conventional "if cached, return; otherwise create, cache and return" pattern.
   * 返回与此缓存中的键关联的值，必要时从mappingFunction获取该值。
   * 该方法为传统的“如果缓存，则返回；否则创建、缓存并返回”模式提供了一个简单的替代方案。
   * <p>
   * If the specified key is not already associated with a value, attempts to compute its value
   * using the given mapping function and enters it into this cache unless {@code null}. The entire
   * method invocation is performed atomically, so the function is applied at most once per key.
   * Some attempted update operations on this cache by other threads may be blocked while the
   * computation is in progress, so the computation should be short and simple, and must not attempt
   * to update any other mappings of this cache.
   * 如果指定的键尚未与值关联，则尝试使用给定的映射函数计算其值，并将其输入到该缓存中，除非为null。
   * 整个方法调用是以原子方式执行的，因此每个键最多应用一次函数。
   * 在计算过程中，其他线程对此缓存尝试的某些更新操作可能会被阻止，因此计算应简短而简单，并且不得尝试更新此缓存的任何其他映射。
   * <p>
   * <b>Warning:</b> as with {@link CacheLoader#load}, {@code mappingFunction} <b>must not</b>
   * attempt to update any other mappings of this cache.
   * 警告：与{@link CacheLoader#load}一样，mappingFunction不得尝试更新此缓存的任何其他映射。
   *
   * @param key the key with which the specified value is to be associated
   * @param mappingFunction the function to compute a value
   * @return the current (existing or computed) value associated with the specified key, or null if
   *         the computed value is null
   * @throws NullPointerException if the specified key or mappingFunction is null
   * @throws IllegalStateException if the computation detectably attempts a recursive update to this
   *         cache that would otherwise never complete
   * @throws RuntimeException or Error if the mappingFunction does so, in which case the mapping is
   *         left unestablished
   */
  @PolyNull
  V get(K key, Function<? super K, ? extends @PolyNull V> mappingFunction);

  /**
   * Returns a map of the values associated with the {@code keys} in this cache. The returned map
   * will only contain entries which are already present in the cache.
   * 返回与此缓存中的键关联的值的映射。
   * 返回的映射将只包含缓存中已经存在的条目。
   * <p>
   * Note that duplicate elements in {@code keys}, as determined by {@link Object#equals}, will be
   * ignored.
   * 请注意，键中由{@link Object#equals}确定的重复元素将被忽略。
   *
   * @param keys the keys whose associated values are to be returned
   * @return an unmodifiable mapping of keys to values for the specified keys found in this cache
   * @throws NullPointerException if the specified collection is null or contains a null element
   */
  Map<K, V> getAllPresent(Iterable<? extends K> keys);

  /**
   * Returns a map of the values associated with the {@code keys}, creating or retrieving those
   * values if necessary. The returned map contains entries that were already cached, combined with
   * the newly loaded entries; it will never contain null keys or values.
   * <p>
   * A single request to the {@code mappingFunction} is performed for all keys which are not already
   * present in the cache. All entries returned by {@code mappingFunction} will be stored in the
   * cache, over-writing any previously cached values. If another call to {@link #get} tries to load
   * the value for a key in {@code keys}, implementations may either have that thread load the entry
   * or simply wait for this thread to finish and return the loaded value. In the case of
   * overlapping non-blocking loads, the last load to complete will replace the existing entry. Note
   * that multiple threads can concurrently load values for distinct keys. Any loaded values for
   * keys that were not specifically requested will not be returned, but will be stored in the
   * cache.
   * <p>
   * Note that duplicate elements in {@code keys}, as determined by {@link Object#equals}, will be
   * ignored.
   *
   * @param keys the keys whose associated values are to be returned
   * @param mappingFunction the function to compute the values
   * @return an unmodifiable mapping of keys to values for the specified keys in this cache
   * @throws NullPointerException if the specified collection is null or contains a null element, or
   *         if the map returned by the mappingFunction is null
   * @throws RuntimeException or Error if the mappingFunction does so, in which case the mapping is
   *         left unestablished
   */
  Map<K, V> getAll(Iterable<? extends K> keys,
      Function<? super Set<? extends K>, ? extends Map<? extends K, ? extends V>> mappingFunction);

  // 插入操作

  /**
   * Associates the {@code value} with the {@code key} in this cache. If the cache previously
   * contained a value associated with the {@code key}, the old value is replaced by the new
   * {@code value}.
   * 将该值与该缓存中的键相关联。
   * 如果缓存以前包含与键关联的值，则旧值将被新值替换。
   * <p>
   * Prefer {@link #get(Object, Function)} when using the conventional "if cached, return; otherwise
   * create, cache and return" pattern.
   * 当使用传统的“如果缓存，返回；否则创建，缓存并返回”模式时，首选{@link #get(Object, Function)}。
   *
   * @param key the key with which the specified value is to be associated
   * @param value value to be associated with the specified key
   * @throws NullPointerException if the specified key or value is null
   */
  void put(K key, V value);

  /**
   * Copies all of the mappings from the specified map to the cache. The effect of this call is
   * equivalent to that of calling {@code put(k, v)} on this map once for each mapping from key
   * {@code k} to value {@code v} in the specified map. The behavior of this operation is undefined
   * if the specified map is modified while the operation is in progress.
   *
   * @param map the mappings to be stored in this cache
   * @throws NullPointerException if the specified map is null or the specified map contains null
   *         keys or values
   */
  void putAll(Map<? extends K, ? extends V> map);

  // Explicit Removals，显式清除操作

  /**
   * Discards any cached value for the {@code key}. The behavior of this operation is undefined for
   * an entry that is being loaded (or reloaded) and is otherwise not present.
   * 丢弃该键的任何缓存值。
   * 对于正在加载（或重新加载）且在其他情况下不存在的条目，此操作的行为是未定义的。
   *
   * @param key the key whose mapping is to be removed from the cache
   * @throws NullPointerException if the specified key is null
   */
  void invalidate(K key);

  /**
   * Discards any cached values for the {@code keys}. The behavior of this operation is undefined
   * for an entry that is being loaded (or reloaded) and is otherwise not present.
   *
   * @param keys the keys whose associated values are to be removed
   * @throws NullPointerException if the specified collection is null or contains a null element
   */
  void invalidateAll(Iterable<? extends K> keys);

  /**
   * Discards all entries in the cache. The behavior of this operation is undefined for an entry
   * that is being loaded (or reloaded) and is otherwise not present.
   */
  void invalidateAll();

  /**
   * Returns the approximate number of entries in this cache. The value returned is an estimate; the
   * actual count may differ if there are concurrent insertions or removals, or if some entries are
   * pending removal due to expiration or weak/soft reference collection. In the case of stale
   * entries this inaccuracy can be mitigated by performing a {@link #cleanUp()} first.
   * 返回此缓存中的大致条目数。
   * 返回的值是一个估计值；如果存在并发插入或删除，或者如果某些条目由于过期或弱/软引用集合而挂起删除，则实际计数可能会有所不同。
   * 在条目陈旧的情况下，可以通过首先执行{@link #cleanUp()}来减轻这种不准确。
   *
   * @return the estimated number of mappings
   */
  @NonNegative
  long estimatedSize();

  // 缓存性能的统计信息

  /**
   * Returns a current snapshot of this cache's cumulative statistics. All statistics are
   * initialized to zero, and are monotonically increasing over the lifetime of the cache.
   * 返回此缓存的累积统计信息的当前快照。
   * 所有统计信息都初始化为零，并且在缓存的生存期内单调增加。
   * <p>
   * Due to the performance penalty of maintaining statistics, some implementations may not record
   * the usage history immediately or at all.
   * 由于维护统计信息会带来性能损失，一些实现可能不会立即或根本不会记录使用历史。
   *
   * @return the current snapshot of the statistics of this cache
   */
  CacheStats stats();

  // 并发映射表，视图转换

  /**
   * Returns a view of the entries stored in this cache as a thread-safe map. Modifications made to
   * the map directly affect the cache.
   * 返回作为线程安全映射存储在此缓存中的条目的视图。
   * 对视图所做的修改会直接影响缓存。
   * <p>
   * A computation operation, such as {@link ConcurrentMap#compute}, performs the entire method
   * invocation atomically, so the function is applied at most once per key. Some attempted update
   * operations by other threads may be blocked while computation is in progress. The computation
   * must not attempt to update any other mappings of this cache.
   * 计算操作以原子方式执行整个方法调用，因此每个键最多应用一次该函数。
   * 当计算正在进行时，其他线程尝试的某些更新操作可能会被阻止。
   * 计算不得尝试更新此缓存的任何其他映射。
   * <p>
   * Iterators from the returned map are at least <i>weakly consistent</i>: they are safe for
   * concurrent use, but if the cache is modified (including by eviction) after the iterator is
   * created, it is undefined which of the changes (if any) will be reflected in that iterator.
   * 返回映射中的迭代器至少是弱一致的：它们对于并发使用是安全的，
   * 但如果在迭代器创建后修改了缓存(包括通过驱逐)，则不确定哪些更改将反映在该迭代器中。
   *
   * @return a thread-safe view of this cache supporting all of the optional {@link Map} operations
   */
  ConcurrentMap<K, V> asMap();

  // When Does Cleanup Happen?
  // 何时进行清理？

  /**
   * Performs any pending maintenance operations needed by the cache. Exactly which activities are
   * performed -- if any -- is implementation-dependent.
   * 执行缓存所需的任何挂起的维护操作。具体执行哪些活动取决于实现。
   */
  void cleanUp();

  // 缓存策略

  /**
   * Returns access to inspect and perform low-level operations on this cache based on its runtime
   * characteristics. These operations are optional and dependent on how the cache was constructed
   * and what abilities the implementation exposes.
   * 返回访问权限，以便根据该缓存的运行时特性检查该缓存并对其执行低级操作。
   * 这些操作是可选的，取决于缓存是如何构建的以及实现公开了什么功能。
   * <p>
   * <b>Warning:</b> policy operations <b>must not</b> be performed from within an atomic scope of
   * another cache operation.
   * 警告：策略操作不得在另一个缓存操作的原子作用域内执行。
   *
   * @return access to inspect and perform advanced operations based on the cache's characteristics
   */
  Policy<K, V> policy();
}
