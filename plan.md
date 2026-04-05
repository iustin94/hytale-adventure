Context
Research task to understand how the built-in adventure sub-module works in the Hytale server codebase. The adventure module provides the RPG gameplay loop through 15 plugin sub-modules. No code changes — this is a documentation/exploration effort that will produce a comprehensive markdown overview committed to the repo branch.

Module Architecture
Location: /com/hypixel/hytale/builtin/adventure/ (228 Java files, 15 plugins)
All plugins extend PluginBase → JavaPlugin and follow lifecycle: setup() → start() → ENABLED → shutdown(). Uses ECS pattern (Components, Systems, Resources).
Dependency Tiers
Tier 1 — Foundation (no cross-adventure deps):

objectives/ (87 files) — Core quest framework with task types, completions, markers, history
reputation/ (12 files) — Reputation ranks/groups, per-player or per-world storage
shop/ (16 files) — Trading system with barter support
farming/ (24 files) — Crop growth, soil, watering, fertilizing, coops
memories/ (22 files) — Collectible system, temple respawn
teleporter/ (10 files) — Warp point blocks and fast travel
stash/ (2 files) — Container/banking
camera/ (13 files) — Camera effects/shakes
worldlocationcondition/ (2 files) — Location-based triggers

Tier 2 — NPC Bridge (depend on Tier 1 + NPCPlugin):
10. npcobjectives/ (22 files) — Kill tasks, bounties, NPC behavior tree nodes
11. npcreputation/ (3 files) — NPC attitude from reputation
12. npcshop/ (7 files) — NPC shop/barter behavior nodes
Tier 3 — Cross-Domain Bridge (depend on 2+ Tier 1 modules):
13. objectivereputation/ (4 files) — Reputation as quest reward
14. objectiveshop/ (3 files) — Quest-gated shop elements
15. shopreputation/ (1 file) — Asset load ordering for rep-based shops
Gameplay Loop Flow

Player enters → ObjectivePlayerSetupSystem loads quest history, MemoriesPlugin initializes, camera starts
Exploration → Location triggers, NPC memory collection, teleporter discovery, stash loot population
Quest engagement → NPC behavior tree starts objectives, tasks include kill/gather/craft/reach/treasure
Reputation effects → Quest completion → objectivereputation/ changes rep → npcreputation/ updates NPC attitudes
Commerce → Base shops + NPC shops + reputation-gated pricing + quest prerequisites
Farming — Independent crop growth/harvest subsystem
Persistence → Auto-save every 5 min + shutdown save for objectives, memories, barter state, warps


Objective System - Complexity & Depth
Quest Structure (3 levels of nesting)

ObjectiveLine — A chain of objectives played sequentially, with branching via nextObjectiveLineIds[] (multiple possible follow-up lines)
Objective — Contains ordered TaskSets (phases), plus completion handlers for rewards
TaskSet — Contains multiple tasks with AND logic (all must complete before advancing to next TaskSet)

This gives arbitrarily deep quest chains with branching paths at the line level and multi-phase objectives within each quest.
9 Task Types (Built-in)
TypeCodec NameTrackingKey ParametersGather"Gather"Count items in inventoryblockTag or itemId, countCraft"Craft"Count PlayerCraftEventsitemId, countUseBlock"UseBlock"Count LivingEntityUseBlockEventsblockTag or itemId, countUseEntity"UseEntity"Unique NPC UUIDs interacted withtaskId, count, animationIdToPlay, dialogOptionsReachLocation"ReachLocation"Binary (reached or not)targetLocationId → ReachLocationMarkerAssetTreasureMap"TreasureMap"Count chests openedchestConfigs[] with radius, droplist, location conditionsKill"Kill"Count kills by NPC groupnpcGroupId, count (via npcobjectives)KillSpawnMarker"KillSpawnMarker"Count kills + spawn markersnpcGroupId, count, spawnMarkerIds[], radiusBounty"Bounty"Binary (target killed)npcId, worldLocationCondition (spawns a specific NPC)
Condition System (2 layers)
Trigger Conditions (gate when objectives can start at location markers):

