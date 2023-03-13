package org.quiltmc.wiki

import net.minecraft.item.Item
import net.minecraft.registry.Registry
import org.quiltmc.loader.api.ModContainer
import org.quiltmc.qkl.library.registry.registryScope
import org.quiltmc.qkl.library.items.itemSettingsOf
import org.quiltmc.qsl.base.api.entrypoint.ModInitializer

object TutorialMod: ModInitializer {
    //@start declare_item
//    val EXAMPLE_ITEM: Item = Item(itemSettingsOf())
    //@end declare_item

    override fun onInitialize(mod: ModContainer) {
        //@start register_item
//        registryScope(mod.metadata().id()) {
//            EXAMPLE_ITEM withPath "example_item" toRegistry Registry.ITEM
//        }
        //@end register_item
    }
}
