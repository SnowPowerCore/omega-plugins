# omega-item-loader-plugin

Loads ItemStacks from a human-readable JSON file into a service other plugins can consume.

## Build

- `./gradlew :plugin:build`
- Output jar: `plugin/build/libs/OmegaItemLoader-0.1.0.jar` (shadowed)

## Runtime

- Drop the jar into your server `plugins/` folder.
- On first start it will create `plugins/OmegaItemLoader/items.json`.
- For a larger example you can copy from, see `sample-items.json` in this repo.
- Each item may also specify an optional `referenceId` string; when present, other plugins can request the item from the `ItemRegistry` using that `referenceId`.
	- `customId` is still accepted as a legacy alias for `referenceId`.
- Set `additionalInfo.glow: true` to force the enchantment glint effect even if the item has no real enchantments.
	- Requires ProtocolLib (soft dependency). When ProtocolLib is present, the glow is applied client-side in packets so the server-side item stays unenchanted.
