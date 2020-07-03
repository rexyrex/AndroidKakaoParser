package com.rexyrex.kakaoparser.Fragments;

import android.content.Intent;
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
import android.widget.ListView;
import android.widget.TextView;

import com.rexyrex.kakaoparser.Activities.WordDetailAnalyseActivity;
import com.rexyrex.kakaoparser.Entities.ChatData;
import com.rexyrex.kakaoparser.Entities.StringIntPair;
import com.rexyrex.kakaoparser.R;
import com.rexyrex.kakaoparser.Utils.LogUtils;

import java.util.ArrayList;

public class WordAnalyseFrag extends Fragment {

    private static final String ARG_PARAM1 = "param1";

    private ChatData cd;
    ArrayList<StringIntPair> freqList;
    WordListAdapter ca;
    TextView wordCountTV;

    public WordAnalyseFrag() {
        // Required empty public constructor
    }


    public static WordAnalyseFrag newInstance() {
        WordAnalyseFrag fragment = new WordAnalyseFrag();
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
            cd = ChatData.getInstance();
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

        for(StringIntPair element : cd.getWordFreqArrList()) freqList.add(element);

        ca = new WordListAdapter(freqList);
        wordLV.setAdapter(ca);
        wordCountTV.setText("검색 결과 " + freqList.size() + "건");


        wordLV.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                LogUtils.e("\"" + cd.getWordFreqArrList().get(position).getword() + "\"");
                //WordDetailAnalyseActivity.wordFreqMap = cd.getWordFreqMap();
                Intent wordDtlIntent = new Intent(WordAnalyseFrag.this.getActivity(), WordDetailAnalyseActivity.class);
                wordDtlIntent.putExtra("word", freqList.get(position).getword());
                WordAnalyseFrag.this.getActivity().startActivity(wordDtlIntent);

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
        ArrayList<StringIntPair> wordFreqArrList;

        WordListAdapter(ArrayList<StringIntPair> wordFreqArrList){
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
            TextView wordTV = convertView.findViewById(R.id.chatLineExampleSentenceTV);
            TextView wordFreqTV = convertView.findViewById(R.id.wordListElemFreqTV);

            StringIntPair wordData = wordFreqArrList.get(position);
            wordTV.setText(wordData.getword());
            wordFreqTV.setText(wordData.getFrequency() + "회");
            return convertView;
        }
    }
}