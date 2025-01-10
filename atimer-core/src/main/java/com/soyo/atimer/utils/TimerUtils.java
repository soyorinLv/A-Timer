package com.soyo.atimer.utils;

import org.quartz.CronExpression;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class TimerUtils {
    public static String getCreateLockKey(String app) {
        return "create_timer_lock_" + app;
    }

    public static String getEnableLockKey(String app) {
        return "enable_timer_lock_" + app;
    }

    public static String getMigratorLockKey(Date date) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH");
        String dateStr = sdf.format(date);
        return "migrator_lock_" + dateStr;
    }

    public static String getTimeBucketLockKey(Date time, int bucketId) {
        StringBuilder sb = new StringBuilder();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm");
        String timeStr = sdf.format(time);
        return sb.append("time_bucket_lock_").append(timeStr).append("_").append(bucketId).toString();
    }

    public static String getSliceMsgKey(Date time, int bucketId) {
        StringBuilder sb = new StringBuilder();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm");
        String timeStr = sdf.format(time);
        return sb.append(timeStr).append("_").append(bucketId).toString();
    }

    public static String getTokenStr() {
        long timestamp = System.currentTimeMillis();
        String thread = Thread.currentThread().getName();
        return thread + timestamp;
    }

    public static Date getForwardTwoMigrateStepEnd(Date start, int diffMinutes) {
        return new Date(start.getTime() + 2L * diffMinutes * 60000);
    }

    public static List<Long> getCronNextsBetween(CronExpression cronExpression, Date now, Date end) {
        List<Long> times = new ArrayList<>();
        if (end.before(now)) {
            return times;
        }

        for (Date start = now; start.before(end); ) {
            Date next = cronExpression.getNextValidTimeAfter(start);
            times.add(next.getTime());
            start = next;
        }
        return times;
    }

    public static String unionTimerIDUnix(long timerId, long unix) {
        return new StringBuilder().append(timerId).append("_").append(unix).toString();
    }

    public static List<Long> splitTimerIDUnix(String timerIDUnix) {
        List<Long> longSet = new ArrayList<>();
        String[] strList = timerIDUnix.split("_");
        if (strList.length != 2) {
            return longSet;
        }
        longSet.add(Long.parseLong(strList[0]));
        longSet.add(Long.parseLong(strList[1]));
        return longSet;
    }
}
