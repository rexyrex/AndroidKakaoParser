package com.rexyrex.kakaoparser.Constants;

import java.util.regex.Pattern;

public final class TextPatterns {
    public static final Pattern korean = Pattern.compile("(\\d{4}년 \\d{1,2}월 \\d{1,2}일 (?:오후|오전) \\d{1,2}:\\d{1,2}),? (.+?) : ?(.+)");
    public static final Pattern english = Pattern.compile("(\\w{3,9} \\d{1,2}, \\d{4}, \\d{1,2}:\\d{1,2} (?:PM|AM)), (.+?) : ?(.+)");

    //Catches 2 cases:
    //1. Only date (e.g. "2021년 6월 9일 오전 1:35")
    //2. Chat room events e.g. "2021년 6월 8일 오전 10:38, 쭈아/38/중구님이 들어왔습니다."
    public static final Pattern koreanDate = Pattern.compile("^(\\d{4}년 \\d{1,2}월 \\d{1,2}일 (?:오후|오전) \\d{1,2}:\\d{1,2}).*");
    public static final Pattern englishDate = Pattern.compile("^(\\w{3,9} \\d{1,2}, \\d{4}, \\d{1,2}:\\d{1,2} (?:PM|AM)).*");


}
