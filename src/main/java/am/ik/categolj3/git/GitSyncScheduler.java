package am.ik.categolj3.git;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class GitSyncScheduler {

    @Autowired
    GitStore gitStore;

    @Scheduled(cron = "0 0/30 * * * *")
    public void onSchedule() {
        gitStore.pull().thenAccept(r -> {
            gitStore.forceRefreshAll();
        });
    }
}
