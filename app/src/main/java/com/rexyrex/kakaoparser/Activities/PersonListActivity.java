package com.rexyrex.kakaoparser.Activities;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import com.rexyrex.kakaoparser.Database.DAO.ChatLineDAO;
import com.rexyrex.kakaoparser.Database.MainDatabase;
import com.rexyrex.kakaoparser.Entities.StringIntPair;
import com.rexyrex.kakaoparser.Fragments.PersonAnalyseFrag;
import com.rexyrex.kakaoparser.R;

import java.util.ArrayList;
import java.util.List;

public class PersonListActivity extends AppCompatActivity {

    EditText searchET;
    ListView searchLV;

    private MainDatabase database;
    private ChatLineDAO chatLineDao;

    ArrayList<StringIntPair> freqList;
    private List<StringIntPair> wordFreqArrList;

    CustomAdapter customAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_person_list);

        database = MainDatabase.getDatabase(this);
        chatLineDao = database.getChatLineDAO();

        searchET = findViewById(R.id.chatterFreqET);
        searchLV = findViewById(R.id.chatterFreqLV);

        int totalCount = chatLineDao.getCount();

        freqList = new ArrayList<>();

        wordFreqArrList = chatLineDao.getChatterFrequencyPairs();

        for(StringIntPair element : wordFreqArrList) freqList.add(element);

        customAdapter = new CustomAdapter(freqList, totalCount);
        searchLV.setAdapter(customAdapter);

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
            freqList.addAll(wordFreqArrList);
        } else
        {
            for(int i = 0;i < wordFreqArrList.size(); i++)
            {
                if (wordFreqArrList.get(i).getword().toLowerCase().contains(charText))
                {
                    freqList.add(wordFreqArrList.get(i));
                }
            }
        }
        customAdapter.notifyDataSetChanged();
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
            valueTV.setText(pairs.get(position).getFrequency() + " (" + String.format("%.1f", (double)pairs.get(position).getFrequency()/totalCount*100) + "%)");

            return convertView;
        }
    }
}