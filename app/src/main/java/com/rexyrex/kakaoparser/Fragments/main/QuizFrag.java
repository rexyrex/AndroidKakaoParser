package com.rexyrex.kakaoparser.Fragments.main;

import android.app.Dialog;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.text.InputFilter;
import android.text.Spanned;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;

import com.google.android.gms.common.util.SharedPreferencesUtils;
import com.rexyrex.kakaoparser.Activities.MainActivity;
import com.rexyrex.kakaoparser.Activities.QuizActivity;
import com.rexyrex.kakaoparser.Database.DAO.AnalysedChatDAO;
import com.rexyrex.kakaoparser.Database.MainDatabase;
import com.rexyrex.kakaoparser.Database.Models.AnalysedChatModel;
import com.rexyrex.kakaoparser.Entities.ChatData;
import com.rexyrex.kakaoparser.Entities.HighscoreData;
import com.rexyrex.kakaoparser.Entities.StringStringPair;
import com.rexyrex.kakaoparser.R;
import com.rexyrex.kakaoparser.Utils.FileParseUtils;
import com.rexyrex.kakaoparser.Utils.FirebaseUtils;
import com.rexyrex.kakaoparser.Utils.LogUtils;
import com.rexyrex.kakaoparser.Utils.SharedPrefUtils;
import com.rexyrex.kakaoparser.Utils.StringParseUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class QuizFrag extends Fragment implements FirebaseUtils.NicknameCallback, FirebaseUtils.HighscoreCallback {
    ChatData cd;
    AnalysedChatModel acm;
    TextView quizScoreTV;

    SharedPrefUtils spu;

    Dialog nicknameDialog, highscoreDialog, instructionsDialog;
    Button ndCancelBtn, ndEnterBtn, highscoreCloseBtn, instructionsCloseBtn;
    TextView ndErrorMsgTv;
    EditText ndNickET;

    ListView highscoreLV, instructionsLV;
    List<HighscoreData> highscoreDataList;
    CustomAdapter ca;
    List<StringStringPair> instructionsDataList;
    InstructionsAdapter ia;

    public QuizFrag() {
        // Required empty public constructor
    }
    public static QuizFrag newInstance() {
        QuizFrag fragment = new QuizFrag();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            cd = ChatData.getInstance();
            acm = cd.getChatAnalyseDbModel();
            spu = new SharedPrefUtils(getContext());

            highscoreDialog = new Dialog(getContext());
            highscoreDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
            highscoreDialog.setContentView(R.layout.quiz_highscore_popup);
            highscoreDialog.getWindow().getAttributes().windowAnimations = R.style.FadeInAndFadeOut;

            highscoreLV = highscoreDialog.findViewById(R.id.quizHighscoreLV);
            highscoreDataList = new ArrayList<>();
            ca = new CustomAdapter(highscoreDataList);
            highscoreLV.setAdapter(ca);

            highscoreCloseBtn = highscoreDialog.findViewById(R.id.quizHighscoreCancelBtn);
            highscoreCloseBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    highscoreDialog.cancel();
                }
            });

            instructionsDialog = new Dialog(getContext());
            instructionsDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
            instructionsDialog.setContentView(R.layout.quiz_instructions_popup);
            instructionsDialog.getWindow().getAttributes().windowAnimations = R.style.FadeInAndFadeOut;

            instructionsLV = instructionsDialog.findViewById(R.id.quizInstructionsLV);
            instructionsDataList = new ArrayList<>();
            instructionsDataList.add(new StringStringPair(
                    "퀴즈가 뭐에요?",
                    "분석한 채팅 기준으로 무작위로 객관식 질문을 생성 후 사용자가 문제를 푸는 모드 입니다."
            ));
            instructionsDataList.add(new StringStringPair(
                    "총 몇 문제가 나오나요?",
                    "문제는 무작위로 생성되므로 무제한적입니다. 사용자는 기회가 3회 주어지며 3문제를 틀릴 시 퀴즈는 종료됩니다."
            ));
            instructionsDataList.add(new StringStringPair(
                    "퀴즈 시작이 안됩니다",
                    "퀴즈 시작 조건 미달 (채팅 최소 대화 횟수 1000, 최소 참여 인원 2 입니다)"
            ));
            instructionsDataList.add(new StringStringPair(
                    "점수 계산은 어떻게 되나요?",
                    "문제를 맞출때마다 점수가 오르는 양은 분석된 채팅의 길이, 참여 인원, 문제 유형을 고려하여 책정됩니다. \n" +
                            "(대화 길이가 길고, 참여인원이 많고, 문제 유형이 어려울수록 점수는 높아집니다)"
            ));
            instructionsDataList.add(new StringStringPair(
                    "화면에 보이는 최고점수랑 \"전체 랭킹\"에 보이는 최고 점수가 달라요",
                    "화면에 보이는 최고점수는 현재 분석된 채팅의 최고 점수이며 \"전체 랭킹\"에 보이는 점수는 모든 채팅 퀴즈 결과를 통합하여 가장 높은 점수가 기록됩니다."
            ));

            ia = new InstructionsAdapter(instructionsDataList);
            instructionsLV.setAdapter(ia);

            instructionsCloseBtn = instructionsDialog.findViewById(R.id.quizInstructionsCancelBtn);
            instructionsCloseBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    instructionsDialog.cancel();
                }
            });

            nicknameDialog = new Dialog(getContext());
            nicknameDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
            nicknameDialog.setContentView(R.layout.quiz_nickname_creation);
            nicknameDialog.getWindow().getAttributes().windowAnimations = R.style.FadeInAndFadeOut;
            nicknameDialog.setCancelable(false);

            ndCancelBtn = nicknameDialog.findViewById(R.id.quizNicknameCancelBtn);
            ndEnterBtn = nicknameDialog.findViewById(R.id.quizNicknameEnterBtn);
            ndErrorMsgTv = nicknameDialog.findViewById(R.id.quizNicknameErrorTV);
            ndNickET = nicknameDialog.findViewById(R.id.quizNicknameET);

            ndErrorMsgTv.setText("");

            InputFilter filter = new InputFilter() {
                @Override
                public CharSequence filter(CharSequence source, int start, int end,
                                           Spanned dest, int dstart, int dend) {
                    for (int i = start; i < end; i++) {
                        if (!Character.isLetterOrDigit(source.charAt(i))) {
                            return "";
                        }
                    }
                    return null;
                }
            };

            ndNickET.setFilters(new InputFilter[] { filter });

            ndCancelBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    ndNickET.setText("");
                    ndErrorMsgTv.setText("");
                    nicknameDialog.cancel();
                }
            });

            ndEnterBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    String attemptedNickname = ndNickET.getText().toString();
                    if(attemptedNickname.length() > 30 || attemptedNickname.length() < 2){
                        ndErrorMsgTv.setText("길이가 2글자 이상 30글자 이하여야 합니다.");
                    } else if(containsBadWords(attemptedNickname)){
                        ndErrorMsgTv.setText("바른 우리말을 사용해주세요");
                    } else {
                        FirebaseUtils.setNicknameCallback(QuizFrag.this);
                        FirebaseUtils.nicknameExists(attemptedNickname);
                    }
                }
            });
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_quiz, container, false);

        Button quizStartBtn = view.findViewById(R.id.quizStartBtn);
        quizStartBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(cd.getChatLineCount() < 1000){
                    Toast.makeText(QuizFrag.this.getActivity(), "대화 내용이 너무 짧아요", Toast.LENGTH_LONG).show();
                } else {
                    //Check if nickname exists on device
                    if(spu.getString(R.string.SP_QUIZ_NICKNAME, "-1").equals("-1")){
                        //nickname does not exist
                        nicknameDialog.show();
                    } else {
                        //Nickname exists -> Continue on with quiz
                        Intent moreIntent = new Intent(QuizFrag.this.getActivity(), QuizActivity.class);
                        startActivityForResult(moreIntent, 77);
                    }
                }
            }
        });

        Button quizInstructionsBtn = view.findViewById(R.id.quizInstructionsBtn);
        Button quizRankingBtn = view.findViewById(R.id.quizRankingBtn);

        quizInstructionsBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                spu.incInt(R.string.SP_QUIZ_INSTRUCTIONS_COUNT);
                instructionsDialog.show();
            }
        });

        quizRankingBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                spu.incInt(R.string.SP_QUIZ_SEE_RANKING_COUNT);
                highscoreDialog.show();
                FirebaseUtils.setHighscoreCallback(QuizFrag.this);
                FirebaseUtils.getHighscores();
            }
        });

        quizScoreTV = view.findViewById(R.id.quizFragScoreTV);
        quizScoreTV.setText("최고 기록 (개인) : " + acm.getHighscore() + "점");
        return view;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == 77){
            quizScoreTV.setText("최고 기록 (개인) : " + acm.getHighscore() + "점");
        }
    }

    public boolean containsBadWords(String attemptedNickname){
        String badWordsStr = "10새,10새기,10새리,10세리,10쉐이,10쉑,10스,10쌔,10쌔기,10쎄,10알,10창,10탱,18것,18넘,18년,18노,18놈,18뇬,18럼,18롬,18새,18새끼,18색,18세끼,18세리,18섹,18쉑,18스,18아,ㄱㅐ,ㄲㅏ,ㄲㅑ,ㄲㅣ,ㅅㅂㄹㅁ,ㅅㅐ,ㅆㅂㄹㅁ,ㅆㅍ,ㅆㅣ,ㅆ앙,ㅍㅏ,凸,갈보,갈보년,강아지,같은년,같은뇬,개같은,개구라,개년,개놈,개뇬,개대중,개독,개돼중,개랄,개보지,개뻥,개뿔,개새,개새기,개새끼,개새키,개색기,개색끼,개색키,개색히,개섀끼,개세,개세끼,개세이,개소리,개쑈, 개쇳기,개수작,개쉐,개쉐리,개쉐이,개쉑,개쉽,개스끼,개시키,개십새기,개십새끼,개쐑,개씹,개아들,개자슥,개자지,개접,개좆,개좌식,개허접,걔새,걔수작,걔시끼,걔시키,걔썌,걸레,게색기,게색끼,광뇬,구녕,구라,구멍,그년,그새끼,냄비,놈현,뇬,눈깔,뉘미럴,니귀미,니기미,니미,니미랄,니미럴,니미씹,니아배,니아베,니아비,니어매,니어메,니어미,닝기리,닝기미,대가리,뎡신,도라이,돈놈,돌아이,돌은놈,되질래,뒈져,뒈져라,뒈진,뒈진다,뒈질, 뒤질래,등신,디져라,디진다,디질래,딩시,따식,때놈,또라이,똘아이,똘아이,뙈놈,뙤놈,뙨넘,뙨놈,뚜쟁,띠바,띠발,띠불,띠팔,메친넘,메친놈,미췬,미췬,미친,미친넘,미친년,미친놈,미친새끼,미친스까이,미틴,미틴넘,미틴년,미틴놈,바랄년,병자,뱅마,뱅신,벼엉신,병쉰,병신,부랄,부럴,불알,불할,붕가,붙어먹,뷰웅,븅,븅신,빌어먹,빙시,빙신,빠가,빠구리,빠굴,빠큐,뻐큐,뻑큐,뽁큐,상넘이,상놈을,상놈의,상놈이,새갸,새꺄,새끼,새새끼,새키,색끼,생쑈,세갸,세꺄,세끼,섹스,쇼하네,쉐,쉐기,쉐끼,쉐리,쉐에기,쉐키,쉑,쉣,쉨,쉬발,쉬밸,쉬벌,쉬뻘,쉬펄,쉽알,스패킹,스팽,시궁창,시끼,시댕,시뎅,시랄,시발,시벌,시부랄,시부럴,시부리,시불,시브랄,시팍,시팔,시펄,신발끈,심발끈,심탱,십8,십라,십새,십새끼,십세,십쉐,십쉐이,십스키,십쌔,십창,십탱,싶알,싸가지,싹아지,쌉년,쌍넘,쌍년,쌍놈,쌍뇬,쌔끼,쌕,쌩쑈,쌴년,썅,썅년,썅놈,썡쇼,써벌,썩을년,썩을놈,쎄꺄,쎄엑,쒸벌,쒸뻘,쒸팔,쒸펄,쓰바,쓰박,쓰발,쓰벌,쓰팔,씁새,씁얼,씌파,씨8,씨끼,씨댕,씨뎅,씨바,씨바랄,씨박,씨발,씨방,씨방새,씨방세,씨밸,씨뱅,씨벌,씨벨,씨봉,씨봉알,씨부랄,씨부럴,씨부렁,씨부리,씨불,씨붕,씨브랄,씨빠,씨빨,씨뽀랄,씨앙,씨파,씨팍,씨팔,씨펄,씸년,씸뇬,씸새끼,씹같,씹년,씹뇬,씹보지,씹새,씹새기,씹새끼,씹새리,씹세,씹쉐,씹스키,씹쌔,씹이,씹자지,씹질,씹창,씹탱,씹퇭,씹팔,씹할,씹헐,아가리,아갈,아갈이,아갈통,아구창,아구통,아굴,얌마,양넘,양년,양놈,엄창,엠병,여물통,염병,엿같,옘병,옘빙,오입,왜년,왜놈,욤병,육갑,은년,을년,이년,이새끼,이새키,이스끼,이스키,임마,자슥,잡것,잡넘,잡년,잡놈,저년,저새끼,접년,젖밥,조까,조까치,조낸,조또,조랭,조빠,조쟁이,조지냐,조진다,조찐,조질래,존나,존나게,존니,존만,존만한,좀물,좁년,좆,좁밥,좃까,좃또,좃만,좃밥,좃이,좃찐,좆같,좆까,좆나,좆또,좆만,좆밥,좆이,좆찐,좇같,좇이,좌식,주글,주글래,주데이,주뎅,주뎅이,주둥아리,주둥이,주접,주접떨,죽고잡,죽을래,죽통,쥐랄,쥐롤,쥬디,지랄,지럴,지롤,지미랄,짜식,짜아식,쪼다,쫍빱,찌랄,창녀,캐년,캐놈,캐스끼,캐스키,캐시키,탱구,팔럼,퍽큐,호로,호로놈,호로새끼,호로색,호로쉑,호로스까이,호로스키,후라들,후래자식,후레,후뢰,씨ㅋ발,ㅆ1발,씌발,띠발,띄발,뛰발,띠ㅋ발,뉘뮈";
        String[] badWordsStrArr = badWordsStr.split(",");

        for(String badWord : badWordsStrArr){
            if(attemptedNickname.contains(badWord)){
                return true;
            }
        }
        return false;
    }

    @Override
    public void getNickname(String nickname) {
        if(nickname.equals("-1")){
            ndErrorMsgTv.setText("등록 완료");
            //save nickname to firebase
            FirebaseUtils.saveNickname(ndNickET.getText().toString(), spu);

            //save nickname locally
            spu.saveString(R.string.SP_QUIZ_NICKNAME, ndNickET.getText().toString());

            nicknameDialog.cancel();

            Intent moreIntent = new Intent(QuizFrag.this.getActivity(), QuizActivity.class);
            startActivityForResult(moreIntent, 77);
        } else {
            ndErrorMsgTv.setText("이미 등록되어있는 닉네임 입니다.");
        }
    }

    @Override
    public void getHighscores(List<HighscoreData> highscores) {
        highscoreDataList.clear();
        for(int i=0; i<highscores.size(); i++){
            highscoreDataList.add(highscores.get(i));
        }
        ca.notifyDataSetChanged();
    }

    class CustomAdapter extends BaseAdapter {
        List<HighscoreData> highscores;

        CustomAdapter(List<HighscoreData> highscores){
            this.highscores = highscores;
        }

        @Override
        public int getCount() {
            return highscores.size();
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
            convertView = getLayoutInflater().inflate(R.layout.list_view_elem_quiz_highscore, null);
            ConstraintLayout cl = convertView.findViewById(R.id.quizHighscoreCV);

            TextView nickTV = convertView.findViewById(R.id.quizHighscoreLVElemNicknameTV);
            TextView scoreTV = convertView.findViewById(R.id.quizHighscoreLVElemScoreTV);

            if( highscores.get(position).getNickname().equals(spu.getString(R.string.SP_QUIZ_NICKNAME, "-1"))){
                cl.setBackground(QuizFrag.this.getActivity().getDrawable(R.drawable.custom_show_more_btn_highlighted));
            }

            nickTV.setText("" + (position+1) + ". " + highscores.get(position).getNickname());
            scoreTV.setText("" + highscores.get(position).getHighscore());

            return convertView;
        }
    }

    class InstructionsAdapter extends BaseAdapter {
        List<StringStringPair> instructions;

        InstructionsAdapter(List<StringStringPair> instructions){
            this.instructions = instructions;
        }

        @Override
        public int getCount() {
            return instructions.size();
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
            convertView = getLayoutInflater().inflate(R.layout.list_view_elem_quiz_instructions, null);
            TextView qTV = convertView.findViewById(R.id.lveQTV);
            TextView aTV = convertView.findViewById(R.id.lveATV);

            qTV.setText("Q - " + instructions.get(position).getTitle());
            aTV.setText("A - " + instructions.get(position).getValue());

            return convertView;
        }
    }
}