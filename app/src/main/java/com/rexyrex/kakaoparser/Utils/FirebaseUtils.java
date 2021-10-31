package com.rexyrex.kakaoparser.Utils;

import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.telephony.TelephonyManager;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.firebase.crashlytics.FirebaseCrashlytics;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.SetOptions;
import com.google.firebase.firestore.Transaction;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.rexyrex.kakaoparser.BuildConfig;
import com.rexyrex.kakaoparser.Constants.DateFormats;
import com.rexyrex.kakaoparser.Database.DAO.AnalysedChatDAO;
import com.rexyrex.kakaoparser.Database.MainDatabase;
import com.rexyrex.kakaoparser.Entities.ChatData;
import com.rexyrex.kakaoparser.Entities.HighscoreData;
import com.rexyrex.kakaoparser.R;

import java.lang.reflect.Type;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class FirebaseUtils {
    public static NicknameCallback nicknameCallback;
    public static HighscoreCallback highscoreCallback;

    public static void logFirebaseEventOpenApp(Context c){
        FirebaseAnalytics mFirebaseAnalytics = FirebaseAnalytics.getInstance(c);
        Bundle bundle = new Bundle();
        mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.APP_OPEN, bundle);
    }

    public static void logFirebaseEventShare(Context c, String shareType){
        FirebaseAnalytics mFirebaseAnalytics = FirebaseAnalytics.getInstance(c);
        Bundle bundle = new Bundle();
        bundle.putString(FirebaseAnalytics.Param.ITEM_ID, "rex_share");
        bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, shareType);
        mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.SHARE, bundle);
    }

    public static void updateUserInfo(Context c, SharedPrefUtils spu, String type, MainDatabase database){
        if(!spu.getBool(R.string.SP_FB_BOOL_USERS2, true)){
            return;
        }
        List<String> analysedChatTitles = database.getAnalysedChatDAO().getAllChatTitles();
        String firebaseToken = spu.getString(R.string.SP_FB_TOKEN, "null");
        String uuid = spu.getString(R.string.SP_UUID, "none");
        String nickname = spu.getString(R.string.SP_QUIZ_NICKNAME, "none");

        String manufacturer = Build.MANUFACTURER;
        String model = Build.MODEL;
        int version = Build.VERSION.SDK_INT;
        TelephonyManager manager = (TelephonyManager)c.getSystemService(Context.TELEPHONY_SERVICE);
        String carrierName = manager.getNetworkOperatorName();

        FirebaseFirestore db = FirebaseFirestore.getInstance();

        Date date = new Date();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy년 M월 d일 a hh:mm", Locale.KOREAN);

        Map<String, Object> user = new HashMap<>();
        user.put("AnalysedChatTitles", analysedChatTitles);

        user.put("Manufacturer", manufacturer);
        user.put("Model", model);
        user.put("Android Version", version);
        user.put("Carrier Name", carrierName);
        user.put("App Version", BuildConfig.VERSION_NAME);
        user.put("Login Count", spu.getInt(R.string.SP_LOGIN_COUNT, 0));
        user.put("Logout Count", spu.getInt(R.string.SP_LOGOUT_COUNT, 0));
        user.put("Analyze Count", spu.getInt(R.string.SP_ANALYSE_COUNT, 0));
        user.put("Load Count", spu.getInt(R.string.SP_LOAD_COUNT, 0));
        user.put("LastChangeDt", sdf.format(date));
        user.put("LastChangeServerDt", FieldValue.serverTimestamp());
        user.put("FirebaseToken", firebaseToken);
        if(firebaseToken.contains("Firebase")){
            user.put("FirebaseError", true);
        } else {
            user.put("FirebaseError", false);
        }

        Type t = new TypeToken< List <String> >() {}.getType();
        ArrayList<String> paths = new Gson().fromJson(spu.getString(R.string.SP_FB_PATHS, ""),t);
        user.put("savePaths", paths);
        user.put("uuid", uuid);
        user.put("nickname", nickname);
        user.put("Save Action", type);
        user.put("Exported Chat Count", spu.getInt(R.string.SP_EXPORTED_CHAT_COUNT, -1));

        user.put("LastLoginDt", spu.getString(R.string.SP_LOGIN_DT, "null"));
        user.put("LastLogoutDt", spu.getString(R.string.SP_LOGOUT_DT, "null"));
        user.put("FrstRegistDt", spu.getString(R.string.SP_REGIST_DT, "null"));

        user.put("ReviewShow", spu.getBool(R.string.SP_REVIEW_REQUESTED, false));
        user.put("ReviewSuccess", spu.getBool(R.string.SP_REVIEW_COMPLETED, false));

        user.put("SettingBtnCount", spu.getInt(R.string.SP_OPEN_SETTINGS_COUNT, 0));
        user.put("KakaoBtnCount", spu.getInt(R.string.SP_OPEN_KAKAO_COUNT, 0));
        user.put("InstructionsBtnCount", spu.getInt(R.string.SP_OPEN_HOW_TO_COUNT, 0));
        user.put("PrivacyPolicyBtnCount", spu.getInt(R.string.SP_OPEN_PRIV_POLICY_COUNT, 0));

        user.put("DeleteCount", spu.getInt(R.string.SP_DELETE_CHAT_COUNT, 0));

        user.put("ShareGeneralCount", spu.getInt(R.string.SP_SHARE_GENERAL_ANALZ_COUNT, 0));
        user.put("SharePersonCount", spu.getInt(R.string.SP_SHARE_PERSON_ANALZ_COUNT, 0));
        user.put("ShareWordCount", spu.getInt(R.string.SP_SHARE_WORD_ANALZ_COUNT, 0));

        user.put("ShareTimeTime", spu.getInt(R.string.SP_SHARE_TIME_ANALZ_1_COUNT, 0));
        user.put("ShareTimeDayOfWeek", spu.getInt(R.string.SP_SHARE_TIME_ANALZ_2_COUNT, 0));
        user.put("ShareTimeDay", spu.getInt(R.string.SP_SHARE_TIME_ANALZ_3_COUNT, 0));
        user.put("ShareTimeMonth", spu.getInt(R.string.SP_SHARE_TIME_ANALZ_4_COUNT, 0));
        user.put("ShareTimeYear", spu.getInt(R.string.SP_SHARE_TIME_ANALZ_5_COUNT, 0));

        db.collection("users2").document(uuid)
                .set(user)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {

                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        //LogUtils.e("Error adding document" + e.getMessage());
                        e.printStackTrace();
                    }
                });
    }

    public static void setNicknameCallback(NicknameCallback nc){
        nicknameCallback = nc;
    }

    public static void setHighscoreCallback(HighscoreCallback nc){
        highscoreCallback = nc;
    }

    public static void saveNickname(String nickname, SharedPrefUtils spu){
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        String firebaseToken = spu.getString(R.string.SP_FB_TOKEN, "null");
        String uuid = spu.getString(R.string.SP_UUID, "none");

        Map<String, Object> quizEntry = new HashMap<>();
        quizEntry.put("uuid", uuid);
        quizEntry.put("nickname", nickname);
        quizEntry.put("firebaseToken", firebaseToken);
        quizEntry.put("highscore", 0);

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-M-d HH:mm:ss", Locale.KOREAN);
        quizEntry.put("nicknameUpdateDt", sdf.format(new Date()));

        db.collection("quiz").document(firebaseToken)
                .set(quizEntry)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {

                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        //LogUtils.e("Error adding document" + e.getMessage());
                        e.printStackTrace();
                    }
                });
    }

    public static void nicknameExists(String nickname){
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        CollectionReference citiesRef = db.collection("quiz");
        Query query = citiesRef.whereEqualTo("nickname", nickname);
        query.get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            if(task.getResult().isEmpty()){
                                nicknameCallback.getNickname("-1");
                            }
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                LogUtils.e(document.getId() + " => " + document.getData());
                                nicknameCallback.getNickname((String) document.getData().get("nickname"));
                            }
                        } else {
                            nicknameCallback.getNickname("-1");
                            LogUtils.e("Error getting documents: " + task.getException().toString());
                        }
                    }
                });
    }

    public interface NicknameCallback{
        void getNickname(String nickname);
    }

    public interface HighscoreCallback{
        void getHighscores(List<HighscoreData> highscores, int myScore);
    }

    public static void saveChatStats(SharedPrefUtils spu, ChatData cd, String dateRangeStr){
        if(!spu.getBool(R.string.SP_FB_BOOL_SAVE_CHAT_FIRESTORE, true)){
            return;
        }
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        String firebaseToken = spu.getString(R.string.SP_FB_TOKEN, "null");
        String uuid = spu.getString(R.string.SP_UUID, "none");

        Map<String, Object> chatEntry = new HashMap<>();

        //chatEntry.put("nickname", spu.getString(R.string.SP_QUIZ_NICKNAME, "-1"));
        //chatEntry.put("firebaseToken", firebaseToken);
        chatEntry.put("uuid", uuid);

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-M-d HH:mm:ss", Locale.KOREAN);
        chatEntry.put("date", sdf.format(new Date()));
        chatEntry.put("serverDt", FieldValue.serverTimestamp());

        chatEntry.put("ChatDateRange", dateRangeStr);
        //chatEntry.put("ChatterCount", cd.getChatterCount());
        chatEntry.put("ChatLineCount", cd.getChatLineCount());
        chatEntry.put("ChatTitle", cd.getChatFileTitle());
        //chatEntry.put("ChatWordCount", cd.getWordCount());
        chatEntry.put("ChatDayCount", cd.getDayCount());
        chatEntry.put("ChatDeletedMsgCount", cd.getDeletedMsgCount());

        //chatEntry.put("ChatLinkCount", cd.getLinkCount());
        chatEntry.put("ChatPicCount", cd.getPicCount());
        chatEntry.put("ChatVideoCount", cd.getVideoCount());
        //chatEntry.put("ChatPowerpointCount", cd.getPptCount());

        chatEntry.put("ChatAnalyseDuration", cd.getLoadElapsedSeconds());

        chatEntry.put("Top10Chatters", cd.getTop10Chatters());

        db.collection("chats").document()
                .set(chatEntry)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {

                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        //LogUtils.e("Error adding document" + e.getMessage());
                        e.printStackTrace();
                    }
                });
    }

    public static void updateMonthlyHighscore(int score, SharedPrefUtils spu){
        String yearMonthStr = getYearMonthStr();
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        String firebaseToken = spu.getString(R.string.SP_FB_TOKEN, "null");
        String uuid = spu.getString(R.string.SP_UUID, "none");
        DocumentReference monthlyDoc = db.collection("quiz").document(firebaseToken);

        db.runTransaction(new Transaction.Function<Void>() {
            @Override
            public Void apply(Transaction transaction) throws FirebaseFirestoreException {
                DocumentSnapshot snapshot = transaction.get(monthlyDoc);

                if(snapshot.exists()){
                    if(snapshot.contains(yearMonthStr)){
                        double serverScore = snapshot.getDouble(yearMonthStr);
                        if(score > serverScore){
                            transaction.update(monthlyDoc, yearMonthStr, score);
                        }
                    } else {
                        Map<String, Object> data = new HashMap<>();
                        data.put(yearMonthStr, score);
                        transaction.set(monthlyDoc, data, SetOptions.merge());
                    }

                }

                return null;
            }
        }).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {

            }
        })
        .addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {

            }
        });

    }

    public static void saveHighscore(int score, SharedPrefUtils spu, ChatData cd){
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        String firebaseToken = spu.getString(R.string.SP_FB_TOKEN, "null");
        String uuid = spu.getString(R.string.SP_UUID, "none");
        String yearMonthStr = getYearMonthStr();

        DocumentReference scoreDoc = db.collection("quiz").document(firebaseToken);

        Map<String, Object> quizEntry = new HashMap<>();
        quizEntry.put("nickname", spu.getString(R.string.SP_QUIZ_NICKNAME, "-1"));
        //quizEntry.put("firebaseToken", firebaseToken);
        quizEntry.put("uuid", uuid);
        quizEntry.put("highscore", score);

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-M-d HH:mm:ss", Locale.KOREAN);
        quizEntry.put("highscoreUpdateDt", sdf.format(new Date()));
        quizEntry.put("highscoreUpdateServerDt", FieldValue.serverTimestamp());

        quizEntry.put("quizStartCount", spu.getInt(R.string.SP_QUIZ_START_COUNT, 0));
        quizEntry.put("quizLoadQuestionCount", spu.getInt(R.string.SP_QUIZ_LOAD_QUESTION_COUNT, 0));
        quizEntry.put("quizCorrectCount", spu.getInt(R.string.SP_QUIZ_CORRECT_COUNT, 0));
        quizEntry.put("quizWrongCount", spu.getInt(R.string.SP_QUIZ_WRONG_COUNT, 0));
        quizEntry.put("quizFinishCount", spu.getInt(R.string.SP_QUIZ_FINISH_COUNT, 0));
        quizEntry.put("quizShareQuestionCount", spu.getInt(R.string.SP_QUIZ_SHARE_QUESTION_COUNT, 0));
        quizEntry.put("quizInstructionsCount", spu.getInt(R.string.SP_QUIZ_INSTRUCTIONS_COUNT, 0));
        quizEntry.put("quizSeeOnlineRankingCount", spu.getInt(R.string.SP_QUIZ_SEE_ONLINE_RANKING_COUNT, 0));
//        quizEntry.put("quizSeeMyRankingCount", spu.getInt(R.string.SP_QUIZ_SEE_MY_RANKING_COUNT, 0));

//        quizEntry.put("quizQ1CorrectCount", spu.getInt(R.string.SP_QUIZ_Q1_CORRECT_COUNT, 0));
//        quizEntry.put("quizQ1TotalCount", spu.getInt(R.string.SP_QUIZ_Q1_TOTAL_COUNT, 0));
//        quizEntry.put("quizQ2CorrectCount", spu.getInt(R.string.SP_QUIZ_Q2_CORRECT_COUNT, 0));
//        quizEntry.put("quizQ2TotalCount", spu.getInt(R.string.SP_QUIZ_Q2_TOTAL_COUNT, 0));
//        quizEntry.put("quizQ3CorrectCount", spu.getInt(R.string.SP_QUIZ_Q3_CORRECT_COUNT, 0));
//        quizEntry.put("quizQ3TotalCount", spu.getInt(R.string.SP_QUIZ_Q3_TOTAL_COUNT, 0));
//        quizEntry.put("quizQ4CorrectCount", spu.getInt(R.string.SP_QUIZ_Q4_CORRECT_COUNT, 0));
//        quizEntry.put("quizQ4TotalCount", spu.getInt(R.string.SP_QUIZ_Q4_TOTAL_COUNT, 0));
//        quizEntry.put("quizQ5CorrectCount", spu.getInt(R.string.SP_QUIZ_Q5_CORRECT_COUNT, 0));
//        quizEntry.put("quizQ5TotalCount", spu.getInt(R.string.SP_QUIZ_Q5_TOTAL_COUNT, 0));

        quizEntry.put("ChatterCount", cd.getChatterCount());
        quizEntry.put("ChatLineCount", cd.getChatLineCount());
        quizEntry.put("ChatTitle", cd.getChatFileTitle());

//        db.collection("quiz").document(firebaseToken)
//                .set(quizEntry)
//                .addOnSuccessListener(new OnSuccessListener<Void>() {
//                    @Override
//                    public void onSuccess(Void aVoid) {
//
//                    }
//                })
//                .addOnFailureListener(new OnFailureListener() {
//                    @Override
//                    public void onFailure(@NonNull Exception e) {
//                        //LogUtils.e("Error adding document" + e.getMessage());
//                        e.printStackTrace();
//                    }
//                });


        db.runTransaction(new Transaction.Function<Void>() {
            @Override
            public Void apply(Transaction transaction) throws FirebaseFirestoreException {
                DocumentSnapshot snapshot = transaction.get(scoreDoc);

                if(snapshot.exists()){
                    if(snapshot.contains("highscore")){
                        double serverScore = snapshot.getDouble("highscore");
                        if(score > serverScore){
                            transaction.update(scoreDoc, quizEntry);
                            //transaction.update(scoreDoc, "highscore", score);
                        }
                    } else {
                        transaction.set(scoreDoc, quizEntry, SetOptions.merge());
                    }

                    if(snapshot.contains(yearMonthStr)){
                        double serverScore = snapshot.getDouble(yearMonthStr);
                        if(score > serverScore){
                            transaction.update(scoreDoc, yearMonthStr, score);
                        }
                    } else {
                        Map<String, Object> data = new HashMap<>();
                        data.put(yearMonthStr, score);
                        transaction.set(scoreDoc, data, SetOptions.merge());
                    }

                }

                return null;
            }
        }).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {

            }
        })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {

                    }
                });



    }

    public static String getYearMonthStr(){
        Date date = new Date();
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        String yearMonthStr = String.valueOf(cal.get(Calendar.YEAR));
        String monthStr = String.valueOf(cal.get(Calendar.MONTH) + 1);
        monthStr = (monthStr.length()==2?"":"0") + monthStr;
        yearMonthStr = yearMonthStr + monthStr;
        return yearMonthStr;
    }

    public static void getHighscores(boolean isMonthly, Context context, SharedPrefUtils spu){
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.clearPersistence();
        CollectionReference citiesRef = db.collection("quiz");
        String yearMonthStr = getYearMonthStr();

        Calendar calendar = Calendar.getInstance();
        // Get the current date
        Date today = calendar.getTime();

        // Setting the first day of month
        calendar.set(Calendar.DAY_OF_MONTH, 1);
        Date firstDayOfMonth = calendar.getTime();

        Query query;

        if(isMonthly){
            query = citiesRef.whereNotEqualTo(yearMonthStr, null);
            //query = citiesRef.whereGreaterThan("highscoreUpdateServerDt", firstDayOfMonth);
        } else {
            //전체
            query = citiesRef
                    .whereNotEqualTo("highscore", 0)
                    .orderBy("highscore", Query.Direction.DESCENDING).limit(100);
        }

        query.get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        List<HighscoreData> highscoreDataList = new ArrayList<>();
                        if (task.isSuccessful()) {

                            if(task.getResult().isEmpty()){
                                highscoreCallback.getHighscores(highscoreDataList, 0);
                            }

                            for (QueryDocumentSnapshot document : task.getResult()) {
                                //LogUtils.e(document.getId() + " => " + document.getData());
                                if(isMonthly){
                                    if(document.getData().containsKey("nickname") && document.getData().containsKey(yearMonthStr) && (long) document.getData().get(yearMonthStr) > 0)
                                        highscoreDataList.add(new HighscoreData((int) ((long) document.getData().get(yearMonthStr)), (String) document.getData().get("nickname")));
                                } else {
                                    if(document.getData().containsKey("nickname") && document.getData().containsKey("highscore") && (long) document.getData().get("highscore") > 0)
                                        highscoreDataList.add(new HighscoreData((int) ((long) document.getData().get("highscore")), (String) document.getData().get("nickname")));
                                }

                            }
                            if(isMonthly){
                                Collections.sort(highscoreDataList, new HighscoreComparator());
                                int myScore = 0;
                                for(HighscoreData hd : highscoreDataList){
                                    if(hd.getNickname().equals(spu.getString(R.string.SP_QUIZ_NICKNAME, "-1"))){
                                        myScore = hd.getHighscore();
                                    }
                                }
                                List<HighscoreData> top100 = new ArrayList<>();
                                for(int i=0; i<(highscoreDataList.size()>100?100:highscoreDataList.size()); i++){
                                    top100.add(highscoreDataList.get(i));
                                }
                                highscoreCallback.getHighscores(top100, myScore);
                            } else {
                                highscoreCallback.getHighscores(highscoreDataList, 0);
                            }

                        } else {
                            highscoreCallback.getHighscores(highscoreDataList, 0);
                            FirebaseCrashlytics.getInstance().log("[REXYREX] Highscore list get fail");
                            LogUtils.e("Error getting documents: " + task.getException().toString());
                            Toast.makeText(context, "순위를 불러오는데 문제가 발생했습니다. 잠시 후 다시 시도해주세요.", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    public static class HighscoreComparator implements Comparator<HighscoreData>{

        @Override
        public int compare(HighscoreData d1, HighscoreData d2) {
            return d1.getHighscore()<d2.getHighscore()?1:-1;
        }
    }

    public static void saveShareQuizQuestion(String title, String subTitle, List<String> options, SharedPrefUtils spu, ChatData cd){
        if(!spu.getBool(R.string.SP_FB_BOOL_QUIZ_SHARE, true)){
            return;
        }
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        String firebaseToken = spu.getString(R.string.SP_FB_TOKEN, "null");
        String uuid = spu.getString(R.string.SP_UUID, "none");

        Map<String, Object> quizEntry = new HashMap<>();
        quizEntry.put("nickname", spu.getString(R.string.SP_QUIZ_NICKNAME, "-1"));
        quizEntry.put("firebaseToken", firebaseToken);
        quizEntry.put("uuid", uuid);

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-M-d HH:mm:ss", Locale.KOREAN);
        quizEntry.put("dt", sdf.format(new Date()));
        quizEntry.put("serverDt", FieldValue.serverTimestamp());

        quizEntry.put("ChatterCount", cd.getChatterCount());
        quizEntry.put("ChatLineCount", cd.getChatLineCount());
        quizEntry.put("ChatTitle", cd.getChatFileTitle());

        quizEntry.put("title", title);
        quizEntry.put("subTitle", subTitle);
        quizEntry.put("options", options);

        db.collection("quizShare").document()
                .set(quizEntry)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {

                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        //LogUtils.e("Error adding document" + e.getMessage());
                        e.printStackTrace();
                    }
                });
    }

    public static void sendOpinion(String opinion, SharedPrefUtils spu){
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        String firebaseToken = spu.getString(R.string.SP_FB_TOKEN, "null");
        String uuid = spu.getString(R.string.SP_UUID, "none");

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-M-d HH:mm:ss", Locale.KOREAN);


        Map<String, Object> opinionEntry = new HashMap<>();
        opinionEntry.put("opinion", opinion);
        opinionEntry.put("firebaseToken", firebaseToken);
        opinionEntry.put("uuid", uuid);
        opinionEntry.put("dt", sdf.format(new Date()));
        opinionEntry.put("serverDt", FieldValue.serverTimestamp());

        db.collection("opinions").document()
                .set(opinionEntry)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {

                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        //LogUtils.e("Error adding document" + e.getMessage());
                        e.printStackTrace();
                    }
                });
    }

}
