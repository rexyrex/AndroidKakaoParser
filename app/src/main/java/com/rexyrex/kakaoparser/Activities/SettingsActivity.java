package com.rexyrex.kakaoparser.Activities;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Switch;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.SwitchPreference;

import com.google.android.gms.common.util.SharedPreferencesUtils;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.messaging.FirebaseMessaging;
import com.rexyrex.kakaoparser.R;
import com.rexyrex.kakaoparser.Utils.SharedPrefUtils;

public class SettingsActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTheme(R.style.SettingsTheme);
        setContentView(R.layout.settings_activity);
        if (savedInstanceState == null) {
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.settings, new SettingsFragment())
                    .commit();
        }
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
    }

    public static class SettingsFragment extends PreferenceFragmentCompat {
        String appPackageName = "com.rexyrex.kakaoparser";
        private SharedPrefUtils spu;

        @Override
        public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
            setPreferencesFromResource(R.xml.root_preferences, rootKey);
            spu = new SharedPrefUtils(getContext());
        }

        @Override
        public boolean onPreferenceTreeClick(Preference preference) {
            String key = preference.getKey();
            if(key.equals("appStore")){

                try {
                    getActivity().startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + appPackageName)));
                } catch (android.content.ActivityNotFoundException anfe) {
                    getActivity().startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=" + appPackageName)));
                }
                return true;
            }

            if(key.equals("appShare")){
                try {
                    Intent i = new Intent(Intent.ACTION_SEND);
                    i.setType("text/plain");
                    //i.putExtra(Intent.EXTRA_SUBJECT, "CherryAndroid");
                    i.putExtra(Intent.EXTRA_TEXT, "https://play.google.com/store/apps/details?id=" + appPackageName);
                    getActivity().startActivity(Intent.createChooser(i, "선택하세요"));
                } catch(Exception e) {
                    //e.printStackTrace();
                }
                return true;
            }

            if(key.equals("appEmail")){
                Intent email = new Intent(Intent.ACTION_SEND);
                email.putExtra(Intent.EXTRA_EMAIL, new String[]{ "rexyrex.dev@gmail.com"});
                email.putExtra(Intent.EXTRA_SUBJECT, "[KakaoParser] 문의합니다");

                email.setType("message/rfc822");

                startActivity(Intent.createChooser(email, "이메일 클라이언트 선택 :"));
                return true;
            }

            if(key.equals("appNoti")){
                boolean val = preference.getSharedPreferences().getBoolean("appNoti", false);
                //subscribe to topic
                if(val){
                    FirebaseMessaging.getInstance().subscribeToTopic(getString(R.string.FirebaseTopicName))
                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if (!task.isSuccessful()) {
                                        //Toast.makeText(getActivity(), "구글 서비스 문제가 발생했습니다. 잠시후 다시 시도해주세요.", Toast.LENGTH_LONG).show();
                                    } else {
                                        //Toast.makeText(getActivity(), "알림 구독 완료", Toast.LENGTH_SHORT).show();
                                    }
                                }
                            });
                } else {
                    FirebaseMessaging.getInstance().unsubscribeFromTopic(getString(R.string.FirebaseTopicName))
                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if (!task.isSuccessful()) {
                                        //Toast.makeText(getActivity(), "구글 서비스 문제가 발생했습니다. 잠시후 다시 시도해주세요.", Toast.LENGTH_LONG).show();
                                    } else {
                                        //Toast.makeText(getActivity(), "알림 구독 해제", Toast.LENGTH_SHORT).show();
                                    }
                                }
                            });
                }
                return true;
            }

            if(key.equals("appPrivacy")){
                spu.saveInt(R.string.SP_OPEN_PRIV_POLICY_COUNT, spu.getInt(R.string.SP_OPEN_PRIV_POLICY_COUNT, 0) + 1);
                Intent browserIntent = new Intent(Intent.ACTION_VIEW,
                        Uri.parse("https://rexyrex.com/kakaoParserAgree"));
                startActivity(browserIntent);
                return true;
            }

            if(key.equals("appOpinion")){
                Intent intent = new Intent(getContext(), SendOpinionActivity.class);
                startActivity(intent);
                return true;
            }

            if(key.equals("saveChatError")){
                Intent browserIntent = new Intent(Intent.ACTION_VIEW,
                        Uri.parse("https://rexyrex.com/kakaoParserErrorHelp1"));
                startActivity(browserIntent);
                return true;
            }

            return false;
        }
    }
}