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

package net.fabricmc.fabric.impl.resource.conditions;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.SharedConstants;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.metadata.MetadataSectionType;
import net.minecraft.server.packs.metadata.pack.PackFormat;

import net.fabricmc.fabric.api.resource.conditions.v1.ResourceCondition;
import net.fabricmc.fabric.api.resource.conditions.v1.ResourceConditions;

public record OverlayConditionsMetadata(List<Entry> overlays) {
	public static final Codec<OverlayConditionsMetadata> CODEC = Entry.CODEC.listOf().fieldOf("entries").xmap(OverlayConditionsMetadata::new, OverlayConditionsMetadata::overlays).codec();
	public static final MetadataSectionType<OverlayConditionsMetadata> SERIALIZER = new MetadataSectionType<>(ResourceConditions.OVERLAYS_KEY, CODEC);

	public List<String> appliedOverlays(PackType type) {
		List<String> appliedOverlays = new ArrayList<>();
		PackFormat currentFormat = type != null
				? SharedConstants.getCurrentVersion().packVersion(type)
				: null;

		for (Entry entry : this.overlays()) {
			if (!entry.condition().test(null)) {
				continue;
			}

			if (currentFormat != null && !entry.isFormatInRange(currentFormat)) {
				continue;
			}

			appliedOverlays.add(entry.directory());
		}

		return appliedOverlays;
	}

	public record Entry(String directory, ResourceCondition condition, Optional<Integer> minFormat, Optional<Integer> maxFormat) {
		public static final Codec<Entry> CODEC = RecordCodecBuilder.create(instance -> instance.group(
				Codec.STRING.validate(Entry::validateDirectory).fieldOf("directory").forGetter(Entry::directory),
				ResourceCondition.CODEC.fieldOf("condition").forGetter(Entry::condition),
				Codec.INT.optionalFieldOf("min_format").forGetter(Entry::minFormat),
				Codec.INT.optionalFieldOf("max_format").forGetter(Entry::maxFormat)
		).apply(instance, Entry::new));
		private static final Pattern DIRECTORY_NAME_PATTERN = Pattern.compile("[-_a-zA-Z0-9.]+");

		public Entry(String directory, ResourceCondition condition) {
			this(directory, condition, Optional.empty(), Optional.empty());
		}

		public boolean isFormatInRange(PackFormat currentFormat) {
			// Compare via PackFormat to reuse vanilla's ordering logic.
			if (this.minFormat.isPresent()) {
				PackFormat min = new PackFormat(this.minFormat.get(), 0);

				if (currentFormat.compareTo(min) < 0) {
					return false;
				}
			}

			if (this.maxFormat.isPresent()) {
				PackFormat max = new PackFormat(this.maxFormat.get(), 0);

				if (currentFormat.compareTo(max) > 0) {
					return false;
				}
			}

			return true;
		}

		private static DataResult<String> validateDirectory(String directory) {
			boolean valid = DIRECTORY_NAME_PATTERN.matcher(directory).matches();
			return valid ? DataResult.success(directory) : DataResult.error(() -> "Directory name is invalid");
		}
	}
}
