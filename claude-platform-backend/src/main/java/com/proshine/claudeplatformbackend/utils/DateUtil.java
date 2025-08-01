package com.proshine.claudeplatformbackend.utils;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

public class DateUtil {
    
    public static final String DEFAULT_DATETIME_PATTERN = "yyyy-MM-dd HH:mm:ss";
    public static final String DEFAULT_DATE_PATTERN = "yyyy-MM-dd";
    public static final String DEFAULT_TIME_PATTERN = "HH:mm:ss";
    
    private static final DateTimeFormatter DEFAULT_DATETIME_FORMATTER = 
        DateTimeFormatter.ofPattern(DEFAULT_DATETIME_PATTERN);
    private static final DateTimeFormatter DEFAULT_DATE_FORMATTER = 
        DateTimeFormatter.ofPattern(DEFAULT_DATE_PATTERN);
    private static final DateTimeFormatter DEFAULT_TIME_FORMATTER = 
        DateTimeFormatter.ofPattern(DEFAULT_TIME_PATTERN);
    
    /**
     * 将时间戳转换为LocalDateTime
     */
    public static LocalDateTime timestampToLocalDateTime(long timestamp) {
        return LocalDateTime.ofInstant(Instant.ofEpochMilli(timestamp), ZoneId.systemDefault());
    }
    
    /**
     * 将LocalDateTime转换为时间戳
     */
    public static long localDateTimeToTimestamp(LocalDateTime dateTime) {
        return dateTime.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
    }
    
    /**
     * 格式化时间戳为字符串
     */
    public static String formatTimestamp(long timestamp) {
        return formatTimestamp(timestamp, DEFAULT_DATETIME_PATTERN);
    }
    
    /**
     * 格式化时间戳为字符串（自定义格式）
     */
    public static String formatTimestamp(long timestamp, String pattern) {
        LocalDateTime dateTime = timestampToLocalDateTime(timestamp);
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(pattern);
        return dateTime.format(formatter);
    }
    
    /**
     * 格式化LocalDateTime为字符串
     */
    public static String formatDateTime(LocalDateTime dateTime) {
        return dateTime.format(DEFAULT_DATETIME_FORMATTER);
    }
    
    /**
     * 格式化LocalDateTime为日期字符串
     */
    public static String formatDate(LocalDateTime dateTime) {
        return dateTime.format(DEFAULT_DATE_FORMATTER);
    }
    
    /**
     * 格式化LocalDateTime为时间字符串
     */
    public static String formatTime(LocalDateTime dateTime) {
        return dateTime.format(DEFAULT_TIME_FORMATTER);
    }
    
    /**
     * 获取当天开始时间戳
     */
    public static long getTodayStartTimestamp() {
        LocalDateTime today = LocalDateTime.now().withHour(0).withMinute(0).withSecond(0).withNano(0);
        return localDateTimeToTimestamp(today);
    }
    
    /**
     * 获取当天结束时间戳
     */
    public static long getTodayEndTimestamp() {
        LocalDateTime todayEnd = LocalDateTime.now().withHour(23).withMinute(59).withSecond(59).withNano(999999999);
        return localDateTimeToTimestamp(todayEnd);
    }
    
    /**
     * 获取本月开始时间戳
     */
    public static long getMonthStartTimestamp() {
        LocalDateTime monthStart = LocalDateTime.now().withDayOfMonth(1).withHour(0).withMinute(0).withSecond(0).withNano(0);
        return localDateTimeToTimestamp(monthStart);
    }
    
    /**
     * 获取本月结束时间戳
     */
    public static long getMonthEndTimestamp() {
        LocalDateTime monthEnd = LocalDateTime.now().withDayOfMonth(1).plusMonths(1).minusNanos(1);
        return localDateTimeToTimestamp(monthEnd);
    }
    
    /**
     * 获取指定天数前的时间戳
     */
    public static long getDaysAgoTimestamp(int days) {
        LocalDateTime daysAgo = LocalDateTime.now().minusDays(days).withHour(0).withMinute(0).withSecond(0).withNano(0);
        return localDateTimeToTimestamp(daysAgo);
    }
    
    /**
     * 计算时间差（分钟）
     */
    public static long getMinutesBetween(long startTimestamp, long endTimestamp) {
        return (endTimestamp - startTimestamp) / (1000 * 60);
    }
    
    /**
     * 计算时间差（小时）
     */
    public static long getHoursBetween(long startTimestamp, long endTimestamp) {
        return (endTimestamp - startTimestamp) / (1000 * 60 * 60);
    }
    
    /**
     * 计算时间差（天）
     */
    public static long getDaysBetween(long startTimestamp, long endTimestamp) {
        return (endTimestamp - startTimestamp) / (1000 * 60 * 60 * 24);
    }
    
    /**
     * 判断时间戳是否为今天
     */
    public static boolean isToday(long timestamp) {
        long todayStart = getTodayStartTimestamp();
        long todayEnd = getTodayEndTimestamp();
        return timestamp >= todayStart && timestamp <= todayEnd;
    }
    
    /**
     * 判断时间戳是否为本月
     */
    public static boolean isThisMonth(long timestamp) {
        long monthStart = getMonthStartTimestamp();
        long monthEnd = getMonthEndTimestamp();
        return timestamp >= monthStart && timestamp <= monthEnd;
    }
}