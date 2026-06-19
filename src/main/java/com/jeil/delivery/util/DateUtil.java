package com.jeil.delivery.util;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import lombok.experimental.UtilityClass;

@UtilityClass
public class DateUtil {
	public static String getDate(String... patterns) {
		String pattern = "yyyy-MM-dd";
		if(patterns.length!=0) {
			pattern= patterns[0];
		}
		return LocalDate.now().format(DateTimeFormatter.ofPattern(pattern));
	}

	public static String getTime(String...patterns) {
		String pattern = "HH:mm:ss";
		if(patterns.length!=0) {
			pattern= patterns[0];
		}
		return LocalTime.now().format(DateTimeFormatter.ofPattern(pattern));
	}

	public static String getDateTime(String... patterns) {
	    String pattern = "yyyy-MM-dd HH:mm:ss";
	    if (patterns.length != 0) {
	        pattern = patterns[0];
	    }
	    return LocalDateTime.now().format(DateTimeFormatter.ofPattern(pattern));
	}

	public static List<String> getDateRange(String fromDate, String toDate) {
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd");
		LocalDate confirmDateFrom = LocalDate.parse(fromDate);
		LocalDate confirmDateTo = LocalDate.parse(toDate);

		int dateDiffrence = confirmDateTo.getDayOfYear() - confirmDateFrom.getDayOfYear();

		List<String> confirmDates = new ArrayList<>();
		for(int i=0; i<=dateDiffrence; i++) {
			confirmDates.add(confirmDateFrom.plusDays(i).format(formatter));
		}
		return confirmDates;
	}

	/**
	 * 특정 연월의 마지막 일자를 반환 (예: 2025, 7 -> 31)
	 */
	public static int getLastDay(int year, int month) {
	    return YearMonth.of(year, month).lengthOfMonth();
	}

	/**
	 * 특정 연월의 마지막 날을 yyyyMMdd 형식으로 반환 (예: 2025, 7 -> 20250731)
	 */
	public static String getLastDateOfMonth(int year, int month) {
	    LocalDate lastDay = YearMonth.of(year, month).atEndOfMonth();
	    return lastDay.format(DateTimeFormatter.BASIC_ISO_DATE);
	}
}
