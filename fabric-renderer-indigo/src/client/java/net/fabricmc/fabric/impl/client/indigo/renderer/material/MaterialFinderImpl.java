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

package net.fabricmc.fabric.impl.client.indigo.renderer.material;

import java.util.Objects;

import net.fabricmc.fabric.api.renderer.v1.material.BlendMode;
import net.fabricmc.fabric.api.renderer.v1.material.GlintMode;
import net.fabricmc.fabric.api.renderer.v1.material.MaterialFinder;
import net.fabricmc.fabric.api.renderer.v1.material.MaterialView;
import net.fabricmc.fabric.api.renderer.v1.material.RenderMaterial;
import net.fabricmc.fabric.api.renderer.v1.material.ShadeMode;
import net.fabricmc.fabric.api.util.TriState;

public class MaterialFinderImpl extends MaterialViewImpl implements MaterialFinder {
	private static final int DEFAULT_BITS;

	static {
		// Start with all zeroes
		MaterialFinderImpl finder = new MaterialFinderImpl(0);
		// Apply non-zero defaults
		finder.ambientOcclusion(TriState.DEFAULT);
		DEFAULT_BITS = finder.bits;

		if (!areBitsValid(DEFAULT_BITS)) {
			throw new AssertionError("Default MaterialFinder bits are not valid!");
		}
	}

	protected MaterialFinderImpl(int bits) {
		super(bits);
	}

	public MaterialFinderImpl() {
		this(DEFAULT_BITS);
	}

	@Override
	public MaterialFinder blendMode(BlendMode blendMode) {
		Objects.requireNonNull(blendMode, "BlendMode may not be null");

		bits = (bits & ~BLEND_MODE_MASK) | (blendMode.ordinal() << BLEND_MODE_BIT_OFFSET);
		return this;
	}

	@Override
	public MaterialFinder emissive(boolean isEmissive) {
		bits = isEmissive ? (bits | EMISSIVE_FLAG) : (bits & ~EMISSIVE_FLAG);
		return this;
	}

	@Override
	public MaterialFinder disableDiffuse(boolean disable) {
		bits = disable ? (bits | DIFFUSE_FLAG) : (bits & ~DIFFUSE_FLAG);
		return this;
	}

	@Override
	public MaterialFinder ambientOcclusion(TriState mode) {
		Objects.requireNonNull(mode, "ambient occlusion TriState may not be null");

		bits = (bits & ~AO_MASK) | (mode.ordinal() << AO_BIT_OFFSET);
		return this;
	}

	@Override
	public MaterialFinder glintMode(GlintMode mode) {
		Objects.requireNonNull(mode, "GlintMode may not be null");

		bits = (bits & ~GLINT_MODE_MASK) | (mode.ordinal() << GLINT_MODE_BIT_OFFSET);
		return this;
	}

	@Override
	public MaterialFinder shadeMode(ShadeMode mode) {
		Objects.requireNonNull(mode, "ShadeMode may not be null");

		bits = (bits & ~SHADE_MODE_MASK) | (mode.ordinal() << SHADE_MODE_BIT_OFFSET);
		return this;
	}

	@Override
	public MaterialFinder copyFrom(MaterialView material) {
		bits = ((MaterialViewImpl) material).bits;
		return this;
	}

	@Override
	public MaterialFinder clear() {
		bits = DEFAULT_BITS;
		return this;
	}

	@Override
	public RenderMaterial find() {
		return RenderMaterialImpl.byIndex(bits);
	}

	@Override
	public String toString() {
		return "MaterialFinderImpl{" + contentsToString() + "}";
	}
}
