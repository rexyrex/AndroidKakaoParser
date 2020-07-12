package com.rexyrex.kakaoparser.Activities;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import com.rexyrex.kakaoparser.Entities.ChatData;
import com.rexyrex.kakaoparser.Entities.StringIntPair;
import com.rexyrex.kakaoparser.R;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;

public class PersonListActivity extends AppCompatActivity {

    EditText searchET;
    ListView searchLV;

    NumberFormat numberFormat;

    ChatData cd;

    ArrayList<StringIntPair> freqList;
    private List<StringIntPair> chatterFreqArrList;


    CustomAdapter customAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_person_list);

        searchET = findViewById(R.id.chatterFreqET);
        searchLV = findViewById(R.id.chatterFreqLV);

        numberFormat = NumberFormat.getInstance();
        numberFormat.setGroupingUsed(true);

        cd = ChatData.getInstance();
        int totalCount = cd.getChatLineCount();

        freqList = new ArrayList<>();

        chatterFreqArrList = cd.getChatterFreqArrList();

        for(StringIntPair element : chatterFreqArrList) freqList.add(element);

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