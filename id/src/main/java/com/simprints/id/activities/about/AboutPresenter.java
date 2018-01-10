package com.simprints.id.activities.about;


import android.os.Handler;
import android.os.HandlerThread;
import android.support.annotation.NonNull;
import android.view.WindowManager;

import com.simprints.id.data.DataManager;
import com.simprints.id.exceptions.unsafe.UninitializedDataManagerError;
import com.simprints.id.model.ALERT_TYPE;
import com.simprints.id.tools.AlertLauncher;
import com.simprints.id.tools.AppState;
import com.simprints.libdata.DATA_ERROR;
import com.simprints.libdata.DataCallback;
import com.simprints.libdata.tools.Constants;

class AboutPresenter implements AboutContract.Presenter {

    @NonNull
    private final AboutContract.View aboutView;

    private static boolean recoveryRunning = false;

    private AppState appState;
    private RecoverDbHandlerThread recoverDbHandlerThread;

    private DataManager dataManager;

    private AlertLauncher alertLauncher;

    /**
     * @param view The AboutActivity
     */
    AboutPresenter(@NonNull AboutContract.View view, DataManager dataManager, AlertLauncher alertLauncher) {
        appState = AppState.getInstance();
        this.dataManager = dataManager;
        aboutView = view;
        aboutView.setPresenter(this);
        this.alertLauncher = alertLauncher;
    }

    @Override
    public void start() {
        Short hardwareVersion = dataManager.getHardwareVersion();
        aboutView.setVersionData(
                dataManager.getAppVersionName(),
                dataManager.getLibVersionName(),
                hardwareVersion > -1 ? String.valueOf(hardwareVersion) : "null");

        try {
            aboutView.setDbCountData(
                    Long.toString(dataManager.getPeopleCount(Constants.GROUP.USER)),
                    Long.toString(dataManager.getPeopleCount(Constants.GROUP.MODULE)),
                    Long.toString(dataManager.getPeopleCount(Constants.GROUP.GLOBAL)));
        } catch (UninitializedDataManagerError error) {
            dataManager.logError(error);
            alertLauncher.launch(ALERT_TYPE.UNEXPECTED_ERROR ,0);
        }

        if (recoveryRunning) aboutView.setRecoverDbUnavailable();
        else aboutView.setRecoverDbAvailable();
    }

    @Override
    public void recoverDb() {
        recoveryRunning = true;
        recoverDbHandlerThread = new RecoverDbHandlerThread("recoverDbHandlerThread");
        recoverDbHandlerThread.start();
        recoverDbHandlerThread.prepareHandler();
        recoverDbHandlerThread.postTask(new Runnable() {
            @Override
            public void run() {
                try {
                    dataManager.recoverRealmDb(Constants.GROUP.GLOBAL, newRecoverRealmDbCallback());
                } catch (UninitializedDataManagerError error) {
                    recoverDbHandlerThread.quit();
                    recoveryRunning = false;
                    dataManager.logError(error);
                    alertLauncher.launch(ALERT_TYPE.UNEXPECTED_ERROR, 0);
                }
            }
        });
    }

    private DataCallback newRecoverRealmDbCallback() {
        return new DataCallback() {
            @Override
            public void onSuccess() {
                recoverDbHandlerThread.quit();
                recoveryRunning = false;
                try {
                    aboutView.setSuccessRecovering();
                    aboutView.setRecoverDbAvailable();
                } catch (WindowManager.BadTokenException e) {
                    dataManager.logSafeException(e);
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(DATA_ERROR data_error) {
                recoverDbHandlerThread.quit();
                recoveryRunning = false;
                try {
                    aboutView.setErrorRecovering(data_error.details());
                    aboutView.setRecoverDbAvailable();
                } catch (WindowManager.BadTokenException e) {
                    dataManager.logSafeException(e);
                    e.printStackTrace();
                }
            }
        };
    }

    private class RecoverDbHandlerThread extends HandlerThread {

        Handler handler;

        RecoverDbHandlerThread(String name) {
            super(name);
        }

        void postTask(Runnable task) {
            handler.post(task);
        }

        void prepareHandler() {
            handler = new Handler(getLooper());
        }
    }
}
