package com.vibez.chat;

import android.app.Application;
import android.content.Intent;

import java.io.PrintWriter;
import java.io.StringWriter;

public class VibeZApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        Thread.setDefaultUncaughtExceptionHandler(new GlobalExceptionHandler());
    }

    private class GlobalExceptionHandler implements Thread.UncaughtExceptionHandler {
        private final Thread.UncaughtExceptionHandler defaultUEH;

        GlobalExceptionHandler() {
            this.defaultUEH = Thread.getDefaultUncaughtExceptionHandler();
        }

        @Override
        public void uncaughtException(Thread t, Throwable e) {
            StringWriter sw = new StringWriter();
            e.printStackTrace(new PrintWriter(sw));
            String stackTrace = sw.toString();

            Intent intent = new Intent(getApplicationContext(), CrashActivity.class);
            intent.putExtra(CrashActivity.EXTRA_CRASH_LOG, stackTrace);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);

            // Terminate the current process
            android.os.Process.killProcess(android.os.Process.myPid());
            System.exit(10);

            // Let the default handler do its thing
            defaultUEH.uncaughtException(t, e);
        }
    }
}