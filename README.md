# NBT Tools (custom fork)

This is a personal fork of **[NBT Tools](https://github.com/TheTypholorian/nbt_tools)** by **The Typhothanian** (TheTypholorian). All credit for the original mod goes to them.

The original mod declares the **MIT License** in its `fabric.mod.json`, and this fork keeps that. It was made for personal use and is not an official release.

## Branches
- `26.3`: Minecraft 26.3, Fabric only, built on upstream `master`.

## What this fork changes
- Builds with a plain Fabric Loom + Kotlin build instead of the upstream Stonecutter setup, which has no 26.3 support. The sources are the same, with the version switches resolved to their 26.x side.
- Ctrl+C copy is ported to 26.3 and copies the hovered slot.
