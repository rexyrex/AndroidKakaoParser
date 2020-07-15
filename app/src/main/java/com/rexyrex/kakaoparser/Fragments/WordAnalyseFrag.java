package com.rexyrex.kakaoparser.Fragments;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.rexyrex.kakaoparser.Activities.WordDetailAnalyseActivity;
import com.rexyrex.kakaoparser.Database.DAO.ChatLineDAO;
import com.rexyrex.kakaoparser.Database.DAO.WordDAO;
import com.rexyrex.kakaoparser.Database.MainDatabase;
import com.rexyrex.kakaoparser.Entities.ChatData;
import com.rexyrex.kakaoparser.Entities.StringIntPair;
import com.rexyrex.kakaoparser.R;
import com.rexyrex.kakaoparser.Utils.KeyboardUtils;
import com.rexyrex.kakaoparser.Utils.LogUtils;

import java.util.ArrayList;
import java.util.List;

public class WordAnalyseFrag extends Fragment {

    private static final String ARG_PARAM1 = "param1";

    ArrayList<StringIntPair> freqList;
    WordListAdapter ca;
    TextView wordCountTV;

    ChatData cd;

    private MainDatabase database;
    private WordDAO wordDao;

    ListView wordLV;

    private List<StringIntPair> wordFreqArrList;

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
            database = MainDatabase.getDatabase(getContext());
            wordDao = database.getWordDAO();
            cd = ChatData.getInstance();
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_word_analyse, container, false);

        wordLV = view.findViewById(R.id.wordSearchLV);
        final EditText wordSearchET = view.findViewById(R.id.wordSearchET);
        wordCountTV = view.findViewById(R.id.wordSearchResTV);

        freqList = new ArrayList<>();

        wordFreqArrList = cd.getWordFreqArrList();

        for(StringIntPair element : wordFreqArrList) freqList.add(element);

        ca = new WordListAdapter(freqList);
        wordLV.setAdapter(ca);
        wordCountTV.setText("검색 결과 " + freqList.size() + "건");


        wordLV.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent wordDtlIntent = new Intent(WordAnalyseFrag.this.getActivity(), WordDetailAnalyseActivity.class);
                wordDtlIntent.putExtra("word", freqList.get(position).getword());
                WordAnalyseFrag.this.getActivity().startActivity(wordDtlIntent);

            }
        });

        wordSearchET.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View view, int i, KeyEvent keyEvent) {
                if ((keyEvent.getAction() == KeyEvent.ACTION_DOWN) &&
                        (keyEvent.getKeyCode() == KeyEvent.KEYCODE_ENTER)) {
                    // Perform action on key press
                    String text = wordSearchET.getText().toString();
                    search(text);

                    KeyboardUtils.hideKeyboard(getActivity());
                    return true;
                }
                return false;
            }
        });

        return view;
    }

    public void search(String charText) {
        freqList.clear();
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
            freqList.addAll(cd.getWordFreqArrList());
        } else {
            freqList.addAll(wordDao.searchFreqWordList(charText));
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
            TextView wordTV = convertView.findViewById(R.id.elemWordTitleTV);
            TextView wordFreqTV = convertView.findViewById(R.id.elemWordFreqTV);

            StringIntPair wordData = wordFreqArrList.get(position);
            wordTV.setText(position+1 + ". " + wordData.getword());
            wordFreqTV.setText(wordData.getFrequency() + "회");
            return convertView;
        }
    }
}