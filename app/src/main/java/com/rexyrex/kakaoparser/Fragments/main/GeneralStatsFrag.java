package com.rexyrex.kakaoparser.Fragments.main;

import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.viewpager.widget.ViewPager;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.rexyrex.kakaoparser.Database.DAO.ChatLineDAO;
import com.rexyrex.kakaoparser.Database.DAO.WordDAO;
import com.rexyrex.kakaoparser.Database.MainDatabase;
import com.rexyrex.kakaoparser.Entities.ChatData;
import com.rexyrex.kakaoparser.Entities.StringStringPair;
import com.rexyrex.kakaoparser.R;
import com.rexyrex.kakaoparser.Utils.ShareUtils;

import java.text.NumberFormat;
import java.util.ArrayList;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link GeneralStatsFrag#newInstance} factory method to
 * create an instance of this fragment.
 */
public class GeneralStatsFrag extends Fragment {


    private static final String ARG_PARAM1 = "param1";

    private ChatData cd;

    private MainDatabase database;
    private ChatLineDAO chatLineDao;
    private WordDAO wordDao;

    private FloatingActionButton fab;

    NumberFormat numberFormat;

    public GeneralStatsFrag() {
        // Required empty public constructor
    }

    public static GeneralStatsFrag newInstance() {
        GeneralStatsFrag fragment = new GeneralStatsFrag();
        Bundle args = new Bundle();
        //args.putParcelable(ARG_PARAM1, param1);

        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            //cd = getArguments().getParcelable(ARG_PARAM1);
            cd = ChatData.getInstance(getContext());
            database = MainDatabase.getDatabase(getContext());
            chatLineDao = database.getChatLineDAO();
            wordDao = database.getWordDAO();
            numberFormat = NumberFormat.getInstance();
            numberFormat.setGroupingUsed(true);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_general_stats, container, false);

        ListView generalStatsLV = view.findViewById(R.id.generalStatsLV);
        fab = view.findViewById(R.id.fabGeneral);



        //SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy.M.d");
        //String dateRange = dateFormat.format(chatLineDao.getStartDate()) + "~" + dateFormat.format(chatLineDao.getEndDate());

        final ArrayList<StringStringPair> pairs = new ArrayList<>();
        //pairs.add(new StringStringPair("분석 기간", "" + dateRange));
        pairs.add(new StringStringPair("대화 참여 인원", ""+ cd.getChatterCount()));
        pairs.add(new StringStringPair("분석 일 수", "" + numberFormat.format(cd.getDayCount())));
        pairs.add(new StringStringPair("분석 대화 수", "" + numberFormat.format(cd.getChatLineCount())));
        pairs.add(new StringStringPair("분석 총 단어 수", "" + numberFormat.format(cd.getTotalWordCount())));
        pairs.add(new StringStringPair("분석 단어 종류", "" + numberFormat.format(cd.getWordCount())));
        pairs.add(new StringStringPair("분석 소요 시간 (초)", "" + String.format("%.1f", cd.getLoadElapsedSeconds())));
        pairs.add(new StringStringPair("문장 평균 단어 수", "" + String.format("%.1f", cd.getAvgWordCount())));
        pairs.add(new StringStringPair("평균 단어 길이", "" + String.format("%.1f", cd.getAvgLetterCount())));
        pairs.add(new StringStringPair("링크 개수", "" + numberFormat.format(cd.getLinkCount())));
        pairs.add(new StringStringPair("사진 개수", "" + numberFormat.format(cd.getPicCount())));
        pairs.add(new StringStringPair("동영상 개수", "" + numberFormat.format(cd.getVideoCount())));
        pairs.add(new StringStringPair("PPT 개수", "" + numberFormat.format(cd.getPptCount())));
        pairs.add(new StringStringPair("삭제된 메시지", "" + numberFormat.format(cd.getDeletedMsgCount())));

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String shareContent = "";
                for(int i=0; i<pairs.size(); i++){
                    shareContent += pairs.get(i).getTitle() + " : " + pairs.get(i).getValue() + "\n";
                }
                ShareUtils.shareAnalysisInfoWithPromo(getActivity(), cd.getChatFileTitle(), "분석 개요", shareContent, R.string.SP_SHARE_GENERAL_ANALZ_COUNT);
            }
        });

        CustomAdapter customAdapter = new CustomAdapter(pairs);
        generalStatsLV.setAdapter(customAdapter);

        generalStatsLV.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if(position==0){
                    ViewPager vp = GeneralStatsFrag.this.getActivity().findViewById(R.id.view_pager);
                    vp.setCurrentItem(1);
                }
                if(position==1){
                    ViewPager vp = GeneralStatsFrag.this.getActivity().findViewById(R.id.view_pager);
                    vp.setCurrentItem(4);
                }
                if(position==2){
                    ViewPager vp = GeneralStatsFrag.this.getActivity().findViewById(R.id.view_pager);
                    vp.setCurrentItem(2);
                }
                if(position==3){
                    ViewPager vp = GeneralStatsFrag.this.getActivity().findViewById(R.id.view_pager);
                    vp.setCurrentItem(3);
                }
            }
        });

        return view;
    }

    class CustomAdapter extends BaseAdapter {

        ArrayList<StringStringPair> pairs;

        CustomAdapter(ArrayList<StringStringPair> pairs){
            this.pairs = pairs;
        }

        @Override
        public int getCount() {
            return pairs.size();
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
            convertView = getLayoutInflater().inflate(R.layout.list_view_elem_general_stat, null);

            TextView titleTV = convertView.findViewById(R.id.pGeneralStatsElemTitleTV);
            TextView valueTV = convertView.findViewById(R.id.pGeneralStatsElemValueTV);

            titleTV.setText(pairs.get(position).getTitle());
            valueTV.setText(pairs.get(position).getValue());

            if(pairs.get(position).getValue().length() > 12){
                valueTV.setTextSize(30);
            }

            return convertView;
        }
    }
}