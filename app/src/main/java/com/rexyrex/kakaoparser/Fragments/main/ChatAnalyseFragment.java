package com.rexyrex.kakaoparser.Fragments.main;

import android.content.Intent;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;

import com.rexyrex.kakaoparser.Activities.ChatPeekActivity;
import com.rexyrex.kakaoparser.Database.DAO.ChatLineDAO;
import com.rexyrex.kakaoparser.Database.MainDatabase;
import com.rexyrex.kakaoparser.Database.Models.ChatLineModel;
import com.rexyrex.kakaoparser.Entities.ChatData;
import com.rexyrex.kakaoparser.Entities.ChatSnippetData;
import com.rexyrex.kakaoparser.R;
import com.rexyrex.kakaoparser.Utils.KeyboardUtils;

import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

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

    Spinner authorSpinner;
    Spinner orderSpinner;
    EditText searchET;

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

        searchET = view.findViewById(R.id.chatSearchET);
        authorSpinner = view.findViewById(R.id.chatAnalyseAuthorSpinner);

        ListView chatLV = view.findViewById(R.id.chatAnalyseChatLV);
        TextView searchResTV = view.findViewById(R.id.chatSearchResTV);

        orderSpinner = view.findViewById(R.id.chatAnalyseOrderSpinner);

        String[] items = new String[]{"시간 ▲", "시간 ▼", "길이 ▲", "길이 ▼"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(view.getContext(), android.R.layout.simple_spinner_dropdown_item, items);
        orderSpinner.setAdapter(adapter);

        orderSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                requestList();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        searchET.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View view, int i, KeyEvent keyEvent) {
                if ((keyEvent.getAction() == KeyEvent.ACTION_DOWN) &&
                        (keyEvent.getKeyCode() == KeyEvent.KEYCODE_ENTER)) {
                    // Perform action on key press
                    requestList();
                    KeyboardUtils.hideKeyboard(getActivity());
                    return true;
                }
                return false;
            }
        });

        chatList = new ArrayList<>();

        //<ChatLineModel> initChatList = chatLineDao.getAllChatsByDateDesc();
        List<ChatLineModel> initChatList = cd.getAllChatInit();
        for(ChatLineModel element : initChatList) chatList.add(element);

        cla = new ChatListAdapter(chatList);
        chatLV.setAdapter(cla);

        //List<String> authorsList = chatLineDao.getChatters();
        List<String> authorsList = cd.getAuthorsList();
        authors = new String[authorsList.size() +1];

        for(int i=0; i<authorsList.size()+1; i++){
            if(i==0){
                authors[i] = "전체";
            } else {
                authors[i] = authorsList.get(i-1);
            }
        }

        ArrayAdapter<String> orderAdapter = new ArrayAdapter<>(view.getContext(), android.R.layout.simple_spinner_dropdown_item, authors);
        authorSpinner.setAdapter(orderAdapter);

        authorSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                requestList();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        chatLV.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View viewz, int position, long id) {
                ChatSnippetData csd = ChatSnippetData.getInstance();
                csd.setClm(chatLineDao.getSurroundingChatLines(chatList.get(position).getId()));
                csd.setHighlightChatLine(chatList.get(position));
                Intent statsIntent = new Intent(ChatAnalyseFragment.this.getActivity(), ChatPeekActivity.class);
                ChatAnalyseFragment.this.getActivity().startActivity(statsIntent);
            }
        });

        return view;
    }

    public void refreshList(List<ChatLineModel> chatLines){
        chatList.clear();
        for(ChatLineModel element : chatLines) chatList.add(element);
        cla.notifyDataSetChanged();
    }

    public void requestList(){
        String authorStr = authorSpinner.getSelectedItem().toString();
        String searchStr = searchET.getText().toString();
        int orderId = orderSpinner.getSelectedItemPosition();

        //No Author Filter
        if(authorStr.equals("전체")){
            //No filters
            if(searchStr.equals("")){
                switch(orderId){
                    case 0: refreshList(chatLineDao.getAllChatsByDateAsc()); break;
                    case 1: refreshList(chatLineDao.getAllChatsByDateDesc()); break;
                    case 2: refreshList(chatLineDao.getAllChatsByLengthAsc()); break;
                    case 3: refreshList(chatLineDao.getAllChatsByLengthDesc()); break;
                }
            } else {
                //Search Filter
                switch(orderId){
                    case 0: refreshList(chatLineDao.getAllChatsByDateAscFilterChat(searchStr)); break;
                    case 1: refreshList(chatLineDao.getAllChatsByDateDescFilterChat(searchStr)); break;
                    case 2: refreshList(chatLineDao.getAllChatsByLengthAscFilterChat(searchStr)); break;
                    case 3: refreshList(chatLineDao.getAllChatsByLengthDescFilterChat(searchStr)); break;
                }
            }
        } else {
            //Author Filter
            if(searchStr.equals("")){
                switch(orderId){
                    case 0: refreshList(chatLineDao.getAllChatsByDateAscFilterAuthor(authorStr)); break;
                    case 1: refreshList(chatLineDao.getAllChatsByDateDescFilterAuthor(authorStr)); break;
                    case 2: refreshList(chatLineDao.getAllChatsByLengthAscFilterAuthor(authorStr)); break;
                    case 3: refreshList(chatLineDao.getAllChatsByLengthDescFilterAuthor(authorStr)); break;
                }
            } else {
                //Search filter, Author Filter
                switch(orderId){
                    case 0: refreshList(chatLineDao.getAllChatsByDateAscFilterChatFilterAuthor(searchStr, authorStr)); break;
                    case 1: refreshList(chatLineDao.getAllChatsByDateDescFilterChatFilterAuthor(searchStr, authorStr)); break;
                    case 2: refreshList(chatLineDao.getAllChatsByLengthAscFilterChatFilterAuthor(searchStr, authorStr)); break;
                    case 3: refreshList(chatLineDao.getAllChatsByLengthDescFilterChatFilterAuthor(searchStr, authorStr)); break;
                }
            }
        }


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
            SimpleDateFormat sdf = new SimpleDateFormat("a h:mm", Locale.KOREAN);

            convertView = getLayoutInflater().inflate(R.layout.list_view_elem_chat_line, null);
            TextView contentsTV = convertView.findViewById(R.id.chatLineElemContentsTV);
            TextView descTV = convertView.findViewById(R.id.chatLineElemDescTV);

            ChatLineModel clm = chatLineArrList.get(position);

            contentsTV.setText(clm.getContent());
            descTV.setText(clm.getAuthor() + "\n" + clm.getDateDayString() + "\n" + sdf.format(clm.getDate()));

            return convertView;
        }
    }
}