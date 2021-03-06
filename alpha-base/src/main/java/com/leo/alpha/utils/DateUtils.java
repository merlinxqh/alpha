package com.leo.alpha.utils;

import org.apache.commons.lang3.time.DateFormatUtils;

import java.text.ParseException;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.Date;

/**
 * Created by leo on 2017/5/4.
 * Date工具类
 */
public class DateUtils {

    public static final String DATE_PATTERN_SIMPLE="yyyy-MM-dd";

    public static final String DATE_PATTERN_COMPLEX="yyyy-MM-dd HH:mm:ss";

    /**
     * 默认 yyyy-MM-dd
     * @param date
     * @return
     */
    public static Date parseDate(String date){
        return parseDate(date,DATE_PATTERN_SIMPLE);
    }
    
    /**
     * @param date
     * @param pattern
     * @return
     */
    public static Date parseDate(String date,String pattern){
        if(DATE_PATTERN_SIMPLE.equals(pattern)){
            date=autoMatchPattern(DATE_PATTERN_SIMPLE,date);
            LocalDate localDate=LocalDate.parse(date,DateTimeFormatter.ofPattern(pattern));
            ZoneId zone = ZoneId.systemDefault();
            return Date.from(localDate.atStartOfDay().atZone(zone).toInstant());
        }
        if(DATE_PATTERN_COMPLEX.equals(pattern)) {
            date=autoMatchPattern(DATE_PATTERN_COMPLEX,date);
            LocalDateTime localDateTime = LocalDateTime.parse(date, DateTimeFormatter.ofPattern(pattern));
            ZoneId zone = ZoneId.systemDefault();
            return Date.from(localDateTime.atZone(zone).toInstant());
        }

    	return null;
    }

    /**
     * 匹配格式
     * @param pattern
     * @param date
     * @return
     */
    public static String autoMatchPattern(String pattern,String date){
        if(date.length() == pattern.length()){
            return date;
        }

        String[] arry=date.split(" ");
        StringBuffer sb=new StringBuffer();
        for(int i=0;i<arry[0].split("-").length;i++){
            String sub=arry[0].split("-")[i];
            if(sub.length() == 1){
               sub="0"+sub;
            }
            if(i == 0){
                sb.append(sub);
            }else{
                sb.append("-"+sub);
            }
        }
        if(arry.length == 2){
            sb.append(" ");
            for(int i=0;i<arry[1].split(":").length;i++){
                String sub=arry[1].split(":")[i];
                if(sub.length() == 1){
                    sub="0"+sub;
                }
                if(i == 0){
                    sb.append(sub);
                }else{
                    sb.append(":"+sub);
                }
            }
        }
        return sb.toString();
    }
    
    /**
     * 时间格式化
     * @param date
     * @return
     */
    public static String formatDate(Date date){
    	return formatDate(date,DATE_PATTERN_SIMPLE);
    }
    
    /**
     * 格式化时间
     * @param date
     * @param pattern
     * @return
     */
    public static String formatDate(Date date, String pattern){
        Instant instant = date.toInstant();
        ZoneId zone = ZoneId.systemDefault();
        LocalDateTime local = LocalDateTime.ofInstant(instant, zone);
        return local.format(DateTimeFormatter.ofPattern(pattern));
    }


    /**
     * 得到当前日期字符串 格式（yyyy-MM-dd） pattern可以为："yyyy-MM-dd" "HH:mm:ss" "E"
     */
    public static String getDate(String pattern) {
        return DateFormatUtils.format(new Date(), pattern);
    }

	/**
	 * 时间添加
	 * @param date
	 * @param amountToAdd
	 * @param unit
	 * @return
	 */
	public static Date plusDate(Date date,long amountToAdd, ChronoUnit unit){
        Instant instant = date.toInstant();
        ZoneId zone = ZoneId.systemDefault();
        LocalDateTime local = LocalDateTime.ofInstant(instant, zone);
        local=local.plus(amountToAdd,unit);
        return Date.from(local.atZone(zone).toInstant());
    }
}