HourRangeTriggerCondition — Time-of-day gating (minHour/maxHour with wrap-around)
WeatherTriggerCondition — Weather-based gating (weatherIds array)

Task Conditions (gate individual task progress):

SoloInventoryCondition — Require item in inventory or held in hand, with optional consumption on completion (quantity, consumeOnCompletion, holdInHand)

Completion Handlers (reward pipeline)

GiveItemsCompletion — Award random items from ItemDropList, per-player reward tracking
ClearObjectiveItemsCompletion — Remove quest-starter items from inventory
ReputationCompletion — Award reputation (via objectivereputation bridge plugin)
Multiple handlers per objective (executed in sequence)

Multi-Player Support

Objectives track playerUUIDs (all enrolled) and activePlayerUUIDs (currently online)
Progress is shared across participants
Per-player history tracking with ObjectiveHistoryComponent storing timesCompleted, lastCompletionTimestamp, per-player rewards
Quests are replayable (timesCompleted increments)

Location & Marker System (3 area types + 3 location providers)
Area detection:

ObjectiveLocationAreaBox — Rectangular entry/exit zones
ObjectiveLocationAreaRadius — Circular radius-based zones
ObjectiveLocationMarker — Entity component with area, trigger conditions, environment filtering

World Location Providers (for dynamic placement like treasure chests):

LookBlocksBelowProvider — Scan downward for valid blocks
LocationRadiusProvider — Fixed radius from center
CheckTagWorldHeightRadiusProvider — Height-based radius with block tag validation

Transaction System (reversible world changes)
Tasks create TransactionRecord objects that can be reverted on failure:

SpawnEntityTransactionRecord, SpawnTreasureChestTransactionRecord, RegistrationTransactionRecord, KillTaskTransaction
All-or-nothing: if any transaction fails during setup, all prior ones revert

Persistence & Hot-Reload

Full codec-based serialization of objective state
Auto-save every 5 minutes + shutdown save
reloadObjectiveAsset() supports hot-swapping task assets while quests are active
Per-world starter objectives configurable via ObjectiveGameplayConfig.starterObjectiveLinePerWorld

Admin Commands

/objective start, /objective complete, /objective panel, /objective history
/objective locationmarker, /objective reachlocation

UseEntity Animation & Dialog Detail

Animation: AnimationIdToPlay (optional String) on UseEntityObjectiveTaskAsset — consumed by NPC behavior tree to play animation on the NPC entity when interaction occurs. Handled by the NPC/entity animation pipeline, not the objectives plugin.
Dialog: DialogOptions with EntityNameKey + DialogKey (localization keys) on asset. Triggered in UseEntityObjectiveTask.increaseTaskCompletion() when task completes (all unique NPCs visited):

Sends chat message: "{NPC Name}: {Dialog Text}"
Opens DialogPage custom UI (Pages/DialogPage.ui) showing entity name and dialog text
UI has a close button; lifetime is CanDismissOrCloseThroughInteraction


Key files: UseEntityObjectiveTaskAsset.java (config + DialogOptions inner class), UseEntityObjectiveTask.java:79-90 (trigger), DialogPage.java (UI rendering)

