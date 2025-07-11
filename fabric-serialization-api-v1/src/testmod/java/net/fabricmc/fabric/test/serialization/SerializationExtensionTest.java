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

package net.fabricmc.fabric.test.serialization;

import java.util.Arrays;
import java.util.Set;
import java.util.stream.Stream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtHelper;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.storage.NbtReadView;
import net.minecraft.storage.NbtWriteView;
import net.minecraft.storage.ReadView;
import net.minecraft.storage.WriteView;
import net.minecraft.util.ErrorReporter;

import net.fabricmc.api.ModInitializer;

public class SerializationExtensionTest implements ModInitializer {
	private static final Logger LOGGER = LoggerFactory.getLogger(SerializationExtensionTest.class);

	private static final String BYTES_KEY = "bytes";
	private static final byte[] BYTES_DATA = new byte[] {0, 1, 2, 3, 4, 5, 6, 7, 8, Byte.MAX_VALUE};
	private static final String LONG_KEY = "longs";
	private static final long[] LONG_DATA = new long[] { 0, 1, 2, 3, 4, 5, 6, 7, 8, Long.MAX_VALUE };
	@Override
	public void onInitialize() {
		NbtWriteView directWrite = NbtWriteView.create(ErrorReporter.EMPTY);
		NbtWriteView defaultedWrite = NbtWriteView.create(ErrorReporter.EMPTY);

		writeData(new DelegateWriteView(defaultedWrite));
		writeData(directWrite);

		if (directWrite.getNbt().equals(defaultedWrite.getNbt())) {
			LOGGER.info("Written NBT data is equal!");
			LOGGER.info("Data:");
			LOGGER.info(NbtHelper.toFormattedString(directWrite.getNbt(), true));
		} else {
			LOGGER.error("Written NBT data is not equal!");
			LOGGER.info("Direct write:");
			LOGGER.info(NbtHelper.toFormattedString(directWrite.getNbt(), true));
			LOGGER.info("Defaulted write:");
			LOGGER.info(NbtHelper.toFormattedString(defaultedWrite.getNbt(), true));
			throw new IllegalStateException("Failed to write equal data!");
		}

		ReadView directRead = NbtReadView.create(ErrorReporter.EMPTY, RegistryWrapper.WrapperLookup.of(Stream.empty()), directWrite.getNbt());
		ReadView defaultedRead = new DelegateReadView(directRead);
		readCheck(directWrite.getNbt(), directRead, "Direct read");
		readCheck(directWrite.getNbt(), defaultedRead, "Defaulted read");
	}

	private void writeData(WriteView view) {
		view.putByteArray(BYTES_KEY, BYTES_DATA);
		view.putLongArray(LONG_KEY, LONG_DATA);

		for (int i = 0; i < 8; i++) {
			view.putInt("key_" + i, i);
		}
	}

	private void readCheck(NbtCompound compound, ReadView view, String type) {
		if (!Arrays.equals(view.getOptionalByteArray(BYTES_KEY).orElse(new byte[0]), BYTES_DATA)) {
			LOGGER.error("Read NBT data doesn't match key {}!", BYTES_KEY);
			LOGGER.info(type + ":");
		}

		if (!Arrays.equals(view.getOptionalLongArray(LONG_KEY).orElse(new long[0]), LONG_DATA)) {
			LOGGER.error("Read NBT data doesn't match key {}!", LONG_KEY);
			LOGGER.info(type + ":");
		}

		if (view.contains("non_existing")) {
			LOGGER.error("Read NBT data wrongly returns contains check for non existing entry!");
			LOGGER.info(type + ":");
		}

		if (!view.contains("key_3")) {
			LOGGER.error("Read NBT data wrongly returns contains check for existing entry!");
			LOGGER.info(type + ":");
		}

		LOGGER.info(String.join(", ", view.keys()));

		if (!Set.copyOf(view.keys()).equals(compound.getKeys())) {
			LOGGER.error("Read NBT data returns wrong keys!");
			LOGGER.info(type + ":");
		}
	}
}
