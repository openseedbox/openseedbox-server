package com.openseedbox.jobs;

import com.openseedbox.Config;
import com.openseedbox.backend.ITorrentBackend;
import com.openseedbox.code.Util;
import play.Logger;
import play.jobs.Job;
import play.jobs.OnApplicationStart;

@OnApplicationStart
public class StartBackendJob extends Job<ITorrentBackend> {
    public static ITorrentBackend backend;
    static boolean iStartedIt = false;
    static boolean iWasHere = false;


    @Override
    public ITorrentBackend doJobWithResult() throws Exception {
        iWasHere = true;
        Class c = Class.forName(Config.getBackendClassName());
        backend = (ITorrentBackend) c.newInstance();
        if (!backend.isRunning() && Config.isBackendAutostart()) {
            backend.start();
            iStartedIt = true;
        }
        return backend;
    }
}
