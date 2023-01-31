package org.quiltmc.wiki

import net.minecraft.item.Item
import net.minecraft.registry.Registry
import org.quiltmc.loader.api.ModContainer
import org.quiltmc.qkl.library.registry.registryScope
import org.quiltmc.qkl.library.items.itemSettingsOf
import org.quiltmc.qsl.base.api.entrypoint.ModInitializer

object TutorialMod: ModInitializer {
    // @start Declaration
//    val EXAMPLE_ITEM: Item = Item(itemSettingsOf())
    // @end Declaration

    override fun onInitialize(mod: ModContainer) {
        // @start Registration
//        registryScope(mod.metadata().id()) {
//            EXAMPLE_ITEM withPath "example_item" toRegistry Registry.ITEM
//        }
        // @end Registration
    }
}
