# Your First Item

Items are crucial to Minecraft, and almost any mod will make use of them.
This tutorial will go through the basic steps for creating an item.

## Registering the Item

The first thing we need to do is register the item so that the game knows to add it in.
First, we need to declare an instance of `net.minecraft.item.Item` with the parameters for our item.

In theory, we could do this directly in the registration line but having a separate variable allows us to reference it elsewhere for other purposes.

=== declare_item
=+= src/$/$/org/quiltmc/wiki/TutorialMod.@

=-= java
    Here, the `public static final` ensures that we can access the item elsewhere but not change the contents of the variable itself,
    making sure that we don't accidentally alter it somewhere else.
=-=
=/=

Our new instance of `Item` takes in an instance of `QuiltItemSettings` as an argument.
This is where we declare all the settings for our item.
There are a variety of these, but we'll touch on them later.

Now that we've declared the item, we need to tell the game registry to put it into the game.
We do so by putting this into the mod's `onInitialize` method:

=== register_item
=+= src/$/$/org/quiltmc/wiki/TutorialMod.@

=-= java scala
    `Registry.register()` takes three parameters:

    - The `Registry` we want to add to. For items this is always `Registries.ITEM`.
    - The `Identifier` used for the item. This must be unique. The first part is the namespace (which should be the mod id, but here it is `simple_item`) and the item name itself. Only lowercase letters, numbers, underscores, dashes, periods, and slashes are allowed.
    - The `Item` to register. Here, we pass in the item we declared earlier.

=-= kotlin
    Here we use the `registryScope` method from Quilt Kotlin Libraries to register our items.
    It takes an argument, the namespace we want to register our items under, and a block where you declare the items you want to register.
    Normally, the namespace should just be our mod ID, which we can get via `mod.metadata().id()`.
    
    In the block, we use the methods `withPath` and `toRegistry` to register our new item.
    `withPath` takes a registrable object (in our case, our item), and combines it with a path, which is `"example_item"`.
    This creates an registrable item called `tutorial:example_item`, which we then register.
    
    Objects in Minecraft are added to "registries", and for items, the correct registry to use is always `Registries.ITEM`.
    So for our item, we then call `toRegistry` to register our item to the `Registries.ITEM` registry.
=/=

Having done all of this, if we load a world with cheats enabled, and run the command `/give @s tutorial:example_item`, we can see that our item appears in our hand!
But it doesn't have a texture, and its name isn't translated properly. How do we fix this?

## Textures

First we need to declare the model for the item. This tells the game how to render the item.

===
=+= src/main/resources/assets/tutorial/models/item/example_item.json json
=/=

For most items, all you need to do here is replace `tutorial` with your mod ID and `example_item` with the item name you set earlier.
This file should go to your assets folder under `/models/item`.

The texture file, as shown in the model, should match the path specified in the `Identifier`, so in our case `textures/item/example_item.png`.

## Language Translation

Finally, we need to add a translation. Put this in `lang/en_us.json` in your assets folder, replacing the same values as before:

===
=+= src/main/resources/assets/tutorial/lang/en_us.json json
=/=

And that's it! Your item should be fully working.


## What's next?

This tutorial only covers the most basic of items. Check the other item tutorials for more advanced items.

If you want your item to have a recipe, generate one from [destruc7i0n's crafting recipe generator](https://crafting.thedestruc7i0n.ca/) (you may want to use a placeholder for the `output` item and then replace it with e.g. `tutorial:example_item`) and then put it in a JSON file under `src/main/resources/data/tutorial/recipes/` (replacing `tutorial` with your mod ID). Further details on item recipes can be found <abbr title="This documentation is not done yet, but it will be soon!">here</abbr>.
