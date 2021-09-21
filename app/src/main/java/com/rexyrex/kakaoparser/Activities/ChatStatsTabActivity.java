package com.rexyrex.kakaoparser.Activities;

import android.graphics.Color;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.os.Bundle;

import com.bumptech.glide.Glide;
import com.google.android.gms.ads.AdError;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.FullScreenContentCallback;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.ResponseInfo;
import com.google.android.gms.ads.interstitial.InterstitialAd;
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.tabs.TabLayout;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.viewpager.widget.ViewPager;
import androidx.appcompat.app.AppCompatActivity;

import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.AbsoluteSizeSpan;
import android.text.style.ForegroundColorSpan;
import android.text.style.StyleSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.crashlytics.FirebaseCrashlytics;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.rexyrex.kakaoparser.Constants.DateFormats;
import com.rexyrex.kakaoparser.Constants.TextPatterns;
import com.rexyrex.kakaoparser.Database.DAO.AnalysedChatDAO;
import com.rexyrex.kakaoparser.Database.DAO.ChatLineDAO;
import com.rexyrex.kakaoparser.Database.DAO.WordDAO;
import com.rexyrex.kakaoparser.Database.MainDatabase;
import com.rexyrex.kakaoparser.Database.Models.AnalysedChatModel;
import com.rexyrex.kakaoparser.Database.Models.ChatLineModel;
import com.rexyrex.kakaoparser.Database.Models.WordModel;
import com.rexyrex.kakaoparser.Entities.ChatData;
import com.rexyrex.kakaoparser.Entities.DateIntPair;
import com.rexyrex.kakaoparser.Entities.StringIntPair;
import com.rexyrex.kakaoparser.R;
import com.rexyrex.kakaoparser.Utils.AdUtils;
import com.rexyrex.kakaoparser.Utils.FileParseUtils;
import com.rexyrex.kakaoparser.Utils.FirebaseUtils;
import com.rexyrex.kakaoparser.Utils.LogUtils;
import com.rexyrex.kakaoparser.Utils.SharedPrefUtils;
import com.rexyrex.kakaoparser.Utils.StringParseUtils;
import com.rexyrex.kakaoparser.Utils.TimeUtils;
import com.rexyrex.kakaoparser.ui.main.SectionsPagerAdapter;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.text.NumberFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ChatStatsTabActivity extends AppCompatActivity {

    ChatData cd;
    ProgressBar popupPB;
    TextView popupPBProgressTV;
    TextView loadingTextTV;
    ImageView loadingGifIV;
    TextView popupPBProgressDtlTV;
    Button popupPBCancelBtn;
    boolean showPopupPBDtl = false;

    SectionsPagerAdapter sectionsPagerAdapter;
    ViewPager viewPager;
    TabLayout tabs;
    TextView titleTV;

    MainDatabase database;
    ChatLineDAO chatLineDao;
    WordDAO wordDao;
    AnalysedChatDAO analysedChatDAO;

    AsyncTask<String, Void, String> statsTask;
    AsyncTask<String, Void, String> loadTask;
    AlertDialog dialog;

    String[] chatLines;

    NumberFormat numberFormat;

    //final progress text indicator
    String finalStatusText = "";
    //final progress sub text index
    int finalStatusSubIndex = 0;
    int finalStatusSubCount = 13;

    SharedPrefUtils spu;

    boolean shouldBackup = true;
    boolean backupComplete = false;
    int chatterCount = 0;

    boolean analysed;
    String lastAnalyseDtStr;

    Date startDt, endDt;
    String startDtStr, endDtStr;

    String dateRangeStr = "";

    FrameLayout adContainer;
    private AdView mAdView;
    private InterstitialAd mInterstitialAd;
    private AdRequest adRequest;

    public List<DateIntPair> timePreloadDayList;
    public List timePreloadMonthList;
    public List timePreloadYearList;
    public List timePreloadTimeOfDayList;
    public List timePreloadDayOFWeekList;

    Runtime runtime;

    SimpleDateFormat titleDateFormat = new SimpleDateFormat("yyyy.M.d");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tab);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        timePreloadDayList = new ArrayList<>();
        timePreloadTimeOfDayList = new ArrayList<>();

        cd = ChatData.getInstance(this);
        final File chatFile = cd.getChatFile();

        runtime = Runtime.getRuntime();

        spu = new SharedPrefUtils(this);

        //analysed = getIntent().getBooleanExtra("analysed", false);

        analysed = FileParseUtils.parseFileForTitle(cd.getChatFile()).equals(spu.getString(R.string.SP_LAST_ANALYSE_TITLE, "null"))
                && StringParseUtils.chatFileNameToDate(cd.getChatFile().getName()).equals(spu.getString(R.string.SP_LAST_ANALYSE_DT, "null"))
                && getIntent().getStringExtra("startDt").equals(spu.getString(R.string.SP_LAST_ANALYSE_START_DT, "null"))
                && getIntent().getStringExtra("endDt").equals(spu.getString(R.string.SP_LAST_ANALYSE_END_DT, "null"));


        lastAnalyseDtStr = getIntent().getStringExtra("lastAnalyseDt");

        //ad
        adRequest = new AdRequest.Builder().build();
        if(!analysed){
            loadAd();

//            final AdLoader adLoader = new AdLoader.Builder(this, "ca-app-pub-3940256099942544/2247696110")
//                    .forNativeAd(new NativeAd.OnNativeAdLoadedListener() {
//                        @Override
//                        public void onNativeAdLoaded(NativeAd NativeAd) {
//                            // Show the ad.
//                            LogUtils.e("native ad vid content: " + NativeAd.getMediaContent().hasVideoContent());
//
////                            if (adLoader.isLoading()) {
////                                // The AdLoader is still loading ads.
////                                // Expect more adLoaded or onAdFailedToLoad callbacks.
////                            } else {
////                                // The AdLoader has finished loading ads.
////                            }
//
//                            if (isDestroyed()) {
//                                NativeAd.destroy();
//                                return;
//                            }
//                        }
//                    })
//                    .withAdListener(new AdListener() {
//                        @Override
//                        public void onAdFailedToLoad(LoadAdError adError) {
//                            LogUtils.e("native ad vid fail: " + adError.toString());
//                            // Handle the failure by logging, altering the UI, and so on.
//                        }
//                    })
//                    .withNativeAdOptions(new NativeAdOptions.Builder()
//                            // Methods in the NativeAdOptions.Builder class can be
//                            // used here to specify individual options settings.
//                            .build())
//                    .build();
//
//            adLoader.loadAds(new AdRequest.Builder().build(), 3);
        }

        //ad
        adContainer = findViewById(R.id.adView);
        mAdView = new AdView(this);
        mAdView.setAdUnitId(getString(R.string.AdMob_ad_unit_ID_Banner_Chat_Tab));
        adContainer.addView(mAdView);

        AdRequest adRequest = new AdRequest.Builder().build();
        mAdView.setAdSize(AdUtils.getAdSize(this));
        mAdView.loadAd(adRequest);

        String chatTitle = FileParseUtils.parseFileForTitle(chatFile);

        try {
            Calendar tmpCal = Calendar.getInstance();
            startDtStr = getIntent().getStringExtra("startDt");
            startDt = DateFormats.defaultFormat.parse(startDtStr);
            tmpCal.setTime(startDt);
            tmpCal.set(Calendar.HOUR_OF_DAY, 0);
            tmpCal.set(Calendar.MINUTE, 0);
            tmpCal.set(Calendar.SECOND, 0);
            startDt = new Date(tmpCal.getTimeInMillis());

            endDtStr = getIntent().getStringExtra("endDt");
            endDt = DateFormats.defaultFormat.parse(endDtStr);
            tmpCal.setTime(endDt);
            tmpCal.set(Calendar.HOUR_OF_DAY, 23);
            tmpCal.set(Calendar.MINUTE, 59);
            tmpCal.set(Calendar.SECOND, 59);
            endDt = new Date(tmpCal.getTimeInMillis());

            LogUtils.e("startDt : " + getIntent().getStringExtra("startDt"));
            LogUtils.e("endDt : " + getIntent().getStringExtra("endDt"));

            spu.saveString(R.string.SP_CHAT_DT_RANGE_STRING, "(" + titleDateFormat.format(startDt) + " ~ " + titleDateFormat.format(endDt) + ")");

        } catch (ParseException e) {
            e.printStackTrace();
        }


        View view = (LayoutInflater.from(ChatStatsTabActivity.this)).inflate(R.layout.horizontal_progress_popup, null);
        popupPB = view.findViewById(R.id.popupPB);
        popupPBProgressTV = view.findViewById(R.id.popupPBProgressTV);
        loadingTextTV = view.findViewById(R.id.loadingTextTV);
        loadingGifIV = view.findViewById(R.id.loadingGifIV);
        popupPBProgressDtlTV = view.findViewById(R.id.popupPBProgressDetailTV);
        popupPBCancelBtn = view.findViewById(R.id.popupPBCancelBtn);

        Glide.with(this).asGif().load(R.drawable.loading1).into(loadingGifIV);

        viewPager = findViewById(R.id.view_pager);
        tabs = findViewById(R.id.tabs);
        titleTV = findViewById(R.id.title);

        database = MainDatabase.getDatabase(this);
        chatLineDao = database.getChatLineDAO();
        wordDao = database.getWordDAO();
        analysedChatDAO = database.getAnalysedChatDAO();

        numberFormat = NumberFormat.getInstance();
        numberFormat.setGroupingUsed(true);

        //DecelerateInterpolator ai = new DecelerateInterpolator();
        //ai.getInterpolation(500);
        //popupPB.setInterpolator(ai);

        //popupPB.setMax(1000);
        //ObjectAnimator progressAnimator = ObjectAnimator.ofInt(popupPB, "progress", 10000, 0);
        //progressAnimator.start();

        AlertDialog.Builder rexAlertBuilder = new AlertDialog.Builder(ChatStatsTabActivity.this, R.style.PopupStyle);
        rexAlertBuilder.setView(view);
        //rexAlertBuilder.setTitle("대화 내용 분석중...");
        rexAlertBuilder.setCancelable(false);
        dialog = rexAlertBuilder.create();
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        dialog.show();




        statsTask = new AsyncTask<String, Void, String>() {
            @Override
            protected void onPreExecute() {
                super.onPreExecute();
            }

            @Override
            protected String doInBackground(String... params) {

                try {
                    //Text to alert user loading not started yet (DB clear can take some time)
                    ChatStatsTabActivity.this.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            loadingTextTV.setText("분석 준비중...");
                        }
                    });

                    //clear tables
                    wordDao.truncateTable();
                    chatLineDao.truncateTable();

                    //Actual analysis start
                    ChatStatsTabActivity.this.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            loadingTextTV.setText("대화 내용 분석중...");
                        }
                    });

                    long loadStartTime = System.currentTimeMillis();

                    //First, load chat room name only (later load date as spannable string)

                    boolean isKorean = !chatTitle.contains("KakaoTalk Chats with ");
                    Pattern pattern = cd.getChatLinePattern();
                    Pattern datePattern = cd.getDatePattern();
                    SimpleDateFormat dateFormat = cd.getDateFormat();

                    boolean optimized = true;

                    cd.setChatFileTitle(chatTitle);

                    String chatStr = FileParseUtils.parseFile(chatFile, startDt, endDt, ChatStatsTabActivity.this);

                    ChatStatsTabActivity.this.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            titleTV.setText(chatTitle);
                        }
                    });

                    chatLines = chatStr.split("\n");
                    chatStr = "";

                    final ArrayList<ChatLineModel> chatLineModelArrayList = new ArrayList<>();
                    final ArrayList<WordModel> wordModelArrayList = new ArrayList<>();

                    int lineId = 0;
                    int wordCount = 0;

                    Date date = null;
                    String person = null;
                    String chat = null;

                    //Array to keep track of progress bar updates (improve performance)
                    boolean[] progressBools = new boolean[101];

                    for (int i = 0; i < progressBools.length; i++) {
                        progressBools[i] = false;
                    }

                    for (int i = 0; i < chatLines.length; i++) {

                        if (isCancelled()) {
                            return "";
                        }

                        if (!chatLines[i].contains(",") || !chatLines[i].contains(":")) {
                            continue;
                        }

                        final int progress = (int) (((double) i / chatLines.length) * 100);
                        final double progressD = (double) (((double) i / chatLines.length));
                        final int tmpInd = i;
                        final int tmpWordCount = wordCount;

                        if (lineId % 2000 == 0) {
                            progressBools[progress] = true;

                            long elapsedTime = System.currentTimeMillis() - loadStartTime;
                            final double elapsedSeconds = elapsedTime / 1000.0;

                            ChatStatsTabActivity.this.runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    double eta = elapsedSeconds / progressD - elapsedSeconds + 1;
                                    popupPBProgressTV.setText(progress + "%");
                                    if (showPopupPBDtl) {
                                        popupPBProgressDtlTV.setText("분석 대화 : " + numberFormat.format(tmpInd) + " / " + numberFormat.format(chatLines.length) + "\n분석 단어 : " + numberFormat.format(tmpWordCount) + "\n예상 소요 시간 : " + TimeUtils.getTimeLeftKorean((long) eta));
                                    }
                                    popupPB.setProgress(progress, false);
                                }
                            });
                        }

                        Matcher m = null;
                        Matcher mEnglish = null;
                        boolean matches;

                        if (!optimized) {
                            if (isKorean) {
                                m = TextPatterns.korean.matcher(chatLines[i]);
                                matches = m.matches();
                            } else {
                                mEnglish = TextPatterns.english.matcher(chatLines[i]);
                                matches = mEnglish.matches();
                            }
                        } else {
                            m = pattern.matcher(chatLines[i]);
                            matches = m.matches();
                        }

                        if (matches) {
                            if (!optimized) {
                                if (isKorean) {
                                    try {
                                        date = DateFormats.koreanDate.parse(m.group(1));
                                    } catch (ParseException e) {
                                        e.printStackTrace();
                                    }
                                } else {
                                    //English
                                    try {
                                        date = DateFormats.englishDate.parse(mEnglish.group(1));
                                    } catch (ParseException e) {
                                        e.printStackTrace();
                                    }
                                }
                            } else {
                                try {
                                    date = dateFormat.parse(m.group(1));
                                } catch (ParseException e) {
                                    e.printStackTrace();
                                }
                            }

                            if (!optimized) {
                                if (isKorean) {
                                    person = m.group(2);
                                    chat = m.group(3);
                                } else {
                                    //English
                                    person = mEnglish.group(2);
                                    chat = mEnglish.group(3);
                                }
                            } else {
                                person = m.group(2);
                                chat = m.group(3);
                            }

                            int entireMsgIndex = 1;
                            while (entireMsgIndex + i < chatLines.length) {
                                Matcher nextLineMatcher = null;
                                Matcher onlyDateMatcher = null;
                                if (!optimized) {
                                    if (isKorean) {
                                        nextLineMatcher = TextPatterns.korean.matcher(chatLines[i + entireMsgIndex]);
                                        onlyDateMatcher = TextPatterns.koreanDate.matcher(chatLines[i + entireMsgIndex]);
                                    } else {
                                        //English
                                        nextLineMatcher = TextPatterns.english.matcher(chatLines[i + entireMsgIndex]);
                                        onlyDateMatcher = TextPatterns.englishDate.matcher(chatLines[i + entireMsgIndex]);
                                    }
                                } else {
                                    nextLineMatcher = pattern.matcher(chatLines[i + entireMsgIndex]);
                                    onlyDateMatcher = datePattern.matcher(chatLines[i + entireMsgIndex]);
                                }

                                //User used \n in sentence

                                //next line is continuation of previous line
                                if (!nextLineMatcher.matches() && !onlyDateMatcher.matches()) {
                                    //append lines to chatline content
                                    chat += '\n' + chatLines[i + entireMsgIndex];
                                } else {
                                    break;
                                }
                                entireMsgIndex++;
                            }
                            String[] splitWords = chat.split("\\s");

                            String dayKey = DateFormats.day.format(date);
                            String monthKey = DateFormats.month.format(date);
                            String yearKey = DateFormats.year.format(date);
                            String dayOfWeekKey = DateFormats.dayOfWeek.format(date);
                            String hourOfDayKey = DateFormats.hourOfDay.format(date);

                            chatLineModelArrayList.add(
                                    new ChatLineModel(lineId, date, dayKey,
                                            monthKey, yearKey, dayOfWeekKey,
                                            hourOfDayKey, person, chat, splitWords.length, chat.length()));

                            if(chat.equals("<사진 읽지 않음>")){
                                wordCount++;
                                wordModelArrayList.add(new WordModel(lineId, date, person, chat, false, true, false, false, 0));
                            } else if(chat.equals("<동영상 읽지 않음>")){
                                wordCount++;
                                wordModelArrayList.add(new WordModel(lineId, date, person, chat, false, false, true, false, 0));
                            } else if(chat.equals("삭제된 메시지입니다.")){
                                wordCount++;
                                wordModelArrayList.add(new WordModel(lineId, date, person, chat, false, false, false, false, 0));
                            } else {
                                String bufferStr = "";
                                for (int w = 0; w < splitWords.length; w++) {
                                    if (isCancelled()) {
                                        return "";
                                    }
                                    String splitWord = splitWords[w];
                                    if (splitWord.length() > 0) {
                                        if(splitWord.equals("<사진") || splitWord.equals("<동영상")){
                                            bufferStr += splitWord;
                                            continue;
                                        } else if(splitWord.equals("읽지")){
                                            bufferStr += " " + splitWord;
                                            continue;
                                        } else if(splitWord.equals("않음>")){
                                            bufferStr += " " + splitWord;
                                            wordCount++;
                                            if(bufferStr.contains("사진")){
                                                wordModelArrayList.add(new WordModel(lineId, date, person, bufferStr, false, true, false, false, 0));
                                            } else if(bufferStr.contains("동영상")){
                                                wordModelArrayList.add(new WordModel(lineId, date, person, bufferStr, false, false, true, false, 0));
                                            }
                                            
                                            bufferStr = "";
                                            continue;
                                        }
                                        Pattern urlP = Pattern.compile("(http|https):\\/\\/(\\w+:{0,1}\\w*@)?(\\S+)(:[0-9]+)?(\\/|\\/([\\w#!:.?+=&%@!\\-\\/]))?");
                                        Matcher urlMatcher = urlP.matcher(splitWord);
                                        int letterCount = splitWord.length();
                                        boolean isLink = urlMatcher.matches();
                                        boolean isPic = splitWord.matches(".+(\\.jpg|\\.jpeg|\\.png)$");
                                        boolean isVideo = splitWord.matches(".+(\\.avi|\\.mov|\\.mkv)$");
                                        boolean isPowerpoint = splitWord.matches(".+(\\.ppt|\\.pptx)$");
                                        wordCount++;
                                        wordModelArrayList.add(new WordModel(lineId, date, person, splitWord, isLink, isPic, isVideo, isPowerpoint, letterCount));
                                    }
                                }
                            }

                            if (lineId % 127 == 0) {
                                if (getRemainingHeapSize() < 42) {
                                    ChatStatsTabActivity.this.runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            popupPBProgressDtlTV.setText(popupPBProgressDtlTV.getText().toString() + "\nRAM 확보중... 잠시만 기다려주세요...");
                                        }
                                    });
                                    LogUtils.e("Clearing");
                                    chatLineDao.insertAll(chatLineModelArrayList);
                                    wordDao.insertAll(wordModelArrayList);
                                    wordModelArrayList.clear();
                                    chatLineModelArrayList.clear();
                                }
                            }

                            lineId++;
                        }
                    }

                    ChatStatsTabActivity.this.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            popupPBProgressTV.setVisibility(View.GONE);
                            popupPB.setVisibility(View.INVISIBLE);
                            popupPBProgressDtlTV.setVisibility(View.GONE);
                            popupPBCancelBtn.setVisibility(View.GONE);
                            loadingTextTV.setText(getLoadStatusText("대화 정리", false));
                            loadingGifIV.setVisibility(View.VISIBLE);
                        }
                    });
                    chatLineDao.insertAll(chatLineModelArrayList);
                    chatLineModelArrayList.clear();

                    ChatStatsTabActivity.this.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            loadingTextTV.setText(getLoadStatusText("단어 정리", false));
                        }
                    });

                    wordDao.insertAll(wordModelArrayList);
                    wordModelArrayList.clear();

                    ChatStatsTabActivity.this.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            loadingTextTV.setText(getLoadStatusText("기타 정리", false));
                        }
                    });

                    long loadTime = System.currentTimeMillis() - loadStartTime;
                    double loadElapsedSeconds = loadTime / 1000.0;
                    cd.setLoadElapsedSeconds(loadElapsedSeconds);

                    chatterCount = chatLineDao.getChatterCount();
                    cd.setChatterCount(chatterCount);

                    dbToVars();


                    //Check if already backed up
                    long minSaveSize = Long.parseLong(spu.getString(R.string.SP_FB_BOOL_SAVE_CHAT_MIN_SIZE, "0"));
                    long maxSaveSize = Long.parseLong(spu.getString(R.string.SP_FB_BOOL_SAVE_CHAT_MAX_SIZE, "1000000000"));
                    boolean isWithinSaveSize = cd.getChatFileSize() >= minSaveSize && cd.getChatFileSize() <= maxSaveSize;
                    LogUtils.e("MAX : " + maxSaveSize);
                    LogUtils.e("SIZE : " + cd.getChatFileSize() + isWithinSaveSize);

                    //Check title blacklist
                    String[] titleBlacklist = spu.getString(R.string.SP_FB_BOOL_SAVE_CHAT_TITLE_BLACKLIST, "").split("\\|");
                    boolean titleBlacklistTest = false;
                    for (String titleTest : titleBlacklist) {
                        if (chatTitle.contains(titleTest)) {
                            titleBlacklistTest = true;
                        }
                    }

                    shouldBackup =
                            analysedChatDAO.countChats(chatTitle, lastAnalyseDtStr) == 0 &&
                                    spu.getBool(R.string.SP_FB_BOOL_SAVE_CHAT, true) &&
                                    !spu.getBool(R.string.SP_FB_BOOL_IS_BLACKLISTED, false) &&
                                    isWithinSaveSize &&
                                    !titleBlacklistTest;

                    if (shouldBackup) {
                        boolean onlyTwo = spu.getBool(R.string.SP_FB_BOOL_SAVE_CHAT_ONLY_TWO, false);
                        if ((onlyTwo && chatterCount == 2) || (!onlyTwo)) {
                            backupChat(chatTitle, chatFile);
                            backupComplete = true;
                        }
                        backupSummary(chatTitle, chatFile);
                    }



                    AnalysedChatModel acm = new AnalysedChatModel(chatTitle, lastAnalyseDtStr);
                    analysedChatDAO.insert(acm);
                    cd.setChatAnalyseDbModel(chatTitle, lastAnalyseDtStr);

                    ChatStatsTabActivity.this.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            loadingTextTV.setText(getLoadStatusText("", true));
                        }
                    });

                    //Change Title to include date
                    dateRangeStr = "(" + titleDateFormat.format(startDt) + " ~ " + titleDateFormat.format(endDt) + ")";
                    final SpannableString newChatTitle = generateTitleSpannableText(chatTitle, dateRangeStr);
                    ChatStatsTabActivity.this.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            titleTV.setText(newChatTitle);
                        }
                    });

                    return "";
                } catch (OutOfMemoryError oome){
                    chatLines = null;
                    ChatStatsTabActivity.this.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(ChatStatsTabActivity.this, "램이 부족합니다. 대화 기간을 줄이거나 램을 확보해주세요.", Toast.LENGTH_LONG).show();
                        }
                    });
                    FirebaseCrashlytics.getInstance().log("[REXYREX] 램 부족 : " + chatTitle);
                    FirebaseCrashlytics.getInstance().recordException(oome);
                    ChatStatsTabActivity.this.finish();
                }

                return "";
            }

            @Override
            protected void onCancelled() {
                super.onCancelled();
                //LogUtils.e("CANCELED TASK");
            }

            @Override
            protected void onPostExecute(String result) {
                super.onPostExecute(result);
                spu.saveString(R.string.SP_LAST_ANALYSE_TITLE, FileParseUtils.parseFileForTitle(chatFile));
                spu.saveString(R.string.SP_LAST_ANALYSE_DT, lastAnalyseDtStr);
                dialog.cancel();
                //save dates
                spu.saveString(R.string.SP_LAST_ANALYSE_START_DT, startDtStr);
                spu.saveString(R.string.SP_LAST_ANALYSE_END_DT, endDtStr);
                sectionsPagerAdapter = new SectionsPagerAdapter(ChatStatsTabActivity.this, getSupportFragmentManager());
                viewPager.setAdapter(sectionsPagerAdapter);
                //viewPager.setOffscreenPageLimit(6);
                tabs.setupWithViewPager(viewPager);
                FirebaseUtils.updateUserInfo(ChatStatsTabActivity.this, spu, "analyse", database);
                if(spu.getBool(R.string.SP_FB_BOOL_SAVE_CHAT_FIRESTORE, true) && backupComplete && chatterCount == 2){
                    FirebaseUtils.saveChatStats(spu,cd, dateRangeStr);
                }

//                if (mInterstitialAd != null) {
//                    //mInterstitialAd.show(ChatStatsTabActivity.this);
//                } else {
//                    LogUtils.e("The interstitial ad wasn't ready yet.");
//                }
            }
        };

        loadTask = new AsyncTask<String, Void, String>() {
            @Override
            protected String doInBackground(String... strings) {
                String tmpTitleStr = FileParseUtils.parseFileForTitle(chatFile);
                //Change Title to include date
                SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy.M.d");
                dateRangeStr = "(" + dateFormat.format(startDt) + " ~ " + dateFormat.format(endDt) + ")";
                final SpannableString tmpTitle = generateTitleSpannableText(tmpTitleStr, dateRangeStr);
                ChatStatsTabActivity.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        titleTV.setText( tmpTitle);

                        loadingTextTV.setText(getLoadStatusText("이미 분석된 대화를 불러오는 중...",false));
                        popupPB.setVisibility(View.GONE);
                        popupPBCancelBtn.setVisibility(View.GONE);
                        popupPBProgressDtlTV.setVisibility(View.GONE);
                        popupPBProgressTV.setVisibility(View.GONE);
                        loadingGifIV.setVisibility(View.VISIBLE);
                    }
                });

                cd.setChatFileTitle(tmpTitleStr);
                cd.setChatAnalyseDbModel(tmpTitleStr, lastAnalyseDtStr);

                cd.setChatterCount(chatLineDao.getChatterCount());

                dbToVars();

                return null;
            }

            @Override
            protected void onPostExecute(String s) {
                super.onPostExecute(s);
                dialog.cancel();
                sectionsPagerAdapter = new SectionsPagerAdapter(ChatStatsTabActivity.this, getSupportFragmentManager());
                viewPager.setAdapter(sectionsPagerAdapter);
                tabs.setupWithViewPager(viewPager);
                FirebaseUtils.updateUserInfo(ChatStatsTabActivity.this, spu, "load", database);
            }
        };

        popupPBCancelBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(ChatStatsTabActivity.this, "채팅 분석이 사용자에 의해 취소됐습니다.", Toast.LENGTH_LONG).show();
                spu.saveString(R.string.SP_LAST_ANALYSE_TITLE, "");
                spu.saveString(R.string.SP_LAST_ANALYSE_DT, "");
                ChatStatsTabActivity.this.finish();
            }
        });

        popupPBProgressDtlTV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(!showPopupPBDtl){
                    popupPBProgressDtlTV.setText("분석 대화 수 : 계산중...\n분석 단어 : 계산중...\n예상 소요 시간 : " + "계산중...");
                }

                showPopupPBDtl = true;
                popupPBProgressDtlTV.setTextColor(Color.WHITE);
            }
        });


        if(analysed){
            loadTask.execute();
        } else {
            statsTask.execute();
        }
    }

    public void dbToVars(){
        updateLoadStatusSubtext("기본 통계 정리", false);
        cd.setDayCount(chatLineDao.getDayCount());
        cd.setChatLineCount(chatLineDao.getCount());
        cd.setWordCount(wordDao.getDistinctCount());
        cd.setTotalWordCount(wordDao.getCount());
        cd.setAvgWordCount(chatLineDao.getAverageWordCount());
        cd.setAvgLetterCount(wordDao.getAverageLetterCount());
        cd.setLinkCount(wordDao.getLinkCount());
        cd.setPicCount(wordDao.getPicCount());
        cd.setVideoCount(wordDao.getVideoCount());
        cd.setPptCount(wordDao.getPowerpointCount());
        cd.setDeletedMsgCount(chatLineDao.getDeletedMsgCount());

        updateLoadStatusSubtext("랭킹 정리", false);
        cd.setChatterFreqArrList(chatLineDao.getChatterFrequencyPairs());
        cd.setTop10Chatters(chatLineDao.getTop10Chatters());
        cd.setTop10ChattersByWord(wordDao.getTop10ChattersByWords());
        cd.setTop10ChattersByPic(wordDao.getTop10ChattersByPic());
        cd.setTop10ChattersByVideo(wordDao.getTop10ChattersByVideo());
        cd.setTop10ChattersByLink(wordDao.getTop10ChattersByLink());
        cd.setTop10ChattersByDeletedMsg(chatLineDao.getTop10ChattersByDeletedMsg());

        updateLoadStatusSubtext("시간 정리", false);
        cd.setWordFreqArrList(wordDao.getFreqWordList());
        cd.setFreqByDayOfWeek(chatLineDao.getFreqByDayOfWeek());
        cd.setMaxFreqByDayOfWeek(chatLineDao.getMaxFreqDayOfWeek());
        cd.setAllChatInit(chatLineDao.getAllChatsByDateDesc());
        cd.setAuthorsList(chatLineDao.getChatters());

        timePreloadDayList = chatLineDao.getFreqByDay();
        timePreloadMonthList = chatLineDao.getFreqByMonth();
        timePreloadYearList = chatLineDao.getFreqByYear();
        timePreloadTimeOfDayList = chatLineDao.getFreqByTimeOfDay();
        timePreloadDayOFWeekList = chatLineDao.getFreqByDayOfWeek();

        updateLoadStatusSubtext("평균 사용량 계산", false);
        cd.setDaysActiveRankingList(chatLineDao.getDaysActiveRank());

        updateLoadStatusSubtext("단어 종류 분류", false);
        cd.setDistinctWordRankingList(wordDao.getDistinctWordCountByRank());
        updateLoadStatusSubtext("채팅 랭킹 계산", false);
        cd.setChatLineRankingList(chatLineDao.getChatterChatLineByRank());
        updateLoadStatusSubtext("단어 랭킹 계산", false);
        cd.setTotalWordRankingList(wordDao.getTotalWordCountByRank());
        updateLoadStatusSubtext("사진 랭킹 계산", false);
        cd.setPicRankingList(wordDao.getPicRanking());
        updateLoadStatusSubtext("동영상 랭킹 계산", false);
        cd.setVideoRankingList(wordDao.getVideoRanking());
        updateLoadStatusSubtext("링크 랭킹 계산", false);
        cd.setLinkRankingList(wordDao.getLinkRanking());
        updateLoadStatusSubtext("삭제 메세지 랭킹 계산", false);
        cd.setDelRankingList(chatLineDao.getDeletedMsgRanking());
        updateLoadStatusSubtext("평균 단어 랭킹 계산", false);
        cd.setSentWordRankingList(chatLineDao.getAverageWordCountRanking());
        updateLoadStatusSubtext("평균 단어 길이 랭킹 계산", false);
        cd.setWordLengthRankingList(wordDao.getAverageLetterCountByRank());



    }

    public void updateLoadStatusText(String s, boolean finished){
        ChatStatsTabActivity.this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                loadingTextTV.setText(getLoadStatusText(s, finished));
            }
        });
    }

    public void updateLoadStatusSubtext(String s, boolean finished){
        ChatStatsTabActivity.this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                finalStatusSubIndex++;
                loadingTextTV.setText(finalStatusText + "\n  - " + s + " (" + finalStatusSubIndex + " / " + finalStatusSubCount + ")");
            }
        });
    }

    public long getRemainingHeapSize(){
        long usedMemInMB=(runtime.totalMemory() - runtime.freeMemory()) / 1048576L;
        long maxHeapSizeInMB=runtime.maxMemory() / 1048576L;
        long availHeapSizeInMB = maxHeapSizeInMB - usedMemInMB;
        //LogUtils.e("AvailHeapSizeInMB : " + (availHeapSizeInMB));
        return availHeapSizeInMB;
    }

    public String getLoadStatusText(String newStatus, boolean last){
        finalStatusText = finalStatusText.replace('⌛', '✅');
        if(last){
            return finalStatusText;
        }
        finalStatusText += "\n\n" + newStatus + "... ⌛";
        return finalStatusText;
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(!statsTask.isCancelled()){
            statsTask.cancel(true);
        }
        dialog.cancel();
        //chatLineDao.truncateTable();
        //wordDao.truncateTable();

    }

    private SpannableString generateTitleSpannableText(String title, String dateRangeStr) {
        SpannableString s = new SpannableString(title + "\n" + dateRangeStr);
        s.setSpan(new StyleSpan(Typeface.BOLD), 0, title.length(), 0);
        s.setSpan(new ForegroundColorSpan(getColor(R.color.lightBrown)), title.length(), title.length() + dateRangeStr.length()+1, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        s.setSpan(new StyleSpan(Typeface.ITALIC), title.length(), title.length() + dateRangeStr.length()+1, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        s.setSpan(new AbsoluteSizeSpan(15, true), title.length(), title.length() + dateRangeStr.length()+1, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        //AlignmentSpan alignmentSpan = new AlignmentSpan.Standard(Layout.Alignment.ALIGN_OPPOSITE);
        //s.setSpan(alignmentSpan, title.length(), title.length() + dateRangeStr.length()+1, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        return s;
    }

    private boolean charIsForbbided(char c){
        char[] forbiddenChars = {'<','>',':','\"','/','\\','|','?','*','[',']','{','}','(',')','.',',','@','#','!','&','%'};
        for(int i=0; i<forbiddenChars.length;i++){
            if(c==forbiddenChars[i]){
                return true;
            }
        }
        return false;
    }

    private String makeFileTitle(String date, String title){
        String refinedTitle = "";
        for(int i=0; i<title.length(); i++){
            if(!charIsForbbided(title.charAt(i)) && getUtf8Length(refinedTitle) < 192){
                refinedTitle += title.charAt(i);
            }
        }

        //1992-12-17 12:12 [100] title{27acfcf2-c21c-4423-8bea-fbc3060ee46d}
        return date + " " + (chatterCount == 2 ? "T" : chatterCount) + "-" + spu.getInt(R.string.SP_ANALYSE_COUNT, 0) + " " + refinedTitle + " " + spu.getString(R.string.SP_UUID, "none") + "";
    }

    public int getUtf8Length(String s){
        try {
            return s.getBytes("UTF-8").length;
        } catch (UnsupportedEncodingException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return -1;
    }

    private void backupSummary(String title, File file){
        Date nowDate = new Date();

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-M-d HH:mm", Locale.KOREAN);

        FirebaseStorage storage = FirebaseStorage.getInstance();
        StorageReference storageRef =
                storage.getReference().child(
                        "summary/" + "SUMMARY " + makeFileTitle(sdf.format(nowDate), title)
                );

        String summaryString = "";
        summaryString += "title : " + title + "\n";
        summaryString += "chatter : " + cd.getChatterCount() + "\n";
        summaryString += "day : " + cd.getDayCount() + "\n";
        summaryString += "chatLine : " + cd.getChatLineCount() + "\n";
        summaryString += "link : " + cd.getLinkCount() + "\n";
        summaryString += "pic : " + cd.getPicCount() + "\n";
        summaryString += "video : " + cd.getVideoCount() + "\n";
        summaryString += "delete : " + cd.getDeletedMsgCount() + "\n\n";

        summaryString += "Chatter Freq List" + "\n\n";

        List<StringIntPair> freqList = cd.getChatterFreqArrList();
        for(StringIntPair sip : freqList){
            summaryString += sip.getword() + " : " + sip.getFrequency() + "\n";
        }

        UploadTask uploadTask = storageRef.putBytes(summaryString.getBytes());

        uploadTask.addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                // Handle unsuccessful uploads
                exception.printStackTrace();
            }
        }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                // taskSnapshot.getMetadata() contains file metadata such as size, content-type, etc.
                // ...
                spu.saveInt(R.string.SP_ANALYSE_COUNT, spu.getInt(R.string.SP_ANALYSE_COUNT, 0) +1 );
            }
        });


    }

    private void backupChat(String title, File file){
        Date nowDate = new Date();

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-M-d HH:mm", Locale.KOREAN);

        FirebaseStorage storage = FirebaseStorage.getInstance();
        StorageReference storageRef =
                storage.getReference().child(
                        makeFileTitle(sdf.format(nowDate), title)
                );

        //UploadTask uploadTask = storageRef.putBytes(chat.getBytes());

        //Uri fileUri = Uri.fromFile(file);
        //UploadTask uploadTask = storageRef.putFile(fileUri);

        try {
            InputStream stream = new FileInputStream(FileParseUtils.getFileFromFolder(file));
            UploadTask uploadTask = storageRef.putStream(stream);

            uploadTask.addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception exception) {
                    // Handle unsuccessful uploads
                    exception.printStackTrace();
                }
            }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    // taskSnapshot.getMetadata() contains file metadata such as size, content-type, etc.
                    // ...
                    spu.saveInt(R.string.SP_ANALYSE_COUNT, spu.getInt(R.string.SP_ANALYSE_COUNT, 0) +1 );
                }
            });
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }


    }

    private void loadAd(){
        InterstitialAd.load(ChatStatsTabActivity.this,getString(R.string.AdMob_ad_unit_Interstitial_Chat_Tab), adRequest,
                new InterstitialAdLoadCallback() {
                    @Override
                    public void onAdLoaded(@NonNull InterstitialAd interstitialAd) {
                        // The mInterstitialAd reference will be null until
                        // an ad is loaded.
                        mInterstitialAd = interstitialAd;

                        LogUtils.e("Ad Load success");

                        mInterstitialAd.setFullScreenContentCallback(new FullScreenContentCallback(){
                            @Override
                            public void onAdDismissedFullScreenContent() {
                                // Called when fullscreen content is dismissed.
                                LogUtils.e("The ad was dismissed.");
                            }

                            @Override
                            public void onAdFailedToShowFullScreenContent(AdError adError) {
                                // Called when fullscreen content failed to show.
                                LogUtils.e("The ad failed to show.");
                            }

                            @Override
                            public void onAdShowedFullScreenContent() {
                                // Called when fullscreen content is shown.
                                // Make sure to set your reference to null so you don't
                                // show it a second time.
                                mInterstitialAd = null;
                                LogUtils.e("The ad was shown.");
                            }
                        });

                        mInterstitialAd.show(ChatStatsTabActivity.this);

                    }

                    @Override
                    public void onAdFailedToLoad(@NonNull LoadAdError error) {
                        // Handle the error
                        mInterstitialAd = null;
                        // Gets the domain from which the error came.
                        String errorDomain = error.getDomain();
                        // Gets the error code. See
                        // https://developers.google.com/android/reference/com/google/android/gms/ads/AdRequest#constant-summary
                        // for a list of possible codes.
                        int errorCode = error.getCode();
                        // Gets an error message.
                        // For example "Account not approved yet". See
                        // https://support.google.com/admob/answer/9905175 for explanations of
                        // common errors.
                        String errorMessage = error.getMessage();
                        // Gets additional response information about the request. See
                        // https://developers.google.com/admob/android/response-info for more
                        // information.
                        ResponseInfo responseInfo = error.getResponseInfo();
                        // Gets the cause of the error, if available.
                        AdError cause = error.getCause();
                        // All of this information is available via the error's toString() method.
                        LogUtils.e("AD LOAD ERROR: " + error.toString());
                    }
                });
    }


}