# SkyBlock Farming Helper (Minecraft 26.1.2)

Initial client-side prototype based on the Fabric template.

## Current prototype
- Counts positive inventory changes for a small list of farming drops.
- Displays harvested item count, estimated session value, and estimated coins/hour.
- Runs locally on the client; it does not automate gameplay or send commands.

## Important limitations
- This is an early prototype, not a verified release build.
- Profit uses placeholder item prices in `SkyblockFarmingHelperClient.java`; these are not live Bazaar prices.
- Inventory-delta detection can miss harvests when inventory is full, items are compacted/sold, or other inventory changes occur.
- HUD rendering and Minecraft 26.1.2 API compatibility must be checked by building against the generated template dependencies.

## Build
Use Java 25, open the folder in IntelliJ IDEA, allow Gradle to sync, then run `gradlew build` (Windows: `gradlew.bat build`). The remapped jar should appear in `build/libs/`.
