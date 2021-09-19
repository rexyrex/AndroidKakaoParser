package com.rexyrex.kakaoparser.Activities;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import com.rexyrex.kakaoparser.Activities.DetailActivities.PersonDtlActivity;
import com.rexyrex.kakaoparser.Database.DAO.ChatLineDAO;
import com.rexyrex.kakaoparser.Database.DAO.WordDAO;
import com.rexyrex.kakaoparser.Database.MainDatabase;
import com.rexyrex.kakaoparser.Entities.ChatData;
import com.rexyrex.kakaoparser.Entities.StringIntPair;
import com.rexyrex.kakaoparser.R;
import com.rexyrex.kakaoparser.Utils.SharedPrefUtils;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;

public class PersonListActivity extends AppCompatActivity {

    EditText searchET;
    ListView searchLV;
    
    Toolbar tb;

    NumberFormat numberFormat;

    ChatData cd;

    ArrayList<StringIntPair> freqList;
    private List<StringIntPair> chatterFreqArrList;

    MainDatabase database;
    ChatLineDAO chatLineDao;
    WordDAO wordDao;

    CustomAdapter customAdapter;

    SharedPrefUtils spu;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_person_list);

        searchET = findViewById(R.id.chatterFreqET);
        searchLV = findViewById(R.id.chatterFreqLV);
        tb = findViewById(R.id.toolbar);

        spu = new SharedPrefUtils(this);

        numberFormat = NumberFormat.getInstance();
        numberFormat.setGroupingUsed(true);

        database = MainDatabase.getDatabase(this);
        chatLineDao = database.getChatLineDAO();
        wordDao = database.getWordDAO();
        cd = ChatData.getInstance(this);

        int totalCount = cd.getChatLineCount();
        freqList = new ArrayList<>();

        int pos = getIntent().getIntExtra("pos", 0);

        switch(pos){
            case 0:
                tb.setTitle("사람별 채팅량 분석");
                totalCount = cd.getChatLineCount();
                chatterFreqArrList = cd.getChatterFreqArrList();
                break;
            case 1:
                tb.setTitle("사람별 단어 수 분석");
                totalCount = cd.getTotalWordCount();
                chatterFreqArrList = wordDao.getTopChattersByWords();
                break;
            case 2:
                tb.setTitle("사람별 사진 공유 횟수 분석");
                totalCount = cd.getPicCount();
                chatterFreqArrList = wordDao.getTopChattersByPic();
                break;
            case 3:
                tb.setTitle("사람별 동영상 공유 횟수 분석");
                totalCount = cd.getVideoCount();
                chatterFreqArrList = wordDao.getTopChattersByVideo();
                break;
            case 4:
                tb.setTitle("사람별 링크 공유 횟수 분석");
                totalCount = cd.getLinkCount();
                chatterFreqArrList = wordDao.getTopChattersByLink();
                break;
            case 5:
                tb.setTitle("사람별 메세지 삭제 횟수 분석");
                totalCount = cd.getDeletedMsgCount();
                chatterFreqArrList = chatLineDao.getTopChattersByDeletedMsg();
                break;
        }



        for(StringIntPair element : chatterFreqArrList) freqList.add(element);

        customAdapter = new CustomAdapter(freqList, totalCount);
        searchLV.setAdapter(customAdapter);

        searchLV.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Intent personDtlIntent = new Intent(PersonListActivity.this, PersonDtlActivity.class);
                personDtlIntent.putExtra("word", freqList.get(i).getword());
                spu.saveString(R.string.SP_PERSON_DTL_NAME, freqList.get(i).getword());
                PersonListActivity.this.startActivity(personDtlIntent);
            }
        });

        searchET.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                String text = searchET.getText().toString();
                search(text);
            }
        });
    }

    public void search(String charText) {
        freqList.clear();
        if (charText.length() == 0) {
            freqList.addAll(chatterFreqArrList);
        } else
        {
            for(int i = 0; i < chatterFreqArrList.size(); i++)
            {
                if (chatterFreqArrList.get(i).getword().toLowerCase().contains(charText))
                {
                    freqList.add(chatterFreqArrList.get(i));
                }
            }
        }
        customAdapter.notifyDataSetChanged();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    class CustomAdapter extends BaseAdapter {

        List<StringIntPair> pairs;
        int totalCount;

        CustomAdapter(List<StringIntPair> pairs, int totalCount){
            this.pairs = pairs;
            this.totalCount = totalCount;
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

            convertView = getLayoutInflater().inflate(R.layout.list_view_elem_person_frequency, null);

            TextView titleTV = convertView.findViewById(R.id.personFreqElemTitleTV);
            TextView valueTV = convertView.findViewById(R.id.personFreqElemFreqTV);

            titleTV.setText(position+1 + ". " + pairs.get(position).getword());
            valueTV.setText(numberFormat.format(pairs.get(position).getFrequency()) + " (" + String.format("%.1f", (double)pairs.get(position).getFrequency()/totalCount*100) + "%)");

            return convertView;
        }
    }
}