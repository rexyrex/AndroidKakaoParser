package com.rexyrex.kakaoparser.Activities.DetailActivities;

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

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.rexyrex.kakaoparser.Database.DAO.ChatLineDAO;
import com.rexyrex.kakaoparser.Database.DAO.WordDAO;
import com.rexyrex.kakaoparser.Database.MainDatabase;
import com.rexyrex.kakaoparser.Entities.ChatData;
import com.rexyrex.kakaoparser.Entities.StringIntPair;
import com.rexyrex.kakaoparser.Fragments.person.PWordFrag;
import com.rexyrex.kakaoparser.R;
import com.rexyrex.kakaoparser.Utils.SharedPrefUtils;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;

public class PersonWordListActivity extends AppCompatActivity {

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
        setContentView(R.layout.activity_person_word_list);

        searchET = findViewById(R.id.personWordET);
        searchLV = findViewById(R.id.personWordLV);
        tb = findViewById(R.id.toolbar);

        spu = new SharedPrefUtils(this);

        numberFormat = NumberFormat.getInstance();
        numberFormat.setGroupingUsed(true);

        database = MainDatabase.getDatabase(this);
        chatLineDao = database.getChatLineDAO();
        wordDao = database.getWordDAO();
        cd = ChatData.getInstance(this);


        freqList = new ArrayList<>();

        String author = spu.getString(R.string.SP_PERSON_DTL_NAME, "");

        chatterFreqArrList = wordDao.getWordsByAuthor(author);
        int totalCount = wordDao.getDistinctWordCountByAuthor(author);

        for(StringIntPair element : chatterFreqArrList) freqList.add(element);

        customAdapter = new CustomAdapter(freqList, totalCount);
        searchLV.setAdapter(customAdapter);

        searchLV.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Intent wordDtlIntent = new Intent(PersonWordListActivity.this, WordDetailAnalyseActivity.class);
                wordDtlIntent.putExtra("word", freqList.get(i).getword());
                PersonWordListActivity.this.startActivity(wordDtlIntent);
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