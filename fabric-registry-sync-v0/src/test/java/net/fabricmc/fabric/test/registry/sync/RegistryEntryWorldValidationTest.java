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

package net.fabricmc.fabric.test.registry.sync;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import net.minecraft.SharedConstants;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.NbtAccounter;
import net.minecraft.nbt.NbtIo;
import net.minecraft.references.BlockItemIds;
import net.minecraft.resources.Identifier;
import net.minecraft.server.Bootstrap;

import net.fabricmc.fabric.impl.registry.sync.validate.RegistryCustomContentState;

public class RegistryEntryWorldValidationTest {
	@BeforeAll
	static void beforeAll() {
		SharedConstants.tryDetectVersion();
		Bootstrap.bootStrap();
	}

	@Test
	void emptyAlwaysPasses() {
		RegistryAccess access = RegistryAccess.fromRegistryOfRegistries(BuiltInRegistries.REGISTRY);

		RegistryCustomContentState.Missing missing = RegistryCustomContentState.EMPTY.validate(access);
		assertTrue(missing.isEmpty());
	}

	@Test
	void missingEntriesTest() {
		RegistryAccess access = RegistryAccess.fromRegistryOfRegistries(BuiltInRegistries.REGISTRY);

		Identifier fakeRegistry = Identifier.fromNamespaceAndPath("fabric", "fake_registry");
		Identifier blocksRegistry = Registries.BLOCK.identifier();

		Identifier fakeEntry1 = Identifier.fromNamespaceAndPath("fabric", "fake_entry_1");
		Identifier fakeEntry2 = Identifier.fromNamespaceAndPath("fabric", "fake_entry_2");
		Identifier fakeEntry3 = Identifier.fromNamespaceAndPath("fabric", "fake_entry_3");
		Identifier grassEntry = BlockItemIds.GRASS_BLOCK.block().identifier();
		Identifier dirtEntry = BlockItemIds.DIRT.block().identifier();

		RegistryCustomContentState state = new RegistryCustomContentState(Map.of(
				fakeRegistry, List.of(fakeEntry1, fakeEntry2, fakeEntry3), // 3 entries, fake registry
				blocksRegistry, List.of(fakeEntry1, grassEntry, fakeEntry2, dirtEntry, fakeEntry3) // 5 entries, vanilla registry
		), RegistryCustomContentState.Status.VALID);

		RegistryCustomContentState.Missing missing = state.validate(access);

		assertFalse(missing.isEmpty());
		assertNull(missing.entries().get(fakeRegistry));
		assertTrue(missing.registries().contains(fakeRegistry));

		List<Identifier> missingBlocks = missing.entries().get(blocksRegistry);

		assertNotNull(missingBlocks);
		assertFalse(missing.registries().contains(blocksRegistry));
		assertEquals(3, missing.entries().get(blocksRegistry).size());
		assertTrue(missingBlocks.contains(fakeEntry1));
		assertTrue(missingBlocks.contains(fakeEntry2));
		assertTrue(missingBlocks.contains(fakeEntry3));
		assertFalse(missingBlocks.contains(grassEntry));
		assertFalse(missingBlocks.contains(dirtEntry));
	}

	@Test
	void fileReadingTest() {
		RegistryCustomContentState state;

		try (InputStream stream = RegistryEntryWorldValidationTest.class.getResourceAsStream("/world_validation/reference_registry_entries.dat")) {
			assertNotNull(stream);
			state = RegistryCustomContentState.fromNbt(NbtIo.readCompressed(stream, NbtAccounter.unlimitedHeap()));
		} catch (IOException e) {
			state = new RegistryCustomContentState(Map.of(), RegistryCustomContentState.Status.INVALID_FILE);
		}

		assertEquals(RegistryCustomContentState.Status.VALID, state.status());
		assertNotEquals(0, state.entries().size());
	}
}
