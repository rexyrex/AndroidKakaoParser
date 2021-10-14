package com.rexyrex.kakaoparser.Activities;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.ml.modeldownloader.CustomModel;
import com.google.firebase.ml.modeldownloader.CustomModelDownloadConditions;
import com.google.firebase.ml.modeldownloader.DownloadType;
import com.google.firebase.ml.modeldownloader.FirebaseModelDownloader;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.rexyrex.kakaoparser.Entities.DateIntPair;
import com.rexyrex.kakaoparser.R;
import com.rexyrex.kakaoparser.Utils.FileParseUtils;
import com.rexyrex.kakaoparser.Utils.LogUtils;

import org.snu.ids.kkma.index.KeywordExtractor;
import org.snu.ids.kkma.index.KeywordList;
import org.snu.ids.kkma.ma.MExpression;
import org.snu.ids.kkma.ma.MorphemeAnalyzer;
import org.snu.ids.kkma.ma.Sentence;
import org.tensorflow.lite.DataType;
import org.tensorflow.lite.Interpreter;
import org.tensorflow.lite.support.label.Category;
import org.tensorflow.lite.support.tensorbuffer.TensorBuffer;
import org.tensorflow.lite.task.text.nlclassifier.BertNLClassifier;
import org.tensorflow.lite.task.text.nlclassifier.NLClassifier;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.lang.reflect.Type;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.CharBuffer;
import java.nio.FloatBuffer;
import java.nio.charset.Charset;
import java.nio.charset.CharsetEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class EmotionsActivity extends AppCompatActivity {
    Interpreter interpreter;

    TextView tv;
    EditText et;
    Button btn;
    File modelFile;
    NLClassifier textClassifier;
    BertNLClassifier bertClassifier;

    MorphemeAnalyzer ma;

    HashMap<String,Double> labelsDict;
    HashMap<String,Double> classProbDict;
    HashMap<String,Double> uniqueWordCountDict;
    HashMap<String,HashMap<String, Double>> wordProbDict;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_emotions);

        tv = findViewById(R.id.emotionsTV);
        btn = findViewById(R.id.emotionsBtn);
        et = findViewById(R.id.emotionsET);

        //ma = new MorphemeAnalyzer();

        String labelsDictStr = FileParseUtils.getJsonFromAssets(this, "labels.json");
        Type StringStringMapType = new TypeToken<HashMap<String, String>>() {}.getType();
        labelsDict = new Gson().fromJson(labelsDictStr, StringStringMapType);

        String classProbDictStr = FileParseUtils.getJsonFromAssets(this, "class_prob_dict.json");
        Type stringFloatMap = new TypeToken<HashMap<String, Double>>() {}.getType();
        classProbDict = new Gson().fromJson(classProbDictStr, stringFloatMap);
        LogUtils.e("E50: " + classProbDict.get("E50"));

        String uniqueWordCountDictStr = FileParseUtils.getJsonFromAssets(this, "unique_word_count_dict.json");
        uniqueWordCountDict = new Gson().fromJson(uniqueWordCountDictStr, stringFloatMap);
        LogUtils.e("E50 unique count: " + uniqueWordCountDict.get("E50"));

        String wordProbDictStr = FileParseUtils.getJsonFromAssets(this, "word_prob_dict.json");
        Type mapInMapType = new TypeToken<HashMap<String, HashMap<String, Double>>>() {}.getType();
        wordProbDict = new Gson().fromJson(wordProbDictStr, mapInMapType);
        LogUtils.e("E50: " + wordProbDict.get("E50").size());

//        for(Map.Entry<String, Double> entry : wordProbDict.get("E50").entrySet()){
//            LogUtils.e("Key : " + entry.getKey());
//            LogUtils.e("Value : " + entry.getValue());
//        }

        LogUtils.e("먹던, E50 : " + wordProbDict.get("E50").get("먹던"));



//        CustomModelDownloadConditions conditions = new CustomModelDownloadConditions.Builder()
//                //.requireWifi()  // Also possible: .requireCharging() and .requireDeviceIdle()
//                .build();
//        FirebaseModelDownloader.getInstance()
//                .getModel("modelWordVec100", DownloadType.LATEST_MODEL, conditions)
//                .addOnSuccessListener(new OnSuccessListener<CustomModel>() {
//                    @Override
//                    public void onSuccess(CustomModel model) {
//                        // Download complete. Depending on your app, you could enable the ML
//                        // feature, or switch from the local model to the remote model, etc.
//
//                        // The CustomModel object contains the local path of the model file,
//                        // which you can use to instantiate a TensorFlow Lite interpreter.
//                        modelFile = model.getFile();
//                        LogUtils.e(model.getLocalFilePath());
////                        try {
////                            FileOutputStream fos = new FileOutputStream(modelFile);
////                            OutputStreamWriter outputWriter=new OutputStreamWriter(fos);
////                            outputWriter.write("emotions_model");
////                            outputWriter.close();
////                            fos.close();
////                        } catch (FileNotFoundException e) {
////                            e.printStackTrace();
////                        } catch (IOException e) {
////                            e.printStackTrace();
////                        }
//
////                        try {
////                            textClassifier = NLClassifier.createFromFile(EmotionsActivity.this, "model.tflite");
////                            Toast.makeText(EmotionsActivity.this, "load success", Toast.LENGTH_LONG).show();
////                        } catch (IOException e) {
////                            Toast.makeText(EmotionsActivity.this, "load fail :(", Toast.LENGTH_LONG).show();
////                            e.printStackTrace();
////                        }
//                        if (modelFile != null) {
//                            interpreter = new Interpreter(modelFile);
//                        }
//                    }
//                });

