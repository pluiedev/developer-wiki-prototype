package org.quiltmc.wiki;

import net.minecraft.item.Item;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;
import net.minecraft.registry.Registry;
import org.quiltmc.loader.api.ModContainer;
import org.quiltmc.qsl.base.api.entrypoint.ModInitializer;
import org.quiltmc.qsl.item.setting.api.QuiltItemSettings;

public class TutorialMod implements ModInitializer {
	//@start declare_item
	public static final Item EXAMPLE_ITEM = new Item(new QuiltItemSettings());
	//@end declare_item

	@Override
	public void onInitialize(ModContainer mod) {
		//@start register_item
		Registry.register(Registries.ITEM, new Identifier(mod.metadata().id(), "example_item"), EXAMPLE_ITEM);
		//@end register_item
	}
}
