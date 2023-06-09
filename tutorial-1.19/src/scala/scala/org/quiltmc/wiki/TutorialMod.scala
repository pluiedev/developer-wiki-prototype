package org.quiltmc.wiki

import net.minecraft.item.Item
import net.minecraft.util.Identifier
import net.minecraft.registry.{Registries, Registry}
import org.quiltmc.loader.api.ModContainer
import org.quiltmc.qsl.base.api.entrypoint.ModInitializer
import org.quiltmc.qsl.item.setting.api.QuiltItemSettings

// Can be made an object with a language adapter for scala
class TutorialMod extends ModInitializer:
  override def onInitialize(mod: ModContainer): Unit =
    //@start register_item
    Registry.register(Registries.ITEM, Identifier(mod.metadata.id, "example_item"), TutorialMod.EXAMPLE_ITEM)
    //@end register_item

object TutorialMod:
  //@start declare_item
  final val EXAMPLE_ITEM: Item = Item(QuiltItemSettings())
  //@end declare_item

