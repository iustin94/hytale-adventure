package dev.hytalemodding.hyadventure.services;

import dev.hytalemodding.api.services.QuestGiverProvider;
import dev.hytalemodding.hyadventure.models.AuthoredNpcAssignment;
import dev.hytalemodding.hyadventure.models.AuthoredQuestLine;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Implements {@link QuestGiverProvider} by looking up quest giver assignments
 * from the authored quest data. Other plugins (e.g., HyCitizens) query this
 * via {@link dev.hytalemodding.api.ServiceRegistry} during NPC role generation.
 */
public class QuestRoleService implements QuestGiverProvider {

    private final QuestAuthoringService authoring;

    public QuestRoleService(@Nonnull QuestAuthoringService authoring) {
        this.authoring = authoring;
    }

    @Override
    @Nullable
    public String getQuestGiverObjectiveId(@Nonnull String npcRoleName) {
        // Find an NPC assignment with this role that is linked as a quest giver
        for (AuthoredNpcAssignment npc : authoring.getData().getNpcAssignments()) {
            if (!npcRoleName.equals(npc.getNpcRole())) continue;

            // Find which quest line references this NPC assignment as quest giver
            for (AuthoredQuestLine line : authoring.getData().getQuestLines()) {
                if (!npc.getId().equals(line.getQuestGiverNpcId())) continue;
                if (line.getObjectiveIds().isEmpty()) continue;

                // Return the first objective of the quest line
                return line.getObjectiveIds().get(0);
            }
        }
        return null;
    }
}