Example: Setting Up a Quest Line with NPC, Animation, Dialog, and Objectives
The overview document will include a fully worked example demonstrating a quest line where a player talks to an NPC herbalist, gathers items, returns to the NPC with dialog/animation, and receives rewards. This involves three asset definitions:
1. ObjectiveLineAsset — defines the quest chain:
json{
  "id": "questline_herbalism",
  "Category": "main_quest",
  "ObjectiveIds": ["objective_gather_herbs", "objective_deliver_herbs"],
  "NextObjectiveLineIds": ["questline_alchemy"],
  "TitleId": "questline.herbalism.title",
  "DescriptionId": "questline.herbalism.desc"
}
2. ObjectiveAsset (phase 1) — gather herbs with inventory condition:
json{
  "id": "objective_gather_herbs",
  "Category": "main_quest",
  "TaskSets": [
    {
      "Tasks": [
        {
          "Type": "Gather",
          "Count": 5,
          "BlockTagOrItemId": { "ItemId": "herb_moonflower" },
          "TaskConditions": [
            {
              "Type": "SoloInventory",
              "BlockTagOrItemId": { "ItemId": "herb_gathering_bag" },
              "Quantity": 1,
              "HoldInHand": false,
              "ConsumeOnCompletion": false
            }
          ],
          "MapMarkerPositions": [[100, 64, 200]]
        }
      ]
    }
  ],
  "Completions": []
}
3. ObjectiveAsset (phase 2) — return to NPC with animation + dialog on completion:
json{
  "id": "objective_deliver_herbs",
  "Category": "main_quest",
  "TaskSets": [
    {
      "Tasks": [
        {
          "Type": "UseEntity",
          "TaskId": "herbalist_delivery",
          "Count": 1,
          "AnimationIdToPlay": "herbalist_receive_herbs",
          "Dialog": {
            "EntityNameKey": "npc.herbalist.name",
            "DialogKey": "npc.herbalist.thanks_dialog"
          }
        }
      ]
    }
  ],
  "Completions": [
    { "Type": "GiveItems", "DropList": "herbalist_reward_items" },
    { "Type": "ClearObjectiveItems" }
  ]
}
4. NPC Behavior Tree — the herbalist NPC starts the quest via:
json{ "Type": "StartObjective", "Objective": "objective_gather_herbs" }
And completes the UseEntity task via:
json{ "Type": "CompleteTask", "PlayAnimation": true }
Flow: Player talks to NPC → StartObjective fires → Phase 1 (gather 5 herbs, needs bag in inventory) → Phase 2 (return to NPC, CompleteTask triggers animation herbalist_receive_herbs, dialog page opens with NPC name + text) → GiveItems awards loot → ClearObjectiveItems removes quest items → Quest line advances to questline_alchemy.
5. Translation/Dialog definitions — .lang files in Server/Languages/{language}/ within asset packs:
propertiesnpc.herbalist.name=Herbalist
quest.gather_herbs.dialog_thanks=Thank you for gathering those herbs! These moonflowers are exactly what I needed.
questline.herbalism.title=The Herbalist's Request
questline.herbalism.desc=Help the herbalist gather rare herbs
quest.gather_herbs.title=Gather Moonflowers
quest.gather_herbs.desc=Collect moonflowers for the herbalist
How dialog keys connect to NPCs:

.lang files are parsed by LangFileParser → loaded by I18nModule → sent to clients via UpdateTranslations packets
NPC role configs define NameTranslationKey (e.g., "npc.herbalist.name") via BuilderRole.java → applied as DisplayNameComponent by RoleBuilderSystem
Dialog keys in UseEntityObjectiveTaskAsset.DialogOptions (EntityNameKey + DialogKey) reference the same translation system
At runtime, Message.translation(key) wraps the key; the client resolves it against its translation map
The NPC doesn't define its own dialog — dialog is configured on the objective task asset, pointing into shared .lang files

Key files for the JSON field names (derived from KeyedCodec definitions):

ObjectiveAsset.java: Category, TaskSets, Completions, TitleId, DescriptionId, RemoveOnItemDrop
TaskSet.java: DescriptionId, Tasks
ObjectiveTaskAsset.java: Type, DescriptionId, TaskConditions, MapMarkerPositions
CountObjectiveTaskAsset.java: Count
UseEntityObjectiveTaskAsset.java: TaskId, AnimationIdToPlay, Dialog → EntityNameKey, DialogKey
GatherObjectiveTaskAsset.java: BlockTagOrItemId → BlockTag or ItemId
ObjectiveLineAsset.java: Category, ObjectiveIds, NextObjectiveLineIds, TitleId, DescriptionId
BuilderActionStartObjective.java: Objective (String)
BuilderActionCompleteTask.java: PlayAnimation (Boolean)

