Blockbench related stuff
========================

**__Read this if you intend to modify the scooter model.__**

Blockbench likes to use absolute paths for textures when saving `bbmodel` files, which not only presents a security risk, but also may make someone else's Blockbench freak out if the files aren't where they're supposed to be, though it also saves a relative path.

Additionally, Blockbench saves the image file within the `bbmodel`, *just in case* it isn't actually on any of the paths, however, the game needs image files to display a model with a texture. The texture will be always be present in this codebase.

With that being said, take these steps after editing the scooter's `bbmodel` file:

 - Remove the absolute paths from the textures, better not to take risks.
 - Remove the base64d picture data, we already have it at home.

Exporting
---------

**Export at your own risk.** Blockbench doesn't have support for Yarn mappings, thus the only mappings available are MCP and Mojmaps, the latter one is used.

If you do export, `.java` files are ignored in `/src/blockbench/`.
These classes are not drop-in replacements for `ScooterEntityModel.java`, and thus are to be used as reference only.

If you need to change something on `ScooterEntityModel.java`, translate from Mojmaps to Yarn.
