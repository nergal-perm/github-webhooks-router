package com.gemini.webhooks.router.tasks;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class ActiveRepos {
    private final Set<String> repoNames = ConcurrentHashMap.newKeySet();

    public boolean isTaken(String repoName) {
        return repoNames.contains(repoName);
    }

    public boolean takeFor(AgentTask task) {
        return repoNames.add(task.repoName());
    }

    public void releaseFor(AgentTask task) {
        repoNames.remove(task.repoName());
    }
}
