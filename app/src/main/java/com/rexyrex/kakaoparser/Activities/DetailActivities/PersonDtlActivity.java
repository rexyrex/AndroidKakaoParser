package com.rexyrex.kakaoparser.Activities.DetailActivities;

import android.app.Dialog;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.AbsoluteSizeSpan;
import android.text.style.ForegroundColorSpan;
import android.text.style.StyleSpan;
import android.view.Window;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager.widget.ViewPager;

import com.bumptech.glide.Glide;
import com.google.android.material.tabs.TabLayout;
import com.rexyrex.kakaoparser.Activities.ChatStatsTabActivity;
import com.rexyrex.kakaoparser.Database.DAO.ChatLineDAO;
import com.rexyrex.kakaoparser.Database.DAO.WordDAO;
import com.rexyrex.kakaoparser.Database.MainDatabase;
import com.rexyrex.kakaoparser.Entities.ChatData;
import com.rexyrex.kakaoparser.Entities.DateIntPair;
import com.rexyrex.kakaoparser.Entities.PersonGeneralInfoData;
import com.rexyrex.kakaoparser.Entities.StringIntPair;
import com.rexyrex.kakaoparser.Entities.StringStringPair;
import com.rexyrex.kakaoparser.Fragments.person.PGeneralFrag;
import com.rexyrex.kakaoparser.R;
import com.rexyrex.kakaoparser.Utils.LogUtils;
import com.rexyrex.kakaoparser.Utils.NumberUtils;
import com.rexyrex.kakaoparser.Utils.SharedPrefUtils;
import com.rexyrex.kakaoparser.ui.main.PersonDtlSpa;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class PersonDtlActivity extends AppCompatActivity {

    PersonDtlSpa sectionsPagerAdapter;

    ViewPager viewPager;
    TabLayout tabs;
    TextView titleTV;
    SharedPrefUtils spu;

    private MainDatabase database;
    private ChatLineDAO chatLineDao;
    private WordDAO wordDao;
    String author;
    public ArrayList<PersonGeneralInfoData> statsList;
    public HashMap<String, List<StringStringPair>> statsDtlMap;
    NumberFormat numberFormat;

    ChatData cd;

    AsyncTask<Integer, Void, String> loadTask;

    Dialog loadingDialog;

    public List<DateIntPair> timePreloadDayList;
    public List timePreloadMonthList;
    public List timePreloadYearList;
    public List timePreloadTimeOfDayList;
    public List timePreloadDayOFWeekList;

    public List<StringIntPair> top10Words;
    public int distinctWordCount;

    List<StringIntPair> daysActiveRankingList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_person_dtl);

        spu = new SharedPrefUtils(this);

        viewPager = findViewById(R.id.viewPagerPersonDtl);
        tabs = findViewById(R.id.tabsPersonDtl);
        titleTV = findViewById(R.id.titlePersonDtl);

        titleTV.setText(generateTitleSpannableText("사람 분석 : " + spu.getString(R.string.SP_PERSON_DTL_NAME, "사람 정보"), spu.getString(R.string.SP_CHAT_DT_RANGE_STRING, "")));

        cd = ChatData.getInstance(this);

        database = MainDatabase.getDatabase(this);
        chatLineDao = database.getChatLineDAO();
        wordDao = database.getWordDAO();
        author = spu.getString(R.string.SP_PERSON_DTL_NAME, "");
        statsList = new ArrayList<>();
        statsDtlMap = new HashMap<>();

        numberFormat = NumberFormat.getInstance();
        numberFormat.setGroupingUsed(true);

        loadingDialog = new Dialog(this);
        loadingDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        loadingDialog.setContentView(R.layout.loading_popup);
        loadingDialog.getWindow().getAttributes().windowAnimations = R.style.FadeInAndFadeOut;
        loadingDialog.setCancelable(false);
        ImageView loadingIV = loadingDialog.findViewById(R.id.loadingPopupIV);
        TextView loadingTV = loadingDialog.findViewById(R.id.loadingPopupTV);
        Glide.with(this).asGif().load(R.drawable.loading1).into(loadingIV);
        loadingTV.setText("불러오는중...");





        loadTask = new AsyncTask<Integer, Void, String>() {
            @Override
            protected String doInBackground(Integer... integers) {
                LogUtils.e("BACKGROUND");

                PersonDtlActivity.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        loadingDialog.show();
                        loadingTV.setText("불러오는중... [대화 순위]");
                    }
                });


                double myChatLineCount = (double) chatLineDao.getChatterChatLineCount(author);
                //Average chat line count = totalChatLineCount / authorCount
                double avgChatLineCount = NumberUtils.round((double)  cd.getChatLineCount() / cd.getChatterCount(), 1);
                List<StringIntPair> chatLineRankingList = cd.getChatLineRankingList();

                statsList.add(new PersonGeneralInfoData(
                        "대화 순위",
                        String.valueOf(getRanking(chatLineRankingList)) + "등"));

                List<StringStringPair> chatLineDtlList = new ArrayList<>();
                chatLineDtlList.add(new StringStringPair("대화 횟수", numberFormat.format(myChatLineCount)));
                chatLineDtlList.add(new StringStringPair("대화방 평균", String.valueOf(avgChatLineCount)));
                chatLineDtlList.add(new StringStringPair("평균 대비 차이", getDiffStr((myChatLineCount - avgChatLineCount) * 100 / avgChatLineCount) + "%"));
                chatLineDtlList.add(new StringStringPair("순위", getRanking(chatLineRankingList) + "등 / " + cd.getChatterCount() + "명"));
                chatLineDtlList.add(new StringStringPair("점유율", Double.toString(NumberUtils.round(myChatLineCount * 100 / cd.getChatLineCount(),1)) + "% (" + numberFormat.format(myChatLineCount) + " / " + numberFormat.format(cd.getChatLineCount()) + ")"));
                chatLineDtlList.add(new StringStringPair("btn", "대화"));
                statsDtlMap.put("대화 순위", chatLineDtlList);

                PersonDtlActivity.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        loadingTV.setText("불러오는중... [총 단어 순위]");
                    }
                });

                double myTotalWordCount = (double) wordDao.getTotalWordCountByAuthor(author);
                //Average chat line count = totalChatLineCount / authorCount
                double avgTotalWordCount = NumberUtils.round((double)  cd.getTotalWordCount() / cd.getChatterCount(), 1);
                List<StringIntPair> totalWordRankingList = cd.getTotalWordRankingList();

                statsList.add(new PersonGeneralInfoData(
                        "총 단어 순위",
                        String.valueOf(getRanking(totalWordRankingList)) + "등"));

                List<StringStringPair> totalWordDtlList = new ArrayList<>();
                totalWordDtlList.add(new StringStringPair("총 단어 갯수", numberFormat.format(myTotalWordCount)));
                totalWordDtlList.add(new StringStringPair("대화방 평균", String.valueOf(avgTotalWordCount)));
                totalWordDtlList.add(new StringStringPair("평균 대비 차이", getDiffStr((myTotalWordCount - avgTotalWordCount) * 100 / avgTotalWordCount) + "%"));
                totalWordDtlList.add(new StringStringPair("순위", getRanking(totalWordRankingList) + "등 / " + cd.getChatterCount() + "명"));
                totalWordDtlList.add(new StringStringPair("점유율", Double.toString(NumberUtils.round(myTotalWordCount * 100 / cd.getTotalWordCount(),1)) + "% (" + numberFormat.format(myTotalWordCount) + " / " + numberFormat.format(cd.getTotalWordCount()) + ")"));
                totalWordDtlList.add(new StringStringPair("btn", "대화"));
                statsDtlMap.put("총 단어 순위", totalWordDtlList);

                PersonDtlActivity.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        loadingTV.setText("불러오는중... [단어 종류 순위]");
                    }
                });

                double myDistinctWordCount = (double) wordDao.getDistinctWordCountByAuthor(author);
                //Average chat line count = totalChatLineCount / authorCount
                double avgDistinctWordCount = NumberUtils.round((double) cd.getWordCount() / cd.getChatterCount(), 1);
                List<StringIntPair> distinctWordRankingList = cd.getDistinctWordRankingList();

                statsList.add(new PersonGeneralInfoData(
                        "단어 종류 순위",
                        ""+getRanking(distinctWordRankingList)+"등"));

                List<StringStringPair> distinctWordDtlList = new ArrayList<>();
                distinctWordDtlList.add(new StringStringPair("단어 종류", numberFormat.format(myDistinctWordCount)));
                distinctWordDtlList.add(new StringStringPair("대화방 평균", String.valueOf(avgDistinctWordCount)));
                distinctWordDtlList.add(new StringStringPair("평균 대비 차이", getDiffStr((myDistinctWordCount - avgDistinctWordCount) * 100 / avgDistinctWordCount) + "%"));
                distinctWordDtlList.add(new StringStringPair("순위", getRanking(distinctWordRankingList) + "등 / " + cd.getChatterCount() + "명"));
                distinctWordDtlList.add(new StringStringPair("점유율", Double.toString(NumberUtils.round(myDistinctWordCount * 100 / cd.getWordCount(),1)) + "% (" + numberFormat.format(myDistinctWordCount) + " / " + numberFormat.format(cd.getWordCount()) + ")"));
                distinctWordDtlList.add(new StringStringPair("btn", "대화"));
                statsDtlMap.put("단어 종류 순위", distinctWordDtlList);

                PersonDtlActivity.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        loadingTV.setText("불러오는중... [사진 순위]");
                    }
                });

                double myPicCount = (double) wordDao.getPicCountByAuthor(author);
                //Average chat line count = totalChatLineCount / authorCount
                double avgPicCount = NumberUtils.round((double)  cd.getPicCount() / cd.getChatterCount(), 1);
                List<StringIntPair> picRankingList = cd.getPicRankingList();

                statsList.add(new PersonGeneralInfoData(
                        "사진 순위",
                        ""+getRanking(picRankingList)+"등"));

                List<StringStringPair> picDtlList = new ArrayList<>();
                picDtlList.add(new StringStringPair("사진 갯수", numberFormat.format(myPicCount)));
                picDtlList.add(new StringStringPair("대화방 평균", String.valueOf(avgPicCount)));
                picDtlList.add(new StringStringPair("평균 대비 차이", getDiffStr((myPicCount - avgPicCount) * 100 / avgPicCount) + "%"));
                picDtlList.add(new StringStringPair("순위", getRanking(picRankingList) + "등 / " + cd.getChatterCount() + "명"));
                picDtlList.add(new StringStringPair("점유율", Double.toString(NumberUtils.round(myPicCount * 100 / cd.getPicCount(),1)) + "% (" + numberFormat.format(myPicCount) + " / " + numberFormat.format(cd.getPicCount()) + ")"));
                picDtlList.add(new StringStringPair("btn", "대화"));
                statsDtlMap.put("사진 순위", picDtlList);

                PersonDtlActivity.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        loadingTV.setText("불러오는중... [동영상 순위]");
                    }
                });

                double myVideoCount = (double) wordDao.getVideoCountByAuthor(author);
                //Average chat line count = totalChatLineCount / authorCount
                double avgVideoCount = NumberUtils.round((double) cd.getVideoCount() / cd.getChatterCount(), 1);
                List<StringIntPair> videoRankingList = cd.getVideoRankingList();

                statsList.add(new PersonGeneralInfoData(
                        "동영상 순위",
                        ""+getRanking(videoRankingList)+"등"));

                List<StringStringPair> videoDtlList = new ArrayList<>();
                videoDtlList.add(new StringStringPair("동영상 갯수", numberFormat.format(myVideoCount)));
                videoDtlList.add(new StringStringPair("대화방 평균", String.valueOf(avgVideoCount)));
                videoDtlList.add(new StringStringPair("평균 대비 차이", getDiffStr((myVideoCount - avgVideoCount) * 100 / avgVideoCount) + "%"));
                videoDtlList.add(new StringStringPair("순위", getRanking(videoRankingList) + "등 / " + cd.getChatterCount() + "명"));
                videoDtlList.add(new StringStringPair("점유율", Double.toString(NumberUtils.round(myVideoCount * 100 / cd.getVideoCount(),1)) + "% (" + numberFormat.format(myVideoCount) + " / " + numberFormat.format(cd.getVideoCount()) + ")"));
                videoDtlList.add(new StringStringPair("btn", "대화"));
                statsDtlMap.put("동영상 순위", videoDtlList);

                PersonDtlActivity.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        loadingTV.setText("불러오는중... [링크 순위]");
                    }
                });

                double myLinkCount = (double) wordDao.getLinkCountByAuthor(author);
                //Average chat line count = totalChatLineCount / authorCount
                double avgLinkCount = NumberUtils.round((double) cd.getLinkCount() / cd.getChatterCount(), 1);
                List<StringIntPair> linkRankingList = cd.getLinkRankingList();

                statsList.add(new PersonGeneralInfoData(
                        "링크 순위",
                        ""+getRanking(linkRankingList)+"등"));

                List<StringStringPair> linkDtlList = new ArrayList<>();
                linkDtlList.add(new StringStringPair("링크 갯수", numberFormat.format(myLinkCount)));
                linkDtlList.add(new StringStringPair("대화방 평균", String.valueOf(avgLinkCount)));
                linkDtlList.add(new StringStringPair("평균 대비 차이", getDiffStr((myLinkCount - avgLinkCount) * 100 / avgLinkCount) + "%"));
                linkDtlList.add(new StringStringPair("순위", getRanking(linkRankingList) + "등 / " + cd.getChatterCount() + "명"));
                linkDtlList.add(new StringStringPair("점유율", Double.toString(NumberUtils.round(myLinkCount * 100 / cd.getLinkCount(),1)) + "% (" + numberFormat.format(myLinkCount) + " / " + numberFormat.format(cd.getLinkCount()) + ")"));
                linkDtlList.add(new StringStringPair("btn", "대화"));
                statsDtlMap.put("링크 순위", linkDtlList);

                PersonDtlActivity.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        loadingTV.setText("불러오는중... [삭제 메세지 순위]");
                    }
                });

                double myDelCount = (double) chatLineDao.getDeletedMsgCountByAuthor(author);
                //Average chat line count = totalChatLineCount / authorCount
                double avgDelCount = NumberUtils.round((double) cd.getDeletedMsgCount() / cd.getChatterCount(), 1);
                List<StringIntPair> delRankingList = cd.getDelRankingList();

                statsList.add(new PersonGeneralInfoData(
                        "삭제 메세지 순위",
                        getRanking(delRankingList) + "등"));

                List<StringStringPair> delMsgDtlList = new ArrayList<>();
                delMsgDtlList.add(new StringStringPair("삭제 메세지 갯수", numberFormat.format(myDelCount)));
                delMsgDtlList.add(new StringStringPair("대화방 평균", String.valueOf(avgDelCount)));
                delMsgDtlList.add(new StringStringPair("평균 대비 차이", getDiffStr((myDelCount - avgDelCount) * 100 / avgDelCount) + "%"));
                delMsgDtlList.add(new StringStringPair("순위", getRanking(delRankingList) + "등 / " + cd.getChatterCount() + "명"));
                delMsgDtlList.add(new StringStringPair("점유율", Double.toString(NumberUtils.round(myDelCount * 100 / cd.getDeletedMsgCount(),1)) + "% (" + numberFormat.format(myDelCount) + " / " + numberFormat.format(cd.getDeletedMsgCount()) + ")"));
                delMsgDtlList.add(new StringStringPair("btn", "대화"));
                statsDtlMap.put("삭제 메세지 순위", delMsgDtlList);

                PersonDtlActivity.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        loadingTV.setText("불러오는중... [문장 평균 단어 순위]");
                    }
                });

                double mySentWordCount = (double) NumberUtils.round(chatLineDao.getAverageWordCountByAuthor(author), 1);
                //Average chat line count = totalChatLineCount / authorCount
                double avgSentWordCount = NumberUtils.round(cd.getAvgWordCount(), 1);
                List<StringIntPair> sentWordRankingList = cd.getSentWordRankingList();

                statsList.add(new PersonGeneralInfoData(
                        "문장 평균 단어 순위",
                        getRanking(sentWordRankingList) + "등"));

                List<StringStringPair> avgSentWordDtlList = new ArrayList<>();
                avgSentWordDtlList.add(new StringStringPair("문장 평균 단어 갯수", numberFormat.format(mySentWordCount)));
                avgSentWordDtlList.add(new StringStringPair("대화방 평균", String.valueOf(avgSentWordCount)));
                avgSentWordDtlList.add(new StringStringPair("평균 대비 차이", getDiffStr((mySentWordCount - avgSentWordCount) * 100 / avgSentWordCount) + "%"));
                avgSentWordDtlList.add(new StringStringPair("순위", getRanking(sentWordRankingList) + "등 / " + cd.getChatterCount() + "명"));
                avgSentWordDtlList.add(new StringStringPair("btn", "대화"));
                statsDtlMap.put("문장 평균 단어 순위", avgSentWordDtlList);

                PersonDtlActivity.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        loadingTV.setText("불러오는중... [평균 단어 길이 순위]");
                    }
                });

                double myWordLengthCount = (double) NumberUtils.round(wordDao.getAverageLetterCountByAuthor(author), 1);
                //Average chat line count = totalChatLineCount / authorCount
                double avgWordLengthCount = NumberUtils.round(cd.getAvgLetterCount(), 1);
                List<StringIntPair> wordLengthRankingList = cd.getWordLengthRankingList();

                statsList.add(new PersonGeneralInfoData(
                        "평균 단어 길이 순위",
                        getRanking(wordLengthRankingList)+"등"));

                List<StringStringPair> avgWordLengthDtlList = new ArrayList<>();
                avgWordLengthDtlList.add(new StringStringPair("평균 단어 길이", numberFormat.format(myWordLengthCount)));
                avgWordLengthDtlList.add(new StringStringPair("대화방 평균", String.valueOf(avgWordLengthCount)));
                avgWordLengthDtlList.add(new StringStringPair("평균 대비 차이", getDiffStr((myWordLengthCount - avgWordLengthCount) * 100 / avgWordLengthCount) + "%"));
                avgWordLengthDtlList.add(new StringStringPair("순위", getRanking(wordLengthRankingList) + "등 / " + cd.getChatterCount() + "명"));
                avgWordLengthDtlList.add(new StringStringPair("btn", "대화"));
                statsDtlMap.put("평균 단어 길이 순위", avgWordLengthDtlList);

                PersonDtlActivity.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        loadingTV.setText("불러오는중... [활동량 순위]");
                    }
                });

                double myDayCount = (double) NumberUtils.round(chatLineDao.getDaysActiveByAuthor(author), 1);
                //Average chat line count = totalChatLineCount / authorCount
                double dayCount = NumberUtils.round(cd.getAvgDaysActive(), 1);
                daysActiveRankingList = cd.getDaysActiveRankingList();

                statsList.add(new PersonGeneralInfoData(
                        "활동량 순위",
                        getRanking(daysActiveRankingList)+"등"));

                List<StringStringPair> dayCountDtlList = new ArrayList<>();
                dayCountDtlList.add(new StringStringPair("활동 일 수", numberFormat.format(myDayCount)));
                dayCountDtlList.add(new StringStringPair("대화방 평균", String.valueOf(dayCount)));
                dayCountDtlList.add(new StringStringPair("평균 대비 차이", getDiffStr((myDayCount - dayCount) * 100 / dayCount) + "%"));
                dayCountDtlList.add(new StringStringPair("순위", getRanking(daysActiveRankingList) + "등 / " + cd.getChatterCount() + "명"));
                dayCountDtlList.add(new StringStringPair("btn", "대화"));
                statsDtlMap.put("활동량 순위", dayCountDtlList);

                PersonDtlActivity.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        loadingTV.setText("불러오는중... [개인 시간 분석]");
                    }
                });

                timePreloadDayList = chatLineDao.getFreqByDayByAuthor(author);
                timePreloadMonthList = chatLineDao.getFreqByMonthByAuthor(author);
                timePreloadYearList = chatLineDao.getFreqByYearByAuthor(author);
                timePreloadTimeOfDayList = chatLineDao.getFreqByTimeOfDayByAuthor(author);
                timePreloadDayOFWeekList = chatLineDao.getFreqByDayOfWeekByAuthor(author);

                PersonDtlActivity.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        loadingTV.setText("마무리...");
                    }
                });

                top10Words = wordDao.getTop10WordsByAuthor(author);
                distinctWordCount = wordDao.getDistinctWordCountByAuthor(author);



                return null;
            }

            @Override
            protected void onPostExecute(String s) {
                super.onPostExecute(s);

                sectionsPagerAdapter = new PersonDtlSpa(PersonDtlActivity.this, getSupportFragmentManager());
                viewPager.setAdapter(sectionsPagerAdapter);
                tabs.setupWithViewPager(viewPager);


                //customExpandableAdapter.notifyDataSetChanged();

                PersonDtlActivity.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        loadingDialog.cancel();
                    }
                });
            }
        };

        loadTask.execute(new Integer[] {0});







    }

    public int getRanking(List<StringIntPair> rankingList){
        int ranking = 1;
        for(int i=0; i<rankingList.size(); i++){
            if(author.equals(rankingList.get(i).getword())){
                break;
            } else {
                ranking++;
            }
            LogUtils.e("Key: " + rankingList.get(i).getword());
            LogUtils.e("Val: " + rankingList.get(i).getFrequency());
        }
        return ranking;
    }

    public String getDiffStr(double val){
        return (NumberUtils.round(val, 1) > 0 ? "+" : "") + NumberUtils.round(val, 1);
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
}