Extensibility

ObjectivePlugin.registerTask(name, assetClass, taskClass) — Add new task types
ObjectivePlugin.registerCompletion(name, assetClass, completionClass) — Add new reward types
Codec-based polymorphism allows any plugin to extend the system


Implementation Plan
Step 1: Read Plugin Infrastructure
Read the plugin lifecycle framework to understand how all modules boot.

/com/hypixel/hytale/server/core/plugin/PluginBase.java
/com/hypixel/hytale/server/core/plugin/JavaPlugin.java
/com/hypixel/hytale/server/core/Constants.java (lines 53-89 for CORE_PLUGINS)

Step 2: Read Tier 1 Foundation Plugin Entry Points
Read the *Plugin.java for each foundation module to understand what each registers:

objectives/ObjectivePlugin.java — the central hub (task/completion registration APIs)
reputation/ReputationPlugin.java
shop/ShopPlugin.java
farming/FarmingPlugin.java
memories/MemoriesPlugin.java
teleporter/TeleporterPlugin.java
stash/StashPlugin.java
camera/CameraPlugin.java
worldlocationcondition/WorldLocationConditionPlugin.java

Step 3: Read Core Data Models
Key asset/data classes that define the adventure content:

objectives/config/ObjectiveAsset.java, ObjectiveLineAsset.java
objectives/config/task/ObjectiveTaskAsset.java
objectives/ObjectiveDataStore.java, Objective.java
reputation/assets/ReputationGroup.java, ReputationRank.java
shop/ShopAsset.java, shop/barter/BarterShopAsset.java

Step 4: Read Tier 2 NPC Bridge Plugins

npcobjectives/NPCObjectivesPlugin.java + task implementations
npcreputation/NPCReputationPlugin.java + ReputationAttitudeSystem.java
npcshop/NPCShopPlugin.java

Step 5: Read Tier 3 Cross-Domain Bridges

objectivereputation/ObjectiveReputationPlugin.java + ReputationCompletion.java
objectiveshop/ObjectiveShopPlugin.java + StartObjectiveInteraction.java
shopreputation/ShopReputationPlugin.java

Step 6: Read NPC Support Infrastructure

/server/npc/role/support/WorldSupport.java
/server/npc/role/support/EntitySupport.java
/server/npc/role/support/CombatSupport.java
/server/npc/role/support/StateSupport.java

Step 7: Write Overview Document
Produce a comprehensive ADVENTURE_MODULE_OVERVIEW.md document in the repo root summarizing:

Module architecture and dependency graph (3-tier structure)
Each sub-module's purpose, key classes, and registrations
The gameplay loop and how modules interact
Objective system deep dive: all 9 task types with parameters, condition system, completion handlers, multi-player support, location/marker system, transaction system, persistence
Quest structure complexity: ObjectiveLine → Objective → TaskSet → Tasks (with branching)
Key extension patterns (task registration, NPC behavior tree, codec registry, asset load ordering)
NPC support infrastructure (WorldSupport, EntitySupport, CombatSupport, StateSupport)

Step 8: Commit and Push
Commit the overview document to branch claude/explore-adventure-module-puEbK and push.

Key Patterns to Document

Extension Point: ObjectivePlugin.registerTask() / registerCompletion() used by npcobjectives/ and objectivereputation/
NPC Behavior Tree: Plugins register via NPCPlugin.get().registerCoreComponentType()
Asset Load Ordering: Bridge plugins inject loadsAfter constraints for cross-module references
Codec Registry Extension: Polymorphic types extended by named subtypes across plugins
Singleton Access: static instance + get() accessor for cross-plugin calls

Verification

Read each plugin file to confirm registrations match documented behavior
Trace cross-module imports to verify dependency tiers
Ensure the overview document accurately reflects the codebase
