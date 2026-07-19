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

package net.fabricmc.fabric.impl.registry.sync.validate;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtAccounter;
import net.minecraft.nbt.NbtIo;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentUtils;
import net.minecraft.network.chat.TextColor;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.storage.LevelResource;
import net.minecraft.world.level.storage.LevelStorageSource;

import net.fabricmc.fabric.api.event.registry.RegistryAttribute;
import net.fabricmc.fabric.api.event.registry.RegistryAttributeHolder;

public record RegistryCustomContentState(Map<Identifier, List<Identifier>> entries) {
	public static RegistryCustomContentState construct(RegistryAccess registryAccess) {
		var map = new HashMap<Identifier, List<Identifier>>();

		registryAccess.listRegistries().filter(registryLookup -> RegistryAttributeHolder.get(registryLookup.key()).hasAttribute(RegistryAttribute.SAVE_DATA_VALIDATED))
				.forEach(registry -> {
					var list = new ArrayList<Identifier>();
					map.put(registry.key().identifier(), list);

					registry.listElementIds().map(ResourceKey::identifier).filter(id -> !id.getNamespace().equals(Identifier.DEFAULT_NAMESPACE)).forEach(list::add);
				});

		return new RegistryCustomContentState(map);
	}

	private static Path getPath(LevelStorageSource.LevelStorageAccess worldAccess) {
		return worldAccess.getLevelPath(LevelResource.DATA).resolve("fabric").resolve("registry_entries.dat");
	}

	public static void writeFile(LevelStorageSource.LevelStorageAccess storageSource, RegistryAccess registryAccess) {
		try {
			Path path = getPath(storageSource);

			if (!Files.isDirectory(path.getParent())) {
				Files.createDirectories(path.getParent());
			}

			NbtIo.writeCompressed(RegistryCustomContentState.construct(registryAccess).toNbt(), path);
		} catch (Throwable e) {
			// Todo
		}
	}

	public static RegistryCustomContentState readFile(LevelStorageSource.LevelStorageAccess access) {
		Path path = RegistryCustomContentState.getPath(access);

		if (!Files.exists(path)) {
			return new RegistryCustomContentState(Map.of());
		}

		try {
			return RegistryCustomContentState.fromNbt(NbtIo.readCompressed(path, NbtAccounter.unlimitedHeap()));
		} catch (Throwable e) {
			// todo
		}

		return new RegistryCustomContentState(Map.of());
	}

	private static RegistryCustomContentState fromNbt(CompoundTag tag) {
		if (tag.getIntOr("format_version", -1) != 0) {
			// Todo
			throw new RuntimeException("Invalid version!");
		}

		var map = new HashMap<Identifier, List<Identifier>>();

		CompoundTag registries = tag.getCompoundOrEmpty("registries");

		for (String key : registries.keySet()) {
			ListTag entries = registries.getListOrEmpty(key);

			var idList = new ArrayList<Identifier>();

			for (Tag entry : entries) {
				if (entry instanceof StringTag id) {
					idList.add(Identifier.parse(id.value()));
				}
			}

			map.put(Identifier.parse(key), idList);
		}

		return new RegistryCustomContentState(map);
	}

	private CompoundTag toNbt() {
		var tag = new CompoundTag();
		tag.putInt("format_version", 0);

		var registries = new CompoundTag();

		for (Map.Entry<Identifier, List<Identifier>> entry : this.entries.entrySet()) {
			var list = new ListTag();

			for (Identifier id : entry.getValue()) {
				list.add(StringTag.valueOf(id.toString()));
			}

			registries.put(entry.getKey().toShortString(), list);
		}

		tag.put("registries", registries);
		return tag;
	}

	public Missing validate(RegistryAccess.Frozen layer) {
		var map = new HashMap<Identifier, List<Identifier>>();
		var registries = new ArrayList<Identifier>();

		for (Map.Entry<Identifier, List<Identifier>> entry : this.entries.entrySet()) {
			Optional<Registry<Object>> registry = layer.lookup(ResourceKey.createRegistryKey(entry.getKey()));

			if (registry.isPresent()) {
				for (Identifier identifier : entry.getValue()) {
					if (!registry.get().containsKey(identifier)) {
						map.computeIfAbsent(entry.getKey(), _ -> new ArrayList<>()).add(identifier);
					}
				}
			} else {
				registries.add(entry.getKey());
			}
		}

		return new Missing(map, registries);
	}

	public record Missing(Map<Identifier, List<Identifier>> entries, List<Identifier> registries) {
		public static final Missing NONE = new Missing(Map.of(), List.of());

		public Details asDetails() {
			var list = new ArrayList<Details.Section>();

			for (Map.Entry<Identifier, List<Identifier>> entry : this.entries.entrySet()) {
				var m = new HashMap<String, List<String>>();
				var body = new ArrayList<Component>();

				for (Identifier id : entry.getValue()) {
					m.computeIfAbsent(id.getNamespace(), _ -> new ArrayList<>()).add(id.getPath());
				}

				for (Map.Entry<String, List<String>> id : m.entrySet()) {
					var group = new ArrayList<Component>();
					group.add(Component.translatable("fabric-registry-sync-v0.missing-entries.details.missing_for_namespace", id.getValue().size(), id.getKey()));

					for (String path : id.getValue()) {
						group.add(Component.translatable("fabric-registry-sync-v0.missing-entries.details.missing_for_namespace.entry", path).withColor(TextColor.GRAY));
					}

					body.add(ComponentUtils.formatList(group, CommonComponents.NEW_LINE));
				}

				list.add(new Details.Section(
						Component.translatable("fabric-registry-sync-v0.missing-entries.details.missing_registry_entry", entry.getKey().toString(), entry.getValue().size()).withColor(TextColor.YELLOW),
						body
				));
			}

			if (!this.registries.isEmpty()) {
				var body = new ArrayList<Component>();

				for (Identifier registry : this.registries) {
					body.add(Component.translatable("fabric-registry-sync-v0.missing-entries.details.missing_registry.entry", registry.toString()).withColor(TextColor.GRAY));
				}

				list.add(new Details.Section(
						Component.translatable("fabric-registry-sync-v0.missing-entries.details.missing_registry", this.registries.size()).withColor(TextColor.YELLOW),
						List.of(ComponentUtils.formatList(body, CommonComponents.NEW_LINE))
				));
			}

			return new Details(list);
		}

		public boolean isEmpty() {
			return this.entries.isEmpty() && this.registries.isEmpty();
		}
	}
}
