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
import android.widget.Toast;

import androidx.fragment.app.Fragment;
import androidx.viewpager.widget.ViewPager;

import com.bumptech.glide.Glide;
import com.rexyrex.kakaoparser.Activities.DetailActivities.PersonDtlActivity;
import com.rexyrex.kakaoparser.Activities.MainActivity;
import com.rexyrex.kakaoparser.Activities.PersonListActivity;
import com.rexyrex.kakaoparser.Database.DAO.ChatLineDAO;
import com.rexyrex.kakaoparser.Database.DAO.WordDAO;
import com.rexyrex.kakaoparser.Database.MainDatabase;
import com.rexyrex.kakaoparser.Entities.ChatData;
import com.rexyrex.kakaoparser.Entities.PersonChatData;
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

    private ChatData cd;

    private MainDatabase database;
    private ChatLineDAO chatLineDao;
    private WordDAO wordDao;
    SharedPrefUtils spu;

    ArrayList<PersonGeneralInfoData> statsList;
    HashMap<String, List<StringStringPair>> statsDtlMap;

    String author;

    NumberFormat numberFormat;

    PersonChatData pcd;

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
            cd = ChatData.getInstance(getContext());
            pcd = PersonChatData.getInstance(getContext());
            database = MainDatabase.getDatabase(getContext());
            chatLineDao = database.getChatLineDAO();
            wordDao = database.getWordDAO();
            statsList = pcd.getStatsList();
            statsDtlMap = pcd.getStatsDtlMap();
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


        CustomExpandableAdapter customExpandableAdapter = new CustomExpandableAdapter(statsList, statsDtlMap);
        statsLV.setAdapter(customExpandableAdapter);


        return view;
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
                        Toast.makeText(getActivity(), "준비중입니다", Toast.LENGTH_SHORT).show();
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