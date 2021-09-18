package com.rexyrex.kakaoparser.Fragments.person;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import androidx.fragment.app.Fragment;
import androidx.viewpager.widget.ViewPager;

import com.rexyrex.kakaoparser.Database.DAO.ChatLineDAO;
import com.rexyrex.kakaoparser.Database.DAO.WordDAO;
import com.rexyrex.kakaoparser.Database.MainDatabase;
import com.rexyrex.kakaoparser.Entities.ChatData;
import com.rexyrex.kakaoparser.Entities.PersonGeneralInfoData;
import com.rexyrex.kakaoparser.Entities.StringIntPair;
import com.rexyrex.kakaoparser.Entities.StringStringPair;
import com.rexyrex.kakaoparser.Fragments.main.GeneralStatsFrag;
import com.rexyrex.kakaoparser.R;
import com.rexyrex.kakaoparser.Utils.LogUtils;
import com.rexyrex.kakaoparser.Utils.NumberUtils;
import com.rexyrex.kakaoparser.Utils.SharedPrefUtils;

import java.text.NumberFormat;
import java.util.ArrayList;
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

    String author;

    ArrayList<PersonGeneralInfoData> statsList;

    NumberFormat numberFormat;

    public PGeneralFrag() {
        // Required empty public constructor
    }

    public static PGeneralFrag newInstance() {
        PGeneralFrag fragment = new PGeneralFrag();
        Bundle args = new Bundle();
        //args.putParcelable(ARG_PARAM1, param1);

        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            cd = ChatData.getInstance(getContext());
            database = MainDatabase.getDatabase(getContext());
            chatLineDao = database.getChatLineDAO();
            wordDao = database.getWordDAO();
            statsList = new ArrayList<>();
            spu = new SharedPrefUtils(getActivity());
            author = spu.getString(R.string.SP_PERSON_DTL_NAME, "");
            numberFormat = NumberFormat.getInstance();
            numberFormat.setGroupingUsed(true);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_p_general, container, false);
        //Title, raw stat, diff from avg, rank, total
        ListView statsLV = view.findViewById(R.id.pGeneralLV);

        double myChatLineCount = (double) chatLineDao.getChatterChatLineCount(author);
        //Average chat line count = totalChatLineCount / authorCount
        double avgChatLineCount = NumberUtils.round(cd.getChatLineCount() / cd.getChatterCount(), 1);
        List<StringIntPair> chatLineRankingList = chatLineDao.getChatterChatLineByRank();

        statsList.add(new PersonGeneralInfoData(
                "대화 수",
                numberFormat.format(myChatLineCount),
                NumberUtils.round((myChatLineCount - avgChatLineCount) * 100 / avgChatLineCount, 1),
                getRanking(chatLineRankingList),
                cd.getChatterCount()));

        double myTotalWordCount = (double) wordDao.getTotalWordCountByAuthor(author);
        //Average chat line count = totalChatLineCount / authorCount
        double avgTotalWordCount = NumberUtils.round(cd.getTotalWordCount() / cd.getChatterCount(), 1);
        List<StringIntPair> totalWordRankingList = wordDao.getTotalWordCountByRank();

        statsList.add(new PersonGeneralInfoData(
                "총 단어 수",
                numberFormat.format(myTotalWordCount),
                NumberUtils.round((myTotalWordCount - avgTotalWordCount) * 100 / avgTotalWordCount, 1),
                getRanking(totalWordRankingList),
                cd.getChatterCount()));

        double myDistinctWordCount = (double) wordDao.getDistinctWordCountByAuthor(author);
        //Average chat line count = totalChatLineCount / authorCount
        double avgDistinctWordCount = NumberUtils.round(cd.getWordCount() / cd.getChatterCount(), 1);
        List<StringIntPair> distinctWordRankingList = wordDao.getDistinctWordCountByRank();

        statsList.add(new PersonGeneralInfoData(
                "단어 종류",
                numberFormat.format(myDistinctWordCount),
                NumberUtils.round((myDistinctWordCount - avgDistinctWordCount) * 100 / avgDistinctWordCount, 1),
                getRanking(distinctWordRankingList),
                cd.getChatterCount()));

        double myPicCount = (double) wordDao.getPicCountByAuthor(author);
        //Average chat line count = totalChatLineCount / authorCount
        double avgPicCount = NumberUtils.round(cd.getPicCount() / cd.getChatterCount(), 1);
        List<StringIntPair> picRankingList = wordDao.getPicRanking();

        statsList.add(new PersonGeneralInfoData(
                "사진 수",
                numberFormat.format(myPicCount),
                NumberUtils.round((myPicCount - avgPicCount) * 100 / avgPicCount, 1),
                getRanking(picRankingList),
                cd.getChatterCount()));

        double myVideoCount = (double) wordDao.getVideoCountByAuthor(author);
        //Average chat line count = totalChatLineCount / authorCount
        double avgVideoCount = NumberUtils.round(cd.getVideoCount() / cd.getChatterCount(), 1);
        List<StringIntPair> videoRankingList = wordDao.getVideoRanking();

        statsList.add(new PersonGeneralInfoData(
                "동영상 수",
                numberFormat.format(myVideoCount),
                NumberUtils.round((myVideoCount - avgVideoCount) * 100 / avgVideoCount, 1),
                getRanking(videoRankingList),
                cd.getChatterCount()));

        double myLinkCount = (double) wordDao.getLinkCountByAuthor(author);
        //Average chat line count = totalChatLineCount / authorCount
        double avgLinkCount = NumberUtils.round(cd.getLinkCount() / cd.getChatterCount(), 1);
        List<StringIntPair> linkRankingList = wordDao.getLinkRanking();

        statsList.add(new PersonGeneralInfoData(
                "링크 수",
                numberFormat.format(myLinkCount),
                NumberUtils.round((myLinkCount - avgLinkCount) * 100 / avgLinkCount, 1),
                getRanking(linkRankingList),
                cd.getChatterCount()));

        double myDelCount = (double) chatLineDao.getDeletedMsgCountByAuthor(author);
        //Average chat line count = totalChatLineCount / authorCount
        double avgDelCount = NumberUtils.round(cd.getDeletedMsgCount() / cd.getChatterCount(), 1);
        List<StringIntPair> delRankingList = chatLineDao.getDeletedMsgRanking();

        statsList.add(new PersonGeneralInfoData(
                "삭제 메세지 수",
                numberFormat.format(myDelCount),
                NumberUtils.round((myDelCount - avgDelCount) * 100 / avgDelCount, 1),
                getRanking(delRankingList),
                cd.getChatterCount()));


        double mySentWordCount = (double) NumberUtils.round(chatLineDao.getAverageWordCountByAuthor(author), 1);
        //Average chat line count = totalChatLineCount / authorCount
        double avgSentWordCount = NumberUtils.round(cd.getAvgWordCount(), 1);
        List<StringIntPair> sentWordRankingList = chatLineDao.getAverageWordCountRanking();

        statsList.add(new PersonGeneralInfoData(
                "문장 평균 단어 수",
                numberFormat.format(mySentWordCount),
                NumberUtils.round((mySentWordCount - avgSentWordCount) * 100 / avgSentWordCount, 1),
                getRanking(sentWordRankingList),
                cd.getChatterCount()));

        double myWordLengthCount = (double) NumberUtils.round(wordDao.getAverageLetterCountByAuthor(author), 1);
        //Average chat line count = totalChatLineCount / authorCount
        double avgWordLengthCount = NumberUtils.round(cd.getAvgLetterCount(), 1);
        List<StringIntPair> wordLengthRankingList = wordDao.getAverageLetterCountByRank();

        statsList.add(new PersonGeneralInfoData(
                "평균 단어 길이",
                numberFormat.format(myWordLengthCount),
                NumberUtils.round((myWordLengthCount - avgWordLengthCount) * 100 / avgWordLengthCount, 1),
                getRanking(wordLengthRankingList),
                cd.getChatterCount()));

        double myDayCount = (double) NumberUtils.round(chatLineDao.getDaysActiveByAuthor(author), 1);
        //Average chat line count = totalChatLineCount / authorCount
        double dayCount = NumberUtils.round(chatLineDao.getDaysActiveAverage(), 1);
        List<StringIntPair> daysActiveRankingList = chatLineDao.getDaysActiveRank();

        statsList.add(new PersonGeneralInfoData(
                "활동 일 수",
                numberFormat.format(myDayCount) + " / " + cd.getDayCount(),
                NumberUtils.round((myDayCount - dayCount) * 100 / dayCount, 1),
                getRanking(daysActiveRankingList),
                cd.getChatterCount()));

        CustomAdapter customAdapter = new CustomAdapter(statsList);
        statsLV.setAdapter(customAdapter);

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

    class CustomAdapter extends BaseAdapter {

        ArrayList<PersonGeneralInfoData> statsList;

        CustomAdapter(ArrayList<PersonGeneralInfoData> pairs){
            this.statsList = pairs;
        }

        @Override
        public int getCount() {
            return statsList.size();
        }

        @Override
        public Object getItem(int position) {
            return null;
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            convertView = getLayoutInflater().inflate(R.layout.list_view_elem_person_general_stat, null);

            TextView titleTV = convertView.findViewById(R.id.pGeneralStatsElemTitleTV);
            TextView valueTV = convertView.findViewById(R.id.pGeneralStatsElemValueTV);
            TextView diffFromAvgTV = convertView.findViewById(R.id.pGeneralStatsDesc1TV);
            TextView rankingTV = convertView.findViewById(R.id.pGeneralStatsDesc2TV);

            titleTV.setText(statsList.get(position).getCategoryTitle());
            valueTV.setText(statsList.get(position).getRawData());

            String addOrSub = "";

            if(statsList.get(position).getDiffFromAvg() > 0){
                LogUtils.e("green");
                addOrSub = "+";
                diffFromAvgTV.setTextColor(getActivity().getColor(R.color.darkGreen));
            } else if(statsList.get(position).getDiffFromAvg() < 0){
                LogUtils.e("red");
                //addOrSub = "-";
                diffFromAvgTV.setTextColor(getActivity().getColor(R.color.design_default_color_error));
            } else {
                LogUtils.e("black");
                diffFromAvgTV.setTextColor(getActivity().getColor(R.color.black));
            }
            diffFromAvgTV.setText("평균 대비 " + addOrSub + statsList.get(position).getDiffFromAvg() + "%");

            rankingTV.setText(statsList.get(position).getRanking() + "등 " + "/ " + statsList.get(position).getTotal() + "명");



            return convertView;
        }
    }

}