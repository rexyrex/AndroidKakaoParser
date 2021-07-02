package com.rexyrex.kakaoparser.Constants;

import java.text.SimpleDateFormat;
import java.util.Locale;

public final class DateFormats {
    public static final SimpleDateFormat koreanDate = new SimpleDateFormat("yyyy년 M월 d일 a h:m", Locale.KOREAN);
    public static final SimpleDateFormat englishDate = new SimpleDateFormat("MMMM d, yyyy, h:m a", Locale.ENGLISH);

    public static final SimpleDateFormat day = new SimpleDateFormat("yyyy년 M월 d일 (E)");
    public static final SimpleDateFormat month = new SimpleDateFormat("yyyy년 M월");
    public static final SimpleDateFormat year = new SimpleDateFormat("yyyy년");
    public static final SimpleDateFormat dayOfWeek = new SimpleDateFormat("E");
    public static final SimpleDateFormat hourOfDay = new SimpleDateFormat("H");

    public static final SimpleDateFormat simpleKoreanFormat = new SimpleDateFormat("yyyy년 M월 d일 (E)", Locale.KOREAN);
    public static final SimpleDateFormat defaultFormat = new SimpleDateFormat("yyyy-MM-dd");
}
