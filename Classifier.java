package com.example.imageclassification;

import android.annotation.SuppressLint;
import android.app.assist.AssistContent;
import android.content.res.AssetFileDescriptor;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;

import androidx.annotation.NonNull;

import org.tensorflow.lite.Interpreter;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.PriorityQueue;

public class Classifier {

    Interpreter interpreter;
    List<String> labellist;
    int INPUT_SIZE;
    int PIXEL_SIZE=3;
    int IMAGE_MEAN=0;
    float IMAGE_STD=255.0f;
    float MAX_RESULTS=3;
    float THRESHOLD=0.5f;

    public Classifier(AssetManager assetManager,String modelPath,String labelPath,int inputSize) throws IOException{
        INPUT_SIZE=inputSize;
        Interpreter.Options options=new Interpreter.Options();
        options.setNumThreads(5);
        options.setUseNNAPI(true);
        interpreter =new Interpreter(loadModelFile(assetManager,modelPath),options);
        labellist=loadLabellist(assetManager,labelPath);
    }

    private MappedByteBuffer loadModelFile(AssetManager assetManager, String MODEL_FILE) throws IOException{
        AssetFileDescriptor fileDescriptor=assetManager.openFd(MODEL_FILE);
        FileInputStream inputStream = new FileInputStream(fileDescriptor.getFileDescriptor());
        FileChannel fileChannel = inputStream.getChannel();
        long startOffset = fileDescriptor.getStartOffset();
        long declaredLength = fileDescriptor.getDeclaredLength();
        return fileChannel.map(FileChannel.MapMode.READ_ONLY,startOffset,declaredLength);
    }

    private List<String> loadLabellist(AssetManager assetManager, String labelPath)throws IOException {
        List<String> labelList=new ArrayList<>();
        BufferedReader reader=new BufferedReader(new InputStreamReader(assetManager.open(labelPath)));
        String line;

        while((line=reader.readLine())!=null){
            labelList.add(line);
        }
        reader.close();
        return labelList;
    }

    class Recognition{
        String id="";
        String title="";
        float confidence=0F;

        public Recognition(String i,String s,float confidence){
            id=i;
            title=s;
            this.confidence=confidence;
        }
        @NonNull
        @Override

        public String toString(){
            return "Title = "+title+", Confidence = "+confidence;
        }
    }




      public String recognizeImage(Bitmap bitmap){

        Bitmap scaledBitmap=Bitmap.createScaledBitmap(bitmap,INPUT_SIZE,INPUT_SIZE,false);
        ByteBuffer byteBuffer=this.convertBitmapToByteBuffer(scaledBitmap);
        float[][]result=new float[1][labellist.size()];
        this.interpreter.run(byteBuffer,result);
        return getlabel(result);
    }

    private String getlabel(float[][] result) {

        int index=0;
        String labelc="";
        float maxval=0;
        for(int i=0;i<result[0].length;i++)
        {
            if(result[0][i]>maxval)
            {
                System.out.println("   RESULT   "+result[0][i]+"  INDEX  "+i);
                maxval=result[0][i];
                index=i;
            }
        }

        labelc=(String)labellist.get(index);
        return labelc;
    }

    private ByteBuffer convertBitmapToByteBuffer(Bitmap bitmap) {
        ByteBuffer byteBuffer;
        byteBuffer=ByteBuffer.allocateDirect(4*INPUT_SIZE*INPUT_SIZE*PIXEL_SIZE);

        byteBuffer.order(ByteOrder.nativeOrder());
        int[] intValues=new int[INPUT_SIZE*INPUT_SIZE];
        bitmap.getPixels(intValues,0,bitmap.getWidth(),0,0,bitmap.getWidth(),bitmap.getHeight());
        int pixel=0;
        for (int i=0; i<INPUT_SIZE;++i){
            for(int j=0;j<INPUT_SIZE;++j){
                final int val=intValues[pixel++];
                byteBuffer.putFloat((((val>>16) & 0xFF)-IMAGE_MEAN)/IMAGE_STD);
                byteBuffer.putFloat((((val>>8) & 0xFF)-IMAGE_MEAN)/IMAGE_STD);
                byteBuffer.putFloat((((val) & 0xFF)-IMAGE_MEAN)/IMAGE_STD);
            }
        }

         return byteBuffer;
        //System.out.println("output");
    }

  /*  private List<Recognition> getSortedResultFloat(float[][] labelProbArray){

        PriorityQueue<Recognition>pq= new PriorityQueue<>((int) MAX_RESULTS, new Comparator<Recognition>() {
            @Override
            public int compare(Recognition o1, Recognition o2) {
                return Float.compare(o1.confidence,o2.confidence);
            }
        });

        for (int i=0;i<labellist.size();++i){
            float confidence=labelProbArray[0][1];
            if(confidence>THRESHOLD){
                pq.add(new Recognition(""+i,labellist.size()>i?labellist.get(i):"unknown",confidence));
            }
        }

        final ArrayList<Recognition>recognitions = new ArrayList<>();
        int recognitionsSize=(int) Math.min(pq.size(),MAX_RESULTS);
        for(int i=0;i<recognitionsSize;++i)
        {
            recognitions.add(pq.poll());
        }
              return recognitions;
    }*/


}
