package com.openseedbox.jobs;

import com.openseedbox.backend.ITorrentBackend;
import play.Logger;
import play.jobs.Job;
import play.jobs.OnApplicationStop;

@OnApplicationStop
public class StopBackendJob extends Job<ITorrentBackend> {
    @Override
    public void doJob() throws Exception {
        if (!StartBackendJob.iWasHere) {
            Logger.error("WTF?!");
            throw new IllegalStateException(String.format("%s didn't run, but it should!", StartBackendJob.class.getSimpleName()));
        }
        if (StartBackendJob.iStartedIt && StartBackendJob.backend.isRunning()) {
            Logger.info("Stopping backend because we started it!");
            StartBackendJob.backend.stop();
        }
    }
}
