package com.rexyrex.kakaoparser.Fragments.person;

import android.app.Dialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.BaseExpandableListAdapter;
import android.widget.Button;
import android.widget.ExpandableListView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import androidx.fragment.app.Fragment;
import androidx.viewpager.widget.ViewPager;

import com.bumptech.glide.Glide;
import com.rexyrex.kakaoparser.Activities.MainActivity;
import com.rexyrex.kakaoparser.Activities.PersonListActivity;
import com.rexyrex.kakaoparser.Database.DAO.ChatLineDAO;
import com.rexyrex.kakaoparser.Database.DAO.WordDAO;
import com.rexyrex.kakaoparser.Database.MainDatabase;
import com.rexyrex.kakaoparser.Entities.ChatData;
import com.rexyrex.kakaoparser.Entities.PersonGeneralInfoData;
import com.rexyrex.kakaoparser.Entities.StringIntPair;
import com.rexyrex.kakaoparser.Entities.StringStringPair;
import com.rexyrex.kakaoparser.Fragments.main.GeneralStatsFrag;
import com.rexyrex.kakaoparser.Fragments.main.PersonAnalyseFrag;
import com.rexyrex.kakaoparser.R;
import com.rexyrex.kakaoparser.Utils.LogUtils;
import com.rexyrex.kakaoparser.Utils.NumberUtils;
import com.rexyrex.kakaoparser.Utils.SharedPrefUtils;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link PGeneralFrag#newInstance} factory method to
 * create an instance of this fragment.
 */
public class PGeneralFrag extends Fragment {


    private static final String ARG_PARAM1 = "param1";

    private ChatData cd;


    private MainDatabase database;
    private ChatLineDAO chatLineDao;
    private WordDAO wordDao;
    SharedPrefUtils spu;

    Dialog loadingDialog;

    AsyncTask<Integer, Void, String> loadTask;

    String author;

    ArrayList<PersonGeneralInfoData> statsList;
    HashMap<String, List<StringStringPair>> statsDtlMap;

    NumberFormat numberFormat;

    public PGeneralFrag() {
        // Required empty public constructor
    }

