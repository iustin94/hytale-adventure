package dev.hytalemodding.hyadventure.services;

import com.hypixel.hytale.logger.HytaleLogger;
import dev.hytalemodding.hyadventure.models.*;

import javax.annotation.Nonnull;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Generates .lang translation files from authored quest data so that
 * translation keys resolve to user-facing text in-game instead of
 * showing raw key strings.
 */
public class TranslationService {

    private static final HytaleLogger LOGGER = HytaleLogger.forEnclosingClass();
    private static final Path LANG_DIR = Path.of("mods/HyAdventureData/Server/Languages/en-US");
    private static final Path LANG_FILE = LANG_DIR.resolve("server.lang");

    private final QuestAuthoringService authoring;

    public TranslationService(@Nonnull QuestAuthoringService authoring) {
        this.authoring = authoring;
    }

    /**
     * Regenerates the .lang file from all authored quest data.
     * Call after any content creation or update.
     */
    public void regenerate() {
        Map<String, String> translations = new LinkedHashMap<>();
        AuthoredQuestData data = authoring.getData();

        // Quest line translations
        for (AuthoredQuestLine line : data.getQuestLines()) {
            String titleKey = line.getTitleKey();
            String descKey = line.getDescriptionKey();
            // Use ID as fallback display text, formatted nicely
            translations.put(titleKey, formatDisplayName(titleKey, line.getId()));
            if (!descKey.isEmpty()) {
                translations.put(descKey, formatDisplayName(descKey, line.getId() + " description"));
            }
        }

        // Objective translations
        for (AuthoredObjective obj : data.getObjectives()) {
            String titleKey = obj.getTitleKey();
            String descKey = obj.getDescriptionKey();
            translations.put(titleKey, formatDisplayName(titleKey, obj.getId()));
            if (!descKey.isEmpty()) {
                translations.put(descKey, formatDisplayName(descKey, obj.getId() + " description"));
            }

            // Task set and task descriptions
            for (int i = 0; i < obj.getTaskSets().size(); i++) {
                AuthoredTaskSet ts = obj.getTaskSets().get(i);
                if (!ts.getDescriptionId().isEmpty()) {
                    translations.put(ts.getDescriptionId(),
                            formatDisplayName(ts.getDescriptionId(), obj.getId() + " phase " + (i + 1)));
                }
                for (int j = 0; j < ts.getTasks().size(); j++) {
                    AuthoredTask task = ts.getTasks().get(j);
                    if (!task.getDescriptionId().isEmpty()) {
                        translations.put(task.getDescriptionId(),
                                formatTaskDescription(task, obj.getId(), i, j));
                    }
                }
            }
        }

        // Dialog translations — use user-provided display text if available
        for (AuthoredDialog dlg : data.getDialogs()) {
            if (!dlg.getEntityNameKey().isEmpty()) {
                String nameText = dlg.getEntityNameText().isEmpty()
                        ? formatDisplayName(dlg.getEntityNameKey(), dlg.getId())
                        : dlg.getEntityNameText();
                translations.put(dlg.getEntityNameKey(), nameText);
            }
            if (!dlg.getDialogKey().isEmpty()) {
                String dialogText = dlg.getDialogText().isEmpty()
                        ? "Hello, adventurer!"
                        : dlg.getDialogText();
                translations.put(dlg.getDialogKey(), dialogText);
            }

            // Dialog step translations
            for (int i = 0; i < dlg.getSteps().size(); i++) {
                var step = dlg.getSteps().get(i);
                if (!step.getSpeakerNameText().isEmpty()) {
                    translations.put(dlg.getId() + "_step_" + i + "_name", step.getSpeakerNameText());
                }
                if (!step.getDialogText().isEmpty()) {
                    translations.put(dlg.getId() + "_step_" + i + "_text", step.getDialogText());
                }
                for (int j = 0; j < step.getChoices().size(); j++) {
                    var choice = step.getChoices().get(j);
                    if (!choice.getLabelText().isEmpty()) {
                        translations.put(dlg.getId() + "_step_" + i + "_choice_" + j, choice.getLabelText());
                    }
                }
            }
        }

        writeLangFile(translations);
    }

    private String formatDisplayName(String key, String fallback) {
        // If the key looks like an actual display name (contains spaces or capitals), use it
        if (key.contains(" ") || (!key.contains("_") && !key.equals(key.toLowerCase()))) {
            return key;
        }
        // Otherwise format the fallback: replace underscores with spaces, title case
        return toTitleCase(fallback.replace("_", " "));
    }

    private String formatTaskDescription(AuthoredTask task, String objId, int phaseIdx, int taskIdx) {
        return switch (task.getType()) {
            case GATHER -> "Gather " + task.getCount() + "x " +
                    (task.getBlockTagOrItemId().isEmpty() ? "items" : formatDisplayName(task.getBlockTagOrItemId(), task.getBlockTagOrItemId()));
            case CRAFT -> "Craft " + task.getCount() + "x " +
                    (task.getBlockTagOrItemId().isEmpty() ? "items" : formatDisplayName(task.getBlockTagOrItemId(), task.getBlockTagOrItemId()));
            case USE_BLOCK -> "Use " + (task.getBlockTagOrItemId().isEmpty() ? "block" : formatDisplayName(task.getBlockTagOrItemId(), task.getBlockTagOrItemId()));
            case USE_ENTITY -> "Talk to " + (task.getDialogEntityNameKey().isEmpty() ? "NPC" : task.getDialogEntityNameKey());
            case REACH_LOCATION -> "Reach " + (task.getTargetLocationId().isEmpty() ? "the destination" : formatDisplayName(task.getTargetLocationId(), task.getTargetLocationId()));
            case KILL -> "Defeat " + task.getCount() + "x " +
                    (task.getNpcGroupId().isEmpty() ? "enemies" : formatDisplayName(task.getNpcGroupId(), task.getNpcGroupId()));
            case KILL_SPAWN_MARKER -> "Clear enemies at " +
                    (task.getNpcGroupId().isEmpty() ? "the marked location" : formatDisplayName(task.getNpcGroupId(), task.getNpcGroupId()));
            case BOUNTY -> "Complete bounty on " +
                    (task.getNpcId().isEmpty() ? "target" : formatDisplayName(task.getNpcId(), task.getNpcId()));
            case TREASURE_MAP -> "Find the treasure";
        };
    }

    private static String toTitleCase(String input) {
        if (input.isEmpty()) return input;
        StringBuilder sb = new StringBuilder();
        boolean nextUpper = true;
        for (char c : input.toCharArray()) {
            if (c == ' ' || c == '_') {
                sb.append(' ');
                nextUpper = true;
            } else if (nextUpper) {
                sb.append(Character.toUpperCase(c));
                nextUpper = false;
            } else {
                sb.append(Character.toLowerCase(c));
            }
        }
        return sb.toString();
    }

    private void writeLangFile(Map<String, String> translations) {
        try {
            File dir = LANG_DIR.toFile();
            if (!dir.exists()) dir.mkdirs();

            StringBuilder sb = new StringBuilder();
            sb.append("# Auto-generated by HyAdventure — do not edit manually\n");
            for (var entry : translations.entrySet()) {
                if (entry.getKey().isEmpty()) continue;
                sb.append(entry.getKey()).append(" = ").append(entry.getValue()).append('\n');
            }
            Files.writeString(LANG_FILE, sb.toString());
            LOGGER.atInfo().log("[HyAdventure] Generated translations: " + translations.size() + " entries");
        } catch (IOException e) {
            LOGGER.atWarning().log("[HyAdventure] Failed to write lang file: " + e.getMessage());
        }
    }
}
