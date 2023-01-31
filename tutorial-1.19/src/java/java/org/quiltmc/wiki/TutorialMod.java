package org.quiltmc.wiki;

import net.minecraft.item.Item;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;
import net.minecraft.registry.Registry;
import org.quiltmc.loader.api.ModContainer;
import org.quiltmc.qsl.base.api.entrypoint.ModInitializer;
import org.quiltmc.qsl.item.setting.api.QuiltItemSettings;

public class TutorialMod implements ModInitializer {
	// @start Declaration
	public static final Item EXAMPLE_ITEM = new Item(new QuiltItemSettings());
	// @end Declaration

	@Override
	public void onInitialize(ModContainer mod) {
		// @start Registration
		Registry.register(Registries.ITEM, new Identifier(mod.metadata().id(), "example_item"), EXAMPLE_ITEM);
		// @end Registration
	}
}
