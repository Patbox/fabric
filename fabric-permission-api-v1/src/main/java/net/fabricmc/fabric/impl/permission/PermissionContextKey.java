/*
 * Copyright (c) 2016, 2017, 2018, 2019 FabricMC
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

package net.fabricmc.fabric.impl.permission;

import java.util.Set;

import com.google.common.collect.Interner;
import com.google.common.collect.Interners;
import com.google.common.collect.Sets;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DynamicOps;
import org.jspecify.annotations.Nullable;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import net.fabricmc.fabric.api.permission.v1.PermissionContext;

public record PermissionContextKey<T>(Identifier id, @Nullable Codec<T> codec) implements PermissionContext.Key<T> {
	private static final Interner<PermissionContextKey<?>> VALUES = Interners.newWeakInterner();

	public static final PermissionContext.Key<String> NAME = fabricKey("name", Codec.STRING);
	public static final PermissionContext.Key<Vec3> POSITION = fabricKey("position", Vec3.CODEC);
	public static final PermissionContext.Key<BlockPos> BLOCK_POSITION = fabricKey("block_position", BlockPos.CODEC);
	public static final PermissionContext.Key<Entity> ENTITY = fabricKey("entity");
	public static final PermissionContext.Key<CommandSourceStack> COMMAND_SOURCE_STACK = fabricKey("command_source_stack");
	public static final PermissionContext.Key<Level> LEVEL = fabricKey("level");
	public static final PermissionContext.Key<ResourceKey<Level>> LEVEL_KEY = fabricKey("level_key", ResourceKey.codec(Registries.DIMENSION));

	public static final Set<PermissionContext.Key<?>> DEFAULT_COMMON_KEYS = Set.of(POSITION, BLOCK_POSITION, LEVEL, LEVEL_KEY, NAME);
	public static final Set<PermissionContext.Key<?>> DEFAULT_ENTITY_KEYS = Sets.union(DEFAULT_COMMON_KEYS, Set.of(ENTITY));
	public static final Set<PermissionContext.Key<?>> DEFAULT_COMMAND_KEYS = Sets.union(DEFAULT_COMMON_KEYS, Set.of(COMMAND_SOURCE_STACK));
	public static final Set<PermissionContext.Key<?>> DEFAULT_COMMAND_ENTITY_KEYS = Sets.union(DEFAULT_COMMON_KEYS, Set.of(ENTITY, COMMAND_SOURCE_STACK));

	private static <T> PermissionContext.Key<T> fabricKey(String path) {
		return getOrCreateKey(Identifier.fromNamespaceAndPath("fabric", path), null);
	}

	private static <T> PermissionContext.Key<T> fabricKey(String path, Codec<T> codec) {
		return getOrCreateKey(Identifier.fromNamespaceAndPath("fabric", path), codec);
	}

	public static <T> PermissionContext.Key<T> getOrCreateKey(Identifier identifier, @Nullable Codec<T> codec) {
		//noinspection unchecked
		return (PermissionContext.Key<T>) VALUES.intern(new PermissionContextKey<>(identifier, codec));
	}

	@Override
	public String toString() {
		return "PermissionContext.Key[" + id + "]";
	}

	@Override
	public boolean isSerializable() {
		return this.codec != null;
	}

	@Override
	public <Y> Y encodeValue(DynamicOps<Y> ops, T value) {
		if (this.codec == null) {
			throw new IllegalStateException("This " + this + " is not serializable!");
		}

		return this.codec.encodeStart(ops, value).getOrThrow();
	}

	@Override
	public <Y> T decodeValue(DynamicOps<Y> ops, Y value) {
		if (this.codec == null) {
			throw new IllegalStateException("This " + this + " is not serializable!");
		}

		return this.codec.decode(ops, value).getOrThrow().getFirst();
	}
}
