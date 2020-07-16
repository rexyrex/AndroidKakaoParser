package com.rexyrex.kakaoparser.Fragments;

import android.os.Bundle;

import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;

import com.rexyrex.kakaoparser.Activities.ChatStatsTabActivity;
import com.rexyrex.kakaoparser.Database.DAO.ChatLineDAO;
import com.rexyrex.kakaoparser.Database.MainDatabase;
import com.rexyrex.kakaoparser.Database.Models.ChatLineModel;
import com.rexyrex.kakaoparser.Entities.ChatData;
import com.rexyrex.kakaoparser.Entities.StringIntPair;
import com.rexyrex.kakaoparser.R;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link ChatAnalyseFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ChatAnalyseFragment extends Fragment {

    ArrayList<ChatLineModel> chatList;
    private ChatData cd;

    private MainDatabase database;
    private ChatLineDAO chatLineDao;
    NumberFormat numberFormat;

    ChatListAdapter cla;

    String[] authors;

    AlertDialog dialog;

    public ChatAnalyseFragment() {
        // Required empty public constructor
    }


    public static ChatAnalyseFragment newInstance() {
        ChatAnalyseFragment fragment = new ChatAnalyseFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            cd = ChatData.getInstance();
            database = MainDatabase.getDatabase(getContext());
            chatLineDao = database.getChatLineDAO();
            numberFormat = NumberFormat.getInstance();
            numberFormat.setGroupingUsed(true);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_chat_analyse, container, false);

        EditText searchET = view.findViewById(R.id.chatSearchET);
        Spinner authorSpinner = view.findViewById(R.id.chatAnalyseAuthorSpinner);
        Button sortPopBtn = view.findViewById(R.id.chatAnalyseSortPopBtn);
        ListView chatLV = view.findViewById(R.id.chatAnalyseChatLV);
        TextView searchResTV = view.findViewById(R.id.chatSearchResTV);

        sortPopBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                View popView = (LayoutInflater.from(ChatAnalyseFragment.this.getContext())).inflate(R.layout.chat_analysis_sort_pop, null);

                AlertDialog.Builder rexAlertBuilder = new AlertDialog.Builder(ChatAnalyseFragment.this.getActivity(), R.style.PopupStyle);
                rexAlertBuilder.setView(popView);
                rexAlertBuilder.setCancelable(true);
                dialog = rexAlertBuilder.create();
                dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
                dialog.show();
            }
        });

        chatList = new ArrayList<>();

        List<ChatLineModel> initChatList = chatLineDao.getAllChatsByDateDesc();
        for(ChatLineModel element : initChatList) chatList.add(element);

        cla = new ChatListAdapter(chatList);
        chatLV.setAdapter(cla);

        List<String> authorsList = chatLineDao.getChatters();

        authors = new String[authorsList.size() +1];

        for(int i=0; i<authorsList.size()+1; i++){
            if(i==0){
                authors[i] = "전체";
            } else {
                authors[i] = authorsList.get(i-1);
            }
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<>(view.getContext(), android.R.layout.simple_spinner_dropdown_item, authors);
        authorSpinner.setAdapter(adapter);

        return view;
    }

    public void search(String charText) {
        chatList.clear();
//        if (charText.length() == 0) {
//            freqList.addAll(wordFreqArrList);
//        } else
//        {
//            for(int i = 0;i < wordFreqArrList.size(); i++)
//            {
//                if (wordFreqArrList.get(i).getword().toLowerCase().contains(charText))
//                {
//                    freqList.add(wordFreqArrList.get(i));
//                }
//            }
//        }
        if(charText.length() == 0){
            //chatList.addAll(cd.getWordFreqArrList());
        } else {
            //chatList.addAll(wordDao.searchFreqWordList(charText));
        }

        //wordCountTV.setText("검색 결과 " + freqList.size() + "건");
        cla.notifyDataSetChanged();
    }

    class ChatListAdapter extends BaseAdapter {
        ArrayList<ChatLineModel> chatLineArrList;

        ChatListAdapter(ArrayList<ChatLineModel> chatLineArrList){
            this.chatLineArrList = chatLineArrList;
        }

        @Override
        public int getCount() {
            return chatLineArrList.size();
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
            convertView = getLayoutInflater().inflate(R.layout.list_view_elem_chat_line, null);
            TextView contentsTV = convertView.findViewById(R.id.chatLineElemContentsTV);
            TextView descTV = convertView.findViewById(R.id.chatLineElemDescTV);

            ChatLineModel clm = chatLineArrList.get(position);

            contentsTV.setText(clm.getContent());
            descTV.setText(clm.getAuthor() + ", " + clm.getDateDayString());

            return convertView;
        }
    }
}