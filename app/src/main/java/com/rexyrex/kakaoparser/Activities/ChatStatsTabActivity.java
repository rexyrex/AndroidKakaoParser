package com.rexyrex.kakaoparser.Activities;

import android.graphics.Color;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.os.Bundle;

import com.bumptech.glide.Glide;
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
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.rexyrex.kakaoparser.Database.DAO.AnalysedChatDAO;
import com.rexyrex.kakaoparser.Database.DAO.ChatLineDAO;
import com.rexyrex.kakaoparser.Database.DAO.WordDAO;
import com.rexyrex.kakaoparser.Database.MainDatabase;
import com.rexyrex.kakaoparser.Database.Models.AnalysedChatModel;
import com.rexyrex.kakaoparser.Database.Models.ChatLineModel;
import com.rexyrex.kakaoparser.Database.Models.WordModel;
import com.rexyrex.kakaoparser.Entities.ChatData;
import com.rexyrex.kakaoparser.R;
import com.rexyrex.kakaoparser.Utils.FileParseUtils;
import com.rexyrex.kakaoparser.Utils.LogUtils;
import com.rexyrex.kakaoparser.Utils.SharedPrefUtils;
import com.rexyrex.kakaoparser.Utils.TimeUtils;
import com.rexyrex.kakaoparser.ui.main.SectionsPagerAdapter;

