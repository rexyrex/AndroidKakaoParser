package com.rexyrex.kakaoparser.Activities;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.rexyrex.kakaoparser.Database.Models.ChatLineModel;
import com.rexyrex.kakaoparser.Entities.ChatSnippetData;
import com.rexyrex.kakaoparser.R;
import com.rexyrex.kakaoparser.Utils.DialogUtils;

import java.text.SimpleDateFormat;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;

public class ChatPeekActivity extends AppCompatActivity {

    List<ChatLineModel> clm;
    ChatLineModel highlightChatLine;
    String author;

    ListView chatLV;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_peek);

        chatLV = findViewById(R.id.chatSnippetLV);

        author = "회원님";

        ChatSnippetData csd = (ChatSnippetData) getIntent().getSerializableExtra("chatSnippetData");
        clm = csd.getClm();
        highlightChatLine = csd.getHighlightChatLine();

        ChatListAdapter cla = new ChatListAdapter(clm);
        chatLV.setAdapter(cla);

        final Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                chatLV.smoothScrollToPosition((int)(getHighlightPos() + 5));
            }
        }, 300);
    }

    public int getHighlightPos(){
        //get position of highlight
        int hPos = 0;
        for(int i=0; i<clm.size(); i++){
            if(clm.get(i).getId() == highlightChatLine.getId()){
                return i;
            }
        }
        return 0;
    }

    class ChatListAdapter extends BaseAdapter {
        List<ChatLineModel> wordFreqArrList;
        int dayCount = 0;
        HashSet<String> parsedDaySet;
        boolean[] showDateSepArr;
        int[] daysPassedArr;
        boolean[] isNewMsg;

        ChatListAdapter(List<ChatLineModel> wordFreqArrList){
            parsedDaySet = new HashSet<>();
            this.wordFreqArrList = wordFreqArrList;

            HashSet<String> daySet = new HashSet<>();

            for(int i=0; i<wordFreqArrList.size(); i++){
                ChatLineModel clm = wordFreqArrList.get(i);
                if(!daySet.contains(clm.getDateDayString())){
                    dayCount++;
                    daySet.add(clm.getDateDayString());
                }
            }

            showDateSepArr = new boolean[wordFreqArrList.size() + dayCount];
            daysPassedArr = new int[wordFreqArrList.size()+ dayCount];

            for(int i=0; i<showDateSepArr.length; i++){
                showDateSepArr[i] = false;
            }

            HashSet<String> daySet2 = new HashSet<>();
            int dayCounter = 0;
            for(int i=0; i<wordFreqArrList.size(); i++){
                if(!daySet2.contains(wordFreqArrList.get(i).getDateDayString())){
                    daySet2.add(wordFreqArrList.get(i).getDateDayString());
                    showDateSepArr[i+dayCounter] = true;
                    dayCounter++;
                }
            }

            int tmp = 0;
            for(int i=0; i<showDateSepArr.length; i++){
                if(showDateSepArr[i]){
                    tmp++;
                }
                daysPassedArr[i] = tmp;
            }

            isNewMsg = new boolean[wordFreqArrList.size()];
            isNewMsg[0] = true;
            for(int i=1; i<isNewMsg.length; i++){
                if(wordFreqArrList.get(i-1).getAuthor().equals(wordFreqArrList.get(i).getAuthor())){
                    isNewMsg[i] = false;
                } else {
                    isNewMsg[i] = true;
                }
            }
        }

        @Override
        public int getCount() {
            return wordFreqArrList.size() + dayCount;
            //return wordFreqArrList.size();
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
            Activity activity = (Activity) ChatPeekActivity.this;
            TextView sentenceTV;
            TextView dateTV;
            TextView authorTV;

            //SimpleDateFormat sdf = new SimpleDateFormat("yyyy-M-dd\na h:mm", Locale.KOREAN);
            SimpleDateFormat sdf = new SimpleDateFormat("a h:mm", Locale.KOREAN);
            //Date separator
            if(showDateSepArr[position]){
                convertView = activity.getLayoutInflater().inflate(R.layout.chat_list_elem_date_separator, null);
                TextView dateSepTV = convertView.findViewById(R.id.chatListElemDateSeparatorTV);
                ChatLineModel prevModel = null;
                if(position == 0){
                    prevModel = wordFreqArrList.get(0);
                } else {
                    prevModel = wordFreqArrList.get(position - daysPassedArr[position]+1);
                }
                dateSepTV.setText(prevModel.getDateDayString());
                return convertView;
            }

            ChatLineModel clm = wordFreqArrList.get(position==0?0 : position - daysPassedArr[position]);
            if(clm.getAuthor().equals(author)){
                //outgoing msg

                //should show author name (new author)
                if(!isNewMsg[position==0?0 : position - daysPassedArr[position]]){
                    convertView = activity.getLayoutInflater().inflate(R.layout.chat_list_elem_outgoing, null);
                    sentenceTV = convertView.findViewById(R.id.outgoingChatContentTV);
                    dateTV = convertView.findViewById(R.id.outgoingChatDateTV);
                } else {
                    convertView = activity.getLayoutInflater().inflate(R.layout.chat_list_elem_outgoing_new, null);
                    sentenceTV = convertView.findViewById(R.id.outgoingNewChatContentTV);
                    dateTV = convertView.findViewById(R.id.outgoingNewChatDateTV);
                    authorTV = convertView.findViewById(R.id.outgoingNewChatAuthorTV);
                    authorTV.setText(clm.getAuthor());
                }
            } else {
                //Incoming msg
                if(!isNewMsg[position==0?0 : position - daysPassedArr[position]]){
                    convertView = activity.getLayoutInflater().inflate(R.layout.chat_list_elem_incoming, null);
                    sentenceTV = convertView.findViewById(R.id.incomingChatContentTV);
                    dateTV = convertView.findViewById(R.id.incomingChatDateTV);
                } else {
                    convertView = activity.getLayoutInflater().inflate(R.layout.chat_list_elem_incoming_new, null);
                    sentenceTV = convertView.findViewById(R.id.incomingNewChatContentTV);
                    dateTV = convertView.findViewById(R.id.incomingNewChatDateTV);
                    authorTV = convertView.findViewById(R.id.incomingNewChatAuthorTV);
                    authorTV.setText(clm.getAuthor());
                }
            }
            sentenceTV.setText(clm.getContent());
            dateTV.setText(sdf.format(clm.getDate()));

            //highlight
            if(highlightChatLine!=null && highlightChatLine.getId() == clm.getId()){
                sentenceTV.setBackground(ChatPeekActivity.this.getDrawable(R.drawable.chat_bubble_highlight));
            }

            return convertView;
        }
    }
}