package com.rexyrex.kakaoparser.Fragments;

import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.rexyrex.kakaoparser.Activities.ChatStatsTabActivity;
import com.rexyrex.kakaoparser.Activities.MainActivity;
import com.rexyrex.kakaoparser.Entities.ChatData;
import com.rexyrex.kakaoparser.Entities.Pair;
import com.rexyrex.kakaoparser.R;
import com.rexyrex.kakaoparser.Utils.FileParseUtils;
import com.rexyrex.kakaoparser.Utils.LogUtils;
import com.rexyrex.kakaoparser.Utils.StringParseUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Queue;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link WordAnalyseFrag#newInstance} factory method to
 * create an instance of this fragment.
 */
public class WordAnalyseFrag extends Fragment {

    private static final String ARG_PARAM1 = "param1";

    private ChatData cd;
    ArrayList<Pair> freqList;
    WordListAdapter ca;
    TextView wordCountTV;

    public WordAnalyseFrag() {
        // Required empty public constructor
    }


    public static WordAnalyseFrag newInstance(ChatData param1) {
        WordAnalyseFrag fragment = new WordAnalyseFrag();
        Bundle args = new Bundle();
        args.putParcelable(ARG_PARAM1, param1);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            cd = getArguments().getParcelable(ARG_PARAM1);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_word_analyse, container, false);

        ListView wordLV = view.findViewById(R.id.wordSearchLV);
        final EditText wordSearchET = view.findViewById(R.id.wordSearchET);
        wordCountTV = view.findViewById(R.id.wordSearchResTV);

        freqList = new ArrayList<>();

        for(Pair element : cd.getWordFreqArrList()) freqList.add(element);

        ca = new WordListAdapter(freqList);
        wordLV.setAdapter(ca);
        wordCountTV.setText("검색 결과 " + freqList.size() + "건");


        wordLV.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

            }
        });

        wordSearchET.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                String text = wordSearchET.getText().toString();
                search(text);
            }
        });

        return view;
    }

    public void search(String charText) {
        freqList.clear();
        if (charText.length() == 0) {
            freqList.addAll(cd.getWordFreqArrList());
        } else
        {
            for(int i = 0;i < cd.getWordFreqArrList().size(); i++)
            {
                if (cd.getWordFreqArrList().get(i).getword().toLowerCase().contains(charText))
                {
                    freqList.add(cd.getWordFreqArrList().get(i));
                }
            }
        }
        wordCountTV.setText("검색 결과 " + freqList.size() + "건");
        ca.notifyDataSetChanged();
    }

    class WordListAdapter extends BaseAdapter {
        ArrayList<Pair> wordFreqArrList;

        WordListAdapter(ArrayList<Pair> wordFreqArrList){
            this.wordFreqArrList = wordFreqArrList;
        }

        @Override
        public int getCount() {
            return wordFreqArrList.size();
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
            convertView = getLayoutInflater().inflate(R.layout.list_view_elem_word, null);
            TextView wordTV = convertView.findViewById(R.id.wordListElemWordTV);
            TextView wordFreqTV = convertView.findViewById(R.id.wordListElemFreqTV);

            Pair wordData = wordFreqArrList.get(position);
            wordTV.setText(wordData.getword());
            wordFreqTV.setText(wordData.getFrequency() + "회");
            return convertView;
        }
    }
}