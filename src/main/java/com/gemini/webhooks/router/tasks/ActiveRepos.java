package com.gemini.webhooks.router.tasks;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class ActiveRepos {
    private final Set<String> repoNames = ConcurrentHashMap.newKeySet();

    public boolean contains(String repoName) {
        return repoNames.contains(repoName);
    }

    public boolean lockRepoFor(AgentTask task) {
        return repoNames.add(task.repoName());
    }

    public void unlockRepoFor(AgentTask task) {
        repoNames.remove(task.repoName());
    }
}