    public static PGeneralFrag newInstance() {
        LogUtils.e("NEW INSTANCE");
        PGeneralFrag fragment = new PGeneralFrag();
        Bundle args = new Bundle();
        //args.putParcelable(ARG_PARAM1, param1);

        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        LogUtils.e("ON CREATE");
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {

            loadingDialog = new Dialog(getContext());
            loadingDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
            loadingDialog.setContentView(R.layout.loading_popup);
            loadingDialog.getWindow().getAttributes().windowAnimations = R.style.FadeInAndFadeOut;
            loadingDialog.setCancelable(false);
            ImageView loadingIV = loadingDialog.findViewById(R.id.loadingPopupIV);
            TextView loadingTV = loadingDialog.findViewById(R.id.loadingPopupTV);
            Glide.with(this).asGif().load(R.drawable.loading1).into(loadingIV);
            loadingTV.setText("불러오는중...");



            cd = ChatData.getInstance(getContext());
            database = MainDatabase.getDatabase(getContext());
            chatLineDao = database.getChatLineDAO();
            wordDao = database.getWordDAO();
            statsList = new ArrayList<>();
            statsDtlMap = new HashMap<>();
            spu = new SharedPrefUtils(getActivity());
            author = spu.getString(R.string.SP_PERSON_DTL_NAME, "");
            numberFormat = NumberFormat.getInstance();
            numberFormat.setGroupingUsed(true);










        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        LogUtils.e("ON CREATE VIEW");
        View view = inflater.inflate(R.layout.fragment_p_general, container, false);
        //Title, raw stat, diff from avg, rank, total
        ExpandableListView statsLV = view.findViewById(R.id.pGeneralLV);

        loadTask = new AsyncTask<Integer, Void, String>() {
            @Override
            protected String doInBackground(Integer... integers) {

                PGeneralFrag.this.getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        loadingDialog.show();
                    }
                });

                double myChatLineCount = (double) chatLineDao.getChatterChatLineCount(author);
                //Average chat line count = totalChatLineCount / authorCount
                double avgChatLineCount = NumberUtils.round((double)  cd.getChatLineCount() / cd.getChatterCount(), 1);
                List<StringIntPair> chatLineRankingList = chatLineDao.getChatterChatLineByRank();

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

                double myTotalWordCount = (double) wordDao.getTotalWordCountByAuthor(author);
                //Average chat line count = totalChatLineCount / authorCount
                double avgTotalWordCount = NumberUtils.round((double)  cd.getTotalWordCount() / cd.getChatterCount(), 1);
                List<StringIntPair> totalWordRankingList = wordDao.getTotalWordCountByRank();

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

                double myDistinctWordCount = (double) wordDao.getDistinctWordCountByAuthor(author);
                //Average chat line count = totalChatLineCount / authorCount
                double avgDistinctWordCount = NumberUtils.round((double) cd.getWordCount() / cd.getChatterCount(), 1);
                List<StringIntPair> distinctWordRankingList = wordDao.getDistinctWordCountByRank();

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

                double myPicCount = (double) wordDao.getPicCountByAuthor(author);
                //Average chat line count = totalChatLineCount / authorCount
                double avgPicCount = NumberUtils.round((double)  cd.getPicCount() / cd.getChatterCount(), 1);
                List<StringIntPair> picRankingList = wordDao.getPicRanking();

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

                double myVideoCount = (double) wordDao.getVideoCountByAuthor(author);
                //Average chat line count = totalChatLineCount / authorCount
                double avgVideoCount = NumberUtils.round((double) cd.getVideoCount() / cd.getChatterCount(), 1);
                List<StringIntPair> videoRankingList = wordDao.getVideoRanking();

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

                double myLinkCount = (double) wordDao.getLinkCountByAuthor(author);
                //Average chat line count = totalChatLineCount / authorCount
                double avgLinkCount = NumberUtils.round((double) cd.getLinkCount() / cd.getChatterCount(), 1);
                List<StringIntPair> linkRankingList = wordDao.getLinkRanking();

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

                double myDelCount = (double) chatLineDao.getDeletedMsgCountByAuthor(author);
                //Average chat line count = totalChatLineCount / authorCount
                double avgDelCount = NumberUtils.round((double) cd.getDeletedMsgCount() / cd.getChatterCount(), 1);
                List<StringIntPair> delRankingList = chatLineDao.getDeletedMsgRanking();

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


                double mySentWordCount = (double) NumberUtils.round(chatLineDao.getAverageWordCountByAuthor(author), 1);
                //Average chat line count = totalChatLineCount / authorCount
                double avgSentWordCount = NumberUtils.round(cd.getAvgWordCount(), 1);
                List<StringIntPair> sentWordRankingList = chatLineDao.getAverageWordCountRanking();

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

                double myWordLengthCount = (double) NumberUtils.round(wordDao.getAverageLetterCountByAuthor(author), 1);
                //Average chat line count = totalChatLineCount / authorCount
                double avgWordLengthCount = NumberUtils.round(cd.getAvgLetterCount(), 1);
                List<StringIntPair> wordLengthRankingList = wordDao.getAverageLetterCountByRank();

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

                double myDayCount = (double) NumberUtils.round(chatLineDao.getDaysActiveByAuthor(author), 1);
                //Average chat line count = totalChatLineCount / authorCount
                double dayCount = NumberUtils.round(chatLineDao.getDaysActiveAverage(), 1);
                List<StringIntPair> daysActiveRankingList = chatLineDao.getDaysActiveRank();

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

                return null;
            }

