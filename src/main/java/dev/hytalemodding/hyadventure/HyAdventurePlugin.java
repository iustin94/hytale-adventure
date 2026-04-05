package dev.hytalemodding.hyadventure;

import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.server.core.plugin.JavaPlugin;
import com.hypixel.hytale.server.core.plugin.JavaPluginInit;
import dev.hytalemodding.hyadventure.api.AdventureHandler;
import dev.hytalemodding.hyadventure.api.AdventureSchemaProvider;
import dev.hytalemodding.hyadventure.services.*;
import dev.hytalemodding.hyadventure.util.ConfigManager;

import javax.annotation.Nonnull;
import java.nio.file.Path;

public class HyAdventurePlugin extends JavaPlugin {

    private static final HytaleLogger LOGGER = HytaleLogger.forEnclosingClass();
    private static HyAdventurePlugin instance;

    private ObjectiveService objectiveService;
    private ReputationService reputationService;
    private ShopService shopService;
    private FarmingService farmingService;
    private MemoriesService memoriesService;
    private CameraService cameraService;
    private TeleporterService teleporterService;
    private NPCObjectiveService npcObjectiveService;
    private StashService stashService;
    private ConfigManager configManager;
    private QuestAuthoringService authoringService;
    private QuestRegistrationService registrationService;
    private QuestRoleService questRoleService;
    private TranslationService translationService;
    private QuestTemplateService templateService;

    public HyAdventurePlugin(@Nonnull JavaPluginInit init) {
        super(init);
        instance = this;
    }

    @Override
    protected void setup() {
        this.objectiveService = new ObjectiveService();
        this.reputationService = new ReputationService();
        this.shopService = new ShopService();
        this.farmingService = new FarmingService();
        this.memoriesService = new MemoriesService();
        this.cameraService = new CameraService();
        this.teleporterService = new TeleporterService();
        this.npcObjectiveService = new NPCObjectiveService();
        this.stashService = new StashService();

        // Quest authoring — init in setup() so assets are registered before builtin plugins start
        this.configManager = new ConfigManager(Path.of("mods/HyAdventureData"));
        this.authoringService = new QuestAuthoringService(configManager);
        this.registrationService = new QuestRegistrationService(configManager);
        this.questRoleService = new QuestRoleService(authoringService);
        this.translationService = new TranslationService(authoringService);
        this.templateService = new QuestTemplateService(authoringService);
        this.registrationService.registerAllOnStartup();
        this.translationService.regenerate();

        // Register quest giver provider so other plugins (e.g., HyCitizens) can query it
        dev.hytalemodding.api.ServiceRegistry.register(
                dev.hytalemodding.api.services.QuestGiverProvider.class, questRoleService);

        LOGGER.atInfo().log("[HyAdventure] Setup complete — 12 services initialized.");
    }

    @Override
    protected void start() {
        try {
            dev.hytalemodding.api.DashboardRegistry.register(new AdventureSchemaProvider(this));

            var apiServer = dev.hytalemodding.ExamplePlugin.get().getApiServer();
            if (apiServer != null) {
                apiServer.registerHandler("/api/adventure", new AdventureHandler(
                        objectiveService, reputationService, shopService, farmingService,
                        memoriesService, cameraService, teleporterService, npcObjectiveService,
                        stashService, authoringService, registrationService));
                LOGGER.atInfo().log("[HyAdventure] Registered /api/adventure endpoints.");
            }
        } catch (NoClassDefFoundError ignored) {}

        LOGGER.atInfo().log("[HyAdventure] Started.");
    }

    @Override
    protected void shutdown() {
        if (configManager != null) configManager.save();
        LOGGER.atInfo().log("[HyAdventure] Shut down.");
    }

    @Nonnull public static HyAdventurePlugin get() { return instance; }
    @Nonnull public ObjectiveService getObjectiveService() { return objectiveService; }
    @Nonnull public ReputationService getReputationService() { return reputationService; }
    @Nonnull public ShopService getShopService() { return shopService; }
    @Nonnull public FarmingService getFarmingService() { return farmingService; }
    @Nonnull public MemoriesService getMemoriesService() { return memoriesService; }
    @Nonnull public CameraService getCameraService() { return cameraService; }
    @Nonnull public TeleporterService getTeleporterService() { return teleporterService; }
    @Nonnull public NPCObjectiveService getNpcObjectiveService() { return npcObjectiveService; }
    @Nonnull public StashService getStashService() { return stashService; }
    @Nonnull public QuestAuthoringService getAuthoringService() { return authoringService; }
    @Nonnull public QuestRegistrationService getRegistrationService() { return registrationService; }
    @Nonnull public QuestRoleService getQuestRoleService() { return questRoleService; }
    @Nonnull public TranslationService getTranslationService() { return translationService; }
    @Nonnull public QuestTemplateService getTemplateService() { return templateService; }
}