//        try {
//            //ModelWordVec100 model = ModelWordVec100.newInstance(this);
//            MobileBert20 model = MobileBert20.newInstance(this);
////            // Creates inputs for reference.
////            TensorBuffer inputText = TensorBuffer.createFixedSize(new int[]{1, 256}, DataType.INT32);
////            inputText.loadBuffer(stringToByteBuffer("안녕"));
////
////            // Runs model inference and gets result.
////            ModelWordVec100.Outputs outputs = model.process(inputText);
////            TensorBuffer probability = outputs.getProbabilityAsTensorBuffer();
////
////            LogUtils.e(probability.toString());
//
//            // Releases model resources if no longer used.
//            model.close();
//        } catch (IOException e) {
//            // TODO Handle the exception
//        }



//        try {
//            textClassifier = NLClassifier.createFromFile(EmotionsActivity.this, "model_word_vec_100.tflite");
//            bertClassifier = BertNLClassifier.createFromFile(EmotionsActivity.this, "mobile_bert_20.tflite");
//
//            Toast.makeText(EmotionsActivity.this, "load success", Toast.LENGTH_LONG).show();
//        } catch (IOException e) {
//            Toast.makeText(EmotionsActivity.this, "load fail :(", Toast.LENGTH_LONG).show();
//            e.printStackTrace();
//        }

        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                //File mFile = get
                interpret();

            }
        });
    }

    public void interpret(){
        String inputStr = et.getText().toString();

//        //Tensor
//
//        ByteBuffer input = stringToByteBuffer(inputStr);
//        int bufferSize = 6 * java.lang.Float.SIZE / java.lang.Byte.SIZE;
//        ByteBuffer modelOutput = ByteBuffer.allocateDirect(bufferSize).order(ByteOrder.nativeOrder());
//        interpreter.run(input, modelOutput);
//
//        modelOutput.rewind();
//        FloatBuffer probabilities = modelOutput.asFloatBuffer();
//
//        for (int i = 0; i < probabilities.capacity(); i++) {
//            float probability = probabilities.get(i);
//            LogUtils.e(String.format("%s: %1.4f", i, probability));
//        }

//        KeywordExtractor ke = new KeywordExtractor();
//        KeywordList kl = ke.extractKeyword(inputStr, false);

//        List<MExpression> ret = null;
//        try {
//            ret = ma.analyze(inputStr);
//            List<Sentence> stl = ma.divideToSentences(ret);
//
//            String res = "";
//            for(int i=0; i<stl.size(); i++){
//                res += stl.get(i).getSentence() + "\n";
//            }
//
//            tv.setText(res);
//
//        } catch (Exception e) {
//            e.printStackTrace();
//        }

        String[] words = inputStr.split("\\s");

        HashMap<String, Double> res = new HashMap<>();

        double maxProb=0, secondProb=0, thirdProb=0, sumProb=0;
        String maxLabel="", secondLabel="", thirdLabel="";

        for(Map.Entry<String, Double> classProbEntry : classProbDict.entrySet()){
            String label = classProbEntry.getKey();
            double prob = classProbEntry.getValue();
            double defaultProb = 1 / uniqueWordCountDict.get(label);
            for(int i=0; i<words.length; i++){
                String word = words[i];
                prob *= wordProbDict.get(label).getOrDefault(word, defaultProb);
            }
            res.put(label, prob);
            sumProb += prob;
            if(prob > maxProb){
                thirdLabel = secondLabel;
                thirdProb = secondProb;

                secondLabel = maxLabel;
                secondProb = maxProb;

                maxLabel = label;
                maxProb = prob;
            } else if(prob > secondProb){
                thirdLabel = secondLabel;
                thirdProb = secondProb;

                secondLabel = label;
                secondProb = prob;
            } else if(prob > thirdProb){
                thirdLabel = label;
                thirdProb = prob;
            }
        }

        tv.setText(
                ""+labelsDict.get(maxLabel) + " : " + (maxProb / sumProb)*100 + "\n"
                + labelsDict.get(secondLabel) + " : " + (secondProb / sumProb)*100 + "\n"
                + labelsDict.get(thirdLabel) + " : " + (thirdProb / sumProb)*100 + "\n"
        );


    }

    public void zam(){
        String input = et.getText().toString();
        List<Category> results = bertClassifier.classify(input);
        String resStr = "Input : " + input + "\n\nOutput : \n\n";
        for(int i=0; i<results.size(); i++){
            Category res = results.get(i);
            resStr += getActualLabel(res.getLabel()) + " : " + res.getScore() + "\n";
        }
        tv.setText(resStr);
    }
    
    public String getActualLabel(String label){
        switch(label){
            case "E1" : return "분노";
            case "E2" : return "슬픔";
            case "E3" : return "불안";
            case "E4" : return "상처";
            case "E5" : return "당황";
            case "E6" : return "기쁨";
            default: return "";
        }
    }

    public ByteBuffer stringToByteBuffer(String str){
        Charset charset = Charset.forName("UTF-8");
        CharsetEncoder encoder = charset.newEncoder();
        try{
            return encoder.encode(CharBuffer.wrap(str));
        }catch(Exception e){e.printStackTrace();}
        return null;
    }
}