            @Override
            protected void onPostExecute(String s) {
                super.onPostExecute(s);

                CustomExpandableAdapter customExpandableAdapter = new CustomExpandableAdapter(statsList, statsDtlMap);
                statsLV.setAdapter(customExpandableAdapter);
                //customExpandableAdapter.notifyDataSetChanged();

                PGeneralFrag.this.getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        loadingDialog.cancel();
                    }
                });
            }
        };

        loadTask.execute(new Integer[] {0});

        return view;
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

    class CustomExpandableAdapter extends BaseExpandableListAdapter{

        ArrayList<PersonGeneralInfoData> statsList;
        private HashMap<String, List<StringStringPair>> infoMap;

        public CustomExpandableAdapter(ArrayList<PersonGeneralInfoData> statsList,HashMap<String, List<StringStringPair>> infoMap) {
            this.statsList = statsList;
            this.infoMap = infoMap;
        }

        @Override
        public int getGroupCount() {
            return statsList.size();
        }

        @Override
        public int getChildrenCount(int i) {
            if(infoMap.get(statsList.get(i).getCategoryTitle()) != null) {
                return infoMap.get(statsList.get(i).getCategoryTitle()).size();
            } else {
                return 0;
            }
        }

        @Override
        public Object getGroup(int i) {
            return statsList.get(i);
        }

        @Override
        public Object getChild(int groupPosition, int childPosition) {
            if(infoMap.get(statsList.get(groupPosition).getCategoryTitle()) != null){
                return infoMap.get(statsList.get(groupPosition).getCategoryTitle()).get(childPosition);
            } else {
                return null;
            }

        }

        @Override
        public long getGroupId(int i) {
            return i;
        }

        @Override
        public long getChildId(int i, int i1) {
            return i1;
        }

        @Override
        public boolean hasStableIds() {
            return false;
        }

        @Override
        public View getGroupView(int position, boolean b, View convertView, ViewGroup viewGroup) {
            convertView = getLayoutInflater().inflate(R.layout.list_view_elem_person_general_stat, null);

            TextView titleTV = convertView.findViewById(R.id.pGeneralStatsElemTitleTV);
            TextView valueTV = convertView.findViewById(R.id.pGeneralStatsElemValueTV);

            PersonGeneralInfoData pgid = statsList.get(position);

            titleTV.setText(pgid.getCategoryTitle());

            String medalStr = "";

            if(pgid.getRawData().contains("등")){
                int place = Integer.parseInt(pgid.getRawData().split("등")[0]);
                switch(place){
                    case 1:
                        valueTV.setText("\uD83E\uDD47");
                        medalStr = "\uD83E\uDD47";
                        break;
                    case 2:
                        valueTV.setText("\uD83E\uDD48");
                        medalStr = "\uD83E\uDD48";
                        break;
                    case 3:
                        valueTV.setText("\uD83E\uDD49");
                        medalStr = "\uD83E\uDD49";
                        break;
                    default:
                        valueTV.setText(pgid.getRawData());
                        break;
                }
            }




            return convertView;
        }

        @Override
        public View getChildView(int i, int i1, boolean b, View convertView, ViewGroup viewGroup) {
            StringStringPair ssp = (StringStringPair) getChild(i, i1);

            if(i1 == getChildrenCount(i)-1 && ssp.getTitle().equals("btn")){
                convertView = getLayoutInflater().inflate(R.layout.list_view_elem_show_more_btn, null);
                Button seeMoreBtn = convertView.findViewById(R.id.showMoreBtn);
                seeMoreBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                    }
                });
                return convertView;
            }


            convertView = getLayoutInflater().inflate(R.layout.list_view_elem_person_general_stat_sub, null);
            TextView titleTV = convertView.findViewById(R.id.pGeneralStatsElemSubTitleTV);
            TextView valueTV = convertView.findViewById(R.id.pGeneralStatsElemSubValueTV);

            if(getChild(i, i1) != null){

                titleTV.setText(ssp.getTitle());
                valueTV.setText(ssp.getValue());

                if(ssp.getValue().contains("%")){
                    if(ssp.getValue().contains("+")){
                        valueTV.setTextColor(getActivity().getColor(R.color.darkGreen));
                    } else if(ssp.getValue().contains("-")){
                        valueTV.setTextColor(getActivity().getColor(R.color.design_default_color_error));
                    }

                }

                return convertView;
            } else {
                return null;
            }

        }

        @Override
        public boolean isChildSelectable(int i, int i1) {
            return false;
        }
    }

}