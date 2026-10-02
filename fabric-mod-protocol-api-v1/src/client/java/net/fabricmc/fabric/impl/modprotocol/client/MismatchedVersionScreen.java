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

package net.fabricmc.fabric.impl.modprotocol.client;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.FocusableTextWidget;
import net.minecraft.client.gui.components.ScrollableLayout;
import net.minecraft.client.gui.layouts.HeaderAndFooterLayout;
import net.minecraft.client.gui.layouts.LinearLayout;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.Identifier;

import net.fabricmc.fabric.impl.modprotocol.ModProtocolManager;

public class MismatchedVersionScreen extends Screen {
	private static final Identifier SCREEN_BACKGROUND = Identifier.withDefaultNamespace("textures/gui/inworld_menu_list_background.png");
	private static final Component TITLE = Component.translatable("text.fabric-mod-protocol-v1.mismatched_versions.title");

	private final Screen lastScreen;
	private final ModProtocolManager.ValidationResult validationResult;
	private HeaderAndFooterLayout layout;

	public MismatchedVersionScreen(ModProtocolManager.ValidationResult validationResult, Screen lastScreen) {
		super(TITLE);
		this.validationResult = validationResult;
		this.lastScreen = lastScreen;
	}

	@Override
	public void resize(int width, int height) {
		super.resize(width, height);
		this.rebuildWidgets();
	}

	protected void init() {
		this.layout = new HeaderAndFooterLayout(this);
		this.addTitle();
		this.addContents();
		this.addFooter();
		this.layout.visitWidgets(this::addRenderableWidget);
		this.repositionElements();
	}

	protected void addTitle() {
		this.layout.addTitleHeader(this.title, this.font);
	}

	protected void addContents() {
		LinearLayout body = LinearLayout.vertical().spacing(4);

		MutableComponent component = Component.empty();
		ModProtocolManager.appendComponentEntries(this.validationResult.missing(), ModProtocolManager.LOCAL_MOD_PROTOCOLS_BY_ID, Integer.MAX_VALUE, component::append, this.validationResult.forceNoRemote());

		body.addChild(FocusableTextWidget.builder(component, font, 4).maxWidth(this.width - 40).alwaysShowBorder(false)
				.backgroundFill(FocusableTextWidget.BackgroundFill.NEVER).build().setCentered(true));

		var scrollable = new ScrollableLayout(minecraft, body, this.layout.getContentHeight());
		body.arrangeElements();
		this.layout.addToContents(scrollable);
	}

	protected void addFooter() {
		this.layout.addToFooter(Button.builder(CommonComponents.GUI_BACK, (_) -> this.onClose()).width(200).build());
	}

	@Override
	public void extractBackground(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float a) {
		super.extractBackground(graphics, mouseX, mouseY, a);

		graphics.blit(
				RenderPipelines.GUI_TEXTURED,
				SCREEN_BACKGROUND,
				0,
				this.layout.getHeaderHeight(),
				this.width, 0,
				width,
				this.layout.getContentHeight(),
				32,
				32
		);

		graphics.blit(RenderPipelines.GUI_TEXTURED, Screen.HEADER_SEPARATOR, 0, this.layout.getHeaderHeight() - 2, 0.0F, 0.0F, width, 2, 32, 2);
		graphics.blit(RenderPipelines.GUI_TEXTURED, Screen.FOOTER_SEPARATOR, 0, this.layout.getHeaderHeight() + this.layout.getContentHeight(), 0.0F, 0.0F, width, 2, 32, 2);
	}

	protected void repositionElements() {
		this.layout.arrangeElements();
	}

	@Override
	public void onClose() {
		this.minecraft.gui.setScreen(this.lastScreen);
	}
}
