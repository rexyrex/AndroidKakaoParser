package com.rexyrex.kakaoparser.Fragments.main;

import android.app.Dialog;
import android.content.Intent;
import android.net.Uri;
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
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.rexyrex.kakaoparser.Activities.QuizActivity;
import com.rexyrex.kakaoparser.Activities.QuizHighscoreActivity;
import com.rexyrex.kakaoparser.Activities.QuizInstructionsActivity;
import com.rexyrex.kakaoparser.Activities.SendOpinionActivity;
import com.rexyrex.kakaoparser.Database.Models.AnalysedChatModel;
import com.rexyrex.kakaoparser.Entities.ChatData;
import com.rexyrex.kakaoparser.Entities.StringIntPair;
import com.rexyrex.kakaoparser.Entities.StringStringPair;
import com.rexyrex.kakaoparser.R;
import com.rexyrex.kakaoparser.Utils.FirebaseUtils;
import com.rexyrex.kakaoparser.Utils.SharedPrefUtils;

import java.util.ArrayList;
import java.util.List;

public class QuizFrag extends Fragment implements FirebaseUtils.NicknameCallback{
    ChatData cd;
    AnalysedChatModel acm;
    TextView quizScoreTV;
    TextView opinionTV;

    SharedPrefUtils spu;

    Dialog nicknameDialog, reviewSuggestDialog;
    Button ndCancelBtn, ndEnterBtn;
    TextView ndErrorMsgTv;
    EditText ndNickET;

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

            reviewSuggestDialog = new Dialog(getContext());
            reviewSuggestDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
            reviewSuggestDialog.setContentView(R.layout.basic_popup);
            reviewSuggestDialog.getWindow().getAttributes().windowAnimations = R.style.FadeInAndFadeOut;
            reviewSuggestDialog.setCancelable(false);

            Button reviewPopupCancelBtn = reviewSuggestDialog.findViewById(R.id.basicPopupCancelBtn);
            TextView reviewPopupTitleTV = reviewSuggestDialog.findViewById(R.id.basicPopupTitle);
            TextView reviewPopupContentsTV = reviewSuggestDialog.findViewById(R.id.basicPopupContents);
            Button reviewPopupGoReviewBtn = reviewSuggestDialog.findViewById(R.id.basicPopupBtn);

            reviewPopupTitleTV.setText("카톡 정밀 분석기를 잘 이용하고 계신가요?");
            reviewPopupContentsTV.setText("앱 리뷰를 해주세요~ \n리뷰 하나 하나가 큰 도움이 됩니다!");
            reviewPopupGoReviewBtn.setText("리뷰 하기");
            reviewPopupGoReviewBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    String appPackageName = "com.rexyrex.kakaoparser";
                    try {
                        getActivity().startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + appPackageName)));
                    } catch (android.content.ActivityNotFoundException anfe) {
                        getActivity().startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=" + appPackageName)));
                    }
                }
            });
            reviewPopupCancelBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    reviewSuggestDialog.cancel();
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
                    if(attemptedNickname.length() > 15 || attemptedNickname.length() < 2){
                        ndErrorMsgTv.setText("길이가 2글자 이상 15글자 이하여야 합니다.");
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
                //get percentage of most chatter 점유율
                //(we dont want a chat where one person does most of the speaking)
                List<StringIntPair> tmpChatFreqList = cd.getChatterFreqArrList();
                int maxFreq = 0;
                for(StringIntPair sip : tmpChatFreqList){
                    if(sip.getFrequency() > maxFreq){
                        maxFreq = sip.getFrequency();
                    }
                }

                if(cd.getChatLineCount() < 1000){
                    Toast.makeText(QuizFrag.this.getActivity(), "대화 내용이 너무 짧아요", Toast.LENGTH_LONG).show();
                } else if(cd.getChatterCount() < 2){
                    Toast.makeText(QuizFrag.this.getActivity(), "최소 대화인원이 2명이여야 해요", Toast.LENGTH_LONG).show();
                } else if((double) maxFreq / cd.getChatLineCount() > 0.8) {
                    Toast.makeText(QuizFrag.this.getActivity(), "모든 사람이 골고루 대화한 채팅으로만 가능합니다", Toast.LENGTH_LONG).show();
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
        Button quizMyRankingBtn = view.findViewById(R.id.quizMyRankingBtn);

        quizInstructionsBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                spu.incInt(R.string.SP_QUIZ_INSTRUCTIONS_COUNT);
                Intent intent = new Intent(QuizFrag.this.getContext(), QuizInstructionsActivity.class);
                startActivity(intent);
            }
        });

        quizMyRankingBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(QuizFrag.this.getContext(), QuizHighscoreActivity.class);
                intent.putExtra("my", true);
                startActivity(intent);
            }
        });

        quizRankingBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(QuizFrag.this.getContext(), QuizHighscoreActivity.class);
                intent.putExtra("my", false);
                startActivity(intent);
            }
        });

        quizScoreTV = view.findViewById(R.id.quizFragScoreTV);
        quizScoreTV.setText(cd.getChatFileTitle() + "\n" + "퀴즈 기록 : " + acm.getHighscore() + "점");

        opinionTV = view.findViewById(R.id.quizFragOpinionTV);
        opinionTV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(QuizFrag.this.getContext(), SendOpinionActivity.class);
                startActivity(intent);
            }
        });
        return view;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == 77){
            quizScoreTV.setText(cd.getChatFileTitle() + "\n" + "퀴즈 기록 : " + acm.getHighscore() + "점");

            //Show review popup if conditions are met
            if(spu.getBool(R.string.SP_REVIEW_POPUP_SHOW, true) && spu.getInt(R.string.SP_QUIZ_FINISH_COUNT, 0) >= 5){
                reviewSuggestDialog.show();
                //If review popup was showed at least once, do not show
                spu.saveBool(R.string.SP_REVIEW_POPUP_SHOW, false);
            }
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


}