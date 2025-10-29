package com.apex.UISettings.utils;

import android.util.Log;

import com.opendroid.base.BuildConfig;
//import com.sdksuite.vendorkit.BuildConfig;

import java.util.HashMap;
import java.util.Map;
public class Logger {

    private final String tag;

    private Logger(String tag) {
        this.tag = tag;
    }

    // 日志等级常量
    private static final int LEVEL_V = 0;
    private static final int LEVEL_D = 1;
    private static final int LEVEL_I = 2;
    private static final int LEVEL_W = 3;
    private static final int LEVEL_E = 4;
    private static final int LEVEL_F = 5;

    // 日志输出接口
    public interface LoggerOutput {
        void output(int level, String tag, String message);
    }

    // 默认输出实现
    public static LoggerOutput loggerOutput = new LoggerOutput() {
        @Override
        public void output(int level, String tag, String message) {
            switch (level) {
                case LEVEL_V:
                    Log.v(tag, message);
                    break;
                case LEVEL_D:
                    Log.d(tag, message);
                    break;
                case LEVEL_I:
                    Log.i(tag, message);
                    break;
                case LEVEL_W:
                    Log.w(tag, message);
                    break;
                case LEVEL_E:
                    Log.e(tag, message);
                    break;
                case LEVEL_F:
                    Log.wtf(tag, message);
                    break;
            }
        }
    };

    private static final Map<String, Logger> LOGGER_INSTANCES = new HashMap<>();

    // 日志等级：Debug 模式打印所有，Release 模式从 INFO 开始
    public static volatile int loggerLevel = (BuildConfig.DEBUG ? LEVEL_V : LEVEL_I);

    public static Logger getLogger(String tag) {
        String newTag = tag;
//        if (!BuildConfig.DEBUG) {
//            newTag = "ApexLauncherActivity";
//        }
        synchronized (LOGGER_INSTANCES) {
            if (LOGGER_INSTANCES.containsKey(newTag)) {
                return LOGGER_INSTANCES.get(newTag);
            }
            Logger logger = new Logger(newTag);
            LOGGER_INSTANCES.put(newTag, logger);
            return logger;
        }
    }

    public static Logger getLogger(Class<?> clazz) {
        return getLogger(clazz.getSimpleName());
    }

    private static void output(int level, String tag, String message) {
        if (level >= loggerLevel) {
            loggerOutput.output(level, tag, message);
        }
    }

    // 日志打印方法
    public void v(String message) {
        output(LEVEL_V, tag, message);
    }

    public void d(String message) {
        output(LEVEL_D, tag, message);
    }

    public void i(String message) {
        output(LEVEL_I, tag, message);
    }

    public void w(String message) {
        output(LEVEL_W, tag, message);
    }

    public void e(String message) {
        output(LEVEL_E, tag, message);
    }

    public void f(String message) {
        output(LEVEL_F, tag, message);
    }
}
