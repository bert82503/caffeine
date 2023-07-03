/*
 * Copyright 2015 Ben Manes. All Rights Reserved.
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

/**
 * This package contains in-memory caching functionality. All cache variants are configured and
 * created by using the {@link com.github.benmanes.caffeine.cache.Caffeine Caffeine} builder.
 * 本包包含内存缓存功能。
 * 所有的缓存变体都是通过使用Caffeine构建器配置和创建的。
 * <p>
 * A {@link com.github.benmanes.caffeine.cache.Cache Cache} provides similar characteristics as
 * {@link java.util.concurrent.ConcurrentHashMap ConcurrentHashMap} with additional support for policies to bound the
 * map by. When built with a {@link com.github.benmanes.caffeine.cache.CacheLoader CacheLoader}, the
 * {@link com.github.benmanes.caffeine.cache.LoadingCache LoadingCache} variant allows the cache to populate
 * itself on miss and offers refresh capabilities.
 * Cache提供了与ConcurrentHashMap类似的特性，并额外支持绑定映射的策略。
 * 当使用CacheLoader构建时，LoadingCache变体允许缓存在未命中时自行填充，并提供刷新功能。
 * <p>
 * A {@link com.github.benmanes.caffeine.cache.AsyncCache AsyncCache} is similar to a
 * {@link com.github.benmanes.caffeine.cache.Cache Cache} except that a cache entry holds a
 * {@link java.util.concurrent.CompletableFuture CompletableFuture} of the value. This entry will be automatically
 * removed if the future fails, resolves to {@code null}, or based on an eviction policy. When built
 * with a {@link com.github.benmanes.caffeine.cache.AsyncCacheLoader AsyncCacheLoader}, the
 * {@link com.github.benmanes.caffeine.cache.AsyncLoadingCache AsyncLoadingCache} variant allows the cache to populate
 * itself on miss and offers refresh capabilities.
 * AsyncCache类似于Cache，不同之处在于缓存条目包含值的CompletableFuture。
 * 如果将来失败、解析为null或基于驱逐策略，此条目将自动删除。
 * 当使用AsyncCacheLoader构建时，AsyncLoadingCache变体允许缓存在未命中时自行填充，并提供刷新功能。
 * <p>
 * Additional functionality such as bounding by the entry's size, removal notifications, statistics,
 * and eviction policies are described in the {@link com.github.benmanes.caffeine.cache.Caffeine Caffeine}
 * builder.
 * Caffeine构建器中介绍了其他功能，如按条目大小进行绑定、删除通知、统计数据和驱逐策略。
 *
 * @author ben.manes@gmail.com (Ben Manes)
 */
@CheckReturnValue
@DefaultQualifier(value = NonNull.class, locations = TypeUseLocation.FIELD)
@DefaultQualifier(value = NonNull.class, locations = TypeUseLocation.PARAMETER)
@DefaultQualifier(value = NonNull.class, locations = TypeUseLocation.RETURN)
package com.github.benmanes.caffeine.cache;

import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.framework.qual.DefaultQualifier;
import org.checkerframework.framework.qual.TypeUseLocation;

import com.google.errorprone.annotations.CheckReturnValue;
