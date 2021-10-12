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
import com.rexyrex.kakaoparser.R;
import com.rexyrex.kakaoparser.Utils.LogUtils;
import com.rexyrex.kakaoparser.ml.MobileBert20;
import com.rexyrex.kakaoparser.ml.ModelWordVec100;

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
import java.io.OutputStreamWriter;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.CharBuffer;
import java.nio.FloatBuffer;
import java.nio.charset.Charset;
import java.nio.charset.CharsetEncoder;
import java.util.List;

public class EmotionsActivity extends AppCompatActivity {
    Interpreter interpreter;

    TextView tv;
    EditText et;
    Button btn;
    File modelFile;
    NLClassifier textClassifier;
    BertNLClassifier bertClassifier;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_emotions);

        tv = findViewById(R.id.emotionsTV);
        btn = findViewById(R.id.emotionsBtn);
        et = findViewById(R.id.emotionsET);



        CustomModelDownloadConditions conditions = new CustomModelDownloadConditions.Builder()
                //.requireWifi()  // Also possible: .requireCharging() and .requireDeviceIdle()
                .build();
        FirebaseModelDownloader.getInstance()
                .getModel("modelWordVec100", DownloadType.LATEST_MODEL, conditions)
                .addOnSuccessListener(new OnSuccessListener<CustomModel>() {
                    @Override
                    public void onSuccess(CustomModel model) {
                        // Download complete. Depending on your app, you could enable the ML
                        // feature, or switch from the local model to the remote model, etc.

                        // The CustomModel object contains the local path of the model file,
                        // which you can use to instantiate a TensorFlow Lite interpreter.
                        modelFile = model.getFile();
                        LogUtils.e(model.getLocalFilePath());
//                        try {
//                            FileOutputStream fos = new FileOutputStream(modelFile);
//                            OutputStreamWriter outputWriter=new OutputStreamWriter(fos);
//                            outputWriter.write("emotions_model");
//                            outputWriter.close();
//                            fos.close();
//                        } catch (FileNotFoundException e) {
//                            e.printStackTrace();
//                        } catch (IOException e) {
//                            e.printStackTrace();
//                        }

//                        try {
//                            textClassifier = NLClassifier.createFromFile(EmotionsActivity.this, "model.tflite");
//                            Toast.makeText(EmotionsActivity.this, "load success", Toast.LENGTH_LONG).show();
//                        } catch (IOException e) {
//                            Toast.makeText(EmotionsActivity.this, "load fail :(", Toast.LENGTH_LONG).show();
//                            e.printStackTrace();
//                        }
                        if (modelFile != null) {
                            interpreter = new Interpreter(modelFile);
                        }
                    }
                });

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

        //Tensor

        ByteBuffer input = stringToByteBuffer(inputStr);
        int bufferSize = 6 * java.lang.Float.SIZE / java.lang.Byte.SIZE;
        ByteBuffer modelOutput = ByteBuffer.allocateDirect(bufferSize).order(ByteOrder.nativeOrder());
        interpreter.run(input, modelOutput);

        modelOutput.rewind();
        FloatBuffer probabilities = modelOutput.asFloatBuffer();

        for (int i = 0; i < probabilities.capacity(); i++) {
            float probability = probabilities.get(i);
            LogUtils.e(String.format("%s: %1.4f", i, probability));
        }

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