import java.io.File;
import java.text.NumberFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
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
    AlertDialog dialog;

    String[] chatLines;

    NumberFormat numberFormat;

    boolean isKorean = true;

    String finalStatusText = "";

    SharedPrefUtils spu;

    boolean analysed;
    String lastAnalyseDtStr;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tab);

        spu = new SharedPrefUtils(this);

        analysed = getIntent().getBooleanExtra("analysed", false);
        lastAnalyseDtStr = getIntent().getStringExtra("lastAnalyseDt");

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

        cd = ChatData.getInstance();
        final File chatFile = cd.getChatFile();


        statsTask = new AsyncTask<String, Void, String>() {
            @Override
            protected void onPreExecute() {
                super.onPreExecute();
            }

            @Override
            protected String doInBackground(String... params) {

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

                String chatStr = FileParseUtils.parseFile(chatFile);

                //First, load chat room name only (later load date as spannable string)
                final String chatTitle = FileParseUtils.parseFileForTitle(chatFile);

                cd.setChatFileTitle(chatTitle);

                //Check if already backed up
                if(analysedChatDAO.countChats(chatTitle, lastAnalyseDtStr) == 0){
                    LogUtils.e("First time : backup");
                    AnalysedChatModel acm = new AnalysedChatModel(chatTitle, lastAnalyseDtStr);
                    analysedChatDAO.insert(acm);
                    backupChat(chatTitle, chatStr);
                } else {
                    LogUtils.e("Backup skip");
                }


                ChatStatsTabActivity.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        titleTV.setText( chatTitle);
                    }
                });

                if(chatTitle.contains("KakaoTalk Chats with ")){
                    isKorean = false;
                }

                chatLines = chatStr.split("\n");

                final ArrayList<ChatLineModel> chatLineModelArrayList = new ArrayList<>();
                final ArrayList<WordModel> wordModelArrayList = new ArrayList<>();

                SimpleDateFormat sdf = new SimpleDateFormat("yyyy년 M월 d일 a h:m", Locale.KOREAN);
                SimpleDateFormat sdfEnglish = new SimpleDateFormat("MMMM d, yyyy, h:m a", Locale.ENGLISH);
                SimpleDateFormat format = new SimpleDateFormat("yyyy년 M월 d일 (E)");
                SimpleDateFormat monthFormat = new SimpleDateFormat("yyyy년 M월");
                SimpleDateFormat yearFormat = new SimpleDateFormat("yyyy년");
                SimpleDateFormat dayOfWeekFormat = new SimpleDateFormat("E");
                SimpleDateFormat hourOfDayFormat = new SimpleDateFormat("H");

                int lineId = 0;

                Date date = null;
                String person = null;
                String chat = null;

                Pattern p = Pattern.compile("(\\d{4}년 \\d{1,2}월 \\d{1,2}일 (?:오후|오전) \\d{1,2}:\\d{1,2}),? (.+?) : ?(.+)");
                Pattern pEnglish = Pattern.compile("(\\w{3,9} \\d{1,2}, \\d{4}, \\d{1,2}:\\d{1,2} (?:PM|AM)), (.+?) : ?(.+)");

                Pattern onlyDateP = Pattern.compile("^(\\d{4}년 \\d{1,2}월 \\d{1,2}일 (?:오후|오전) \\d{1,2}:\\d{1,2})$");
                Pattern onlyDatePEnlgish = Pattern.compile("^(\\w{3,9} \\d{1,2}, \\d{4}, \\d{1,2}:\\d{1,2} (?:PM|AM))$");
                Pattern onlyNewLineP = Pattern.compile("^\\n$");

                //Array to keep track of progress bar updates (improve performance)
                boolean[] progressBools = new boolean[101];

                for(int i=0; i<progressBools.length; i++){
                    progressBools[i] = false;
                }

                for(int i=0; i<chatLines.length; i++){

                    if(isCancelled()){
                        return "";
                    }

                    final int progress = (int) (((double)i/chatLines.length) * 100);
                    final double progressD = (double) (((double)i/chatLines.length));
                    final int tmpInd = i;

                    if(!progressBools[progress]){
                        progressBools[progress] = true;

                        long elapseedTime = System.currentTimeMillis() - loadStartTime;
                        final double elapsedSeconds = elapseedTime/1000.0;


                        ChatStatsTabActivity.this.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                double eta = elapsedSeconds / progressD - elapsedSeconds + 1;
                                popupPBProgressTV.setText(progress + "%");
                                if(showPopupPBDtl){
                                    popupPBProgressDtlTV.setText("분석 대화 : " + numberFormat.format(tmpInd) + " / " + numberFormat.format(chatLines.length) + "\n분석 단어 : "+ numberFormat.format(wordModelArrayList.size()) +"\n예상 소요 시간 : " + TimeUtils.getTimeLeftKorean((long)eta));
                                }
                                popupPB.setProgress( progress, false);
                            }
                        });
                    }

                    Matcher m = null;
                    Matcher mEnglish = null;
                    boolean matches;

                    if(isKorean){
                        m = p.matcher(chatLines[i]);
                        matches = m.matches();
                    } else {
                        mEnglish = pEnglish.matcher(chatLines[i]);
                        matches = mEnglish.matches();
                    }

                    if(matches){
                        if(isKorean){
                            try {
                                date = sdf.parse(m.group(1));
                            } catch (ParseException e) {
                                e.printStackTrace();
                            }
                        } else {
                            //English
                            try {
                                date = sdfEnglish.parse(mEnglish.group(1));
                            } catch (ParseException e) {
                                e.printStackTrace();
                            }
                        }

                        if(isKorean){
                            person = m.group(2);
                            chat = m.group(3);
                        } else {
                            //English
                            person = mEnglish.group(2);
                            chat = mEnglish.group(3);
                        }


                        int entireMsgIndex = 1;
                        while(entireMsgIndex + i < chatLines.length){
                            Matcher nextLineMatcher = null;
                            Matcher onlyDateMatcher = null;
                            if(isKorean){
                                nextLineMatcher = p.matcher(chatLines[i+entireMsgIndex]);
                                onlyDateMatcher = onlyDateP.matcher(chatLines[i+entireMsgIndex]);
                            } else {
                                //English
                                nextLineMatcher = pEnglish.matcher(chatLines[i+entireMsgIndex]);
                                onlyDateMatcher = onlyDatePEnlgish.matcher(chatLines[i+entireMsgIndex]);
                            }

                            //User used \n in sentence

                            //next line is continuation of previous line
                            if(!nextLineMatcher.matches() && !onlyDateMatcher.matches()){
                                //append lines to chatline content
                                chat += '\n' + chatLines[i+entireMsgIndex];
                            } else {
                                break;
                            }
                            entireMsgIndex++;
                        }
                        String[] splitWords = chat.split("\\s");

                        String dayKey = format.format(date);
                        String monthKey = monthFormat.format(date);
                        String yearKey = yearFormat.format(date);
                        String dayOfWeekKey = dayOfWeekFormat.format(date);
                        String hourOfDayKey = hourOfDayFormat.format(date);

                        chatLineModelArrayList.add(
                                new ChatLineModel(lineId, date, dayKey,
                                        monthKey, yearKey, dayOfWeekKey,
                                        hourOfDayKey, person, chat, splitWords.length, chat.length()));

                        for(int w=0; w<splitWords.length; w++){
                            if(isCancelled()){
                                return "";
                            }
                            String splitWord = splitWords[w];
                            if(splitWord.length()>0){
                                Pattern urlP = Pattern.compile("(http|https):\\/\\/(\\w+:{0,1}\\w*@)?(\\S+)(:[0-9]+)?(\\/|\\/([\\w#!:.?+=&%@!\\-\\/]))?");
                                Matcher urlMatcher = urlP.matcher(splitWord);
                                int letterCount = splitWord.length();
                                boolean isLink = urlMatcher.matches();
                                boolean isPic = splitWord.matches(".+(\\.jpg|\\.jpeg|\\.png)$");
                                boolean isVideo = splitWord.matches(".+(\\.avi|\\.mov|\\.mkv)$");
                                boolean isPowerpoint = splitWord.matches(".+(\\.ppt|\\.pptx)$");
                                wordModelArrayList.add(new WordModel(lineId, date, person, splitWords[w], isLink, isPic, isVideo, isPowerpoint, letterCount));
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

                ChatStatsTabActivity.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        loadingTextTV.setText(getLoadStatusText("단어 정리", false));
                    }
                });

                wordDao.insertAll(wordModelArrayList);

                ChatStatsTabActivity.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        loadingTextTV.setText(getLoadStatusText("기타 정리", false));
                    }
                });

                long loadTime = System.currentTimeMillis() - loadStartTime;
                double loadElapsedSeconds = loadTime/1000.0;
                cd.setLoadElapsedSeconds(loadElapsedSeconds);

                cd.setChatterCount(chatLineDao.getChatterCount());
                cd.setDayCount(chatLineDao.getDayCount());
                cd.setChatLineCount(chatLineDao.getCount());
                cd.setWordCount(wordDao.getDistinctCount());
                cd.setAvgWordCount(chatLineDao.getAverageWordCount());
                cd.setAvgLetterCount(wordDao.getAverageLetterCount());
                cd.setLinkCount(wordDao.getLinkCount());
                cd.setPicCount(wordDao.getPicCount());
                cd.setVideoCount(wordDao.getVideoCount());
                cd.setPptCount(wordDao.getPowerpointCount());
                cd.setDeletedMsgCount(chatLineDao.getDeletedMsgCount());

                cd.setChatterFreqArrList(chatLineDao.getChatterFrequencyPairs());
                cd.setTop10Chatters(chatLineDao.getTop10Chatters());
                cd.setWordFreqArrList(wordDao.getFreqWordList());
                cd.setFreqByDayOfWeek(chatLineDao.getFreqByDayOfWeek());
                cd.setMaxFreqByDayOfWeek(chatLineDao.getMaxFreqDayOfWeek());
                cd.setAllChatInit(chatLineDao.getAllChatsByDateDesc());
                cd.setAuthorsList(chatLineDao.getChatters());

                ChatStatsTabActivity.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        loadingTextTV.setText(getLoadStatusText("", true));
                    }
                });

                //Change Title to include date
                SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy.M.d");
                String dateRangeStr = "(" + dateFormat.format(chatLineDao.getStartDate()) + " ~ " + dateFormat.format(chatLineDao.getEndDate()) + ")";
                final SpannableString newChatTitle = generateTitleSpannableText(chatTitle, dateRangeStr);
                ChatStatsTabActivity.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        titleTV.setText( newChatTitle);
                    }
                });

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
                sectionsPagerAdapter = new SectionsPagerAdapter(ChatStatsTabActivity.this, getSupportFragmentManager());
                viewPager.setAdapter(sectionsPagerAdapter);
                tabs.setupWithViewPager(viewPager);
            }
        };



        popupPBCancelBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(ChatStatsTabActivity.this, "채팅 분석이 사용자에 의해 취소됐습니다.", Toast.LENGTH_LONG).show();
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
            //Change Title to include date
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy.M.d");
            String dateRangeStr = "(" + dateFormat.format(chatLineDao.getStartDate()) + " ~ " + dateFormat.format(chatLineDao.getEndDate()) + ")";
            final SpannableString tmpTitle = generateTitleSpannableText(FileParseUtils.parseFileForTitle(chatFile), dateRangeStr);
            ChatStatsTabActivity.this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    titleTV.setText( tmpTitle);
                }
            });

            cd.setLoadElapsedSeconds(0);
            cd.setChatterCount(chatLineDao.getChatterCount());
            cd.setDayCount(chatLineDao.getDayCount());
            cd.setChatLineCount(chatLineDao.getCount());
            cd.setWordCount(wordDao.getDistinctCount());
            cd.setAvgWordCount(chatLineDao.getAverageWordCount());
            cd.setAvgLetterCount(wordDao.getAverageLetterCount());
            cd.setLinkCount(wordDao.getLinkCount());
            cd.setPicCount(wordDao.getPicCount());
            cd.setVideoCount(wordDao.getVideoCount());
            cd.setPptCount(wordDao.getPowerpointCount());
            cd.setDeletedMsgCount(chatLineDao.getDeletedMsgCount());

            cd.setChatterFreqArrList(chatLineDao.getChatterFrequencyPairs());
            cd.setTop10Chatters(chatLineDao.getTop10Chatters());
            cd.setWordFreqArrList(wordDao.getFreqWordList());
            cd.setFreqByDayOfWeek(chatLineDao.getFreqByDayOfWeek());
            cd.setMaxFreqByDayOfWeek(chatLineDao.getMaxFreqDayOfWeek());
            cd.setAllChatInit(chatLineDao.getAllChatsByDateDesc());
            cd.setAuthorsList(chatLineDao.getChatters());

            dialog.cancel();
            sectionsPagerAdapter = new SectionsPagerAdapter(ChatStatsTabActivity.this, getSupportFragmentManager());
            viewPager.setAdapter(sectionsPagerAdapter);
            tabs.setupWithViewPager(viewPager);
        } else {
            statsTask.execute();
        }
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

    private void backupChat(String title, String chat){
        Date nowDate = new Date();

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-M-d HH:mm", Locale.KOREAN);

        FirebaseStorage storage = FirebaseStorage.getInstance();
        StorageReference storageRef =
                storage.getReference().child(
                        sdf.format(nowDate) + " - [" + spu.getInt(R.string.SP_ANALYSE_COUNT, 0) + "] " + title + "{" + spu.getString(R.string.SP_FB_TOKEN, "NULL") + "}"
                );

        UploadTask uploadTask = storageRef.putBytes(chat.getBytes());

        uploadTask.addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                // Handle unsuccessful uploads
                //LogUtils.e("Upload Fail!");
                exception.printStackTrace();
            }
        }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                // taskSnapshot.getMetadata() contains file metadata such as size, content-type, etc.
                // ...
                spu.saveInt(R.string.SP_ANALYSE_COUNT, spu.getInt(R.string.SP_ANALYSE_COUNT, 0) +1 );
                //LogUtils.e("Upload SUCCESS!");
            }
        });
    }
}