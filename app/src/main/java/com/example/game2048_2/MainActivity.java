package com.example.game2048_2;

import android.content.res.Resources;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.GridLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    private GridLayout mGridLayout;
    private GameMain mGameMain;
    private TextView scoreText;
    private Button reStart;
    public static int cardWidth;
    public final static int margin=10;//px
    class Position{
        private int x;
        private int y;
        Position(int x,int y){
            this.x=x;
            this.y=y;
        }
        final int getX(){
            return x;
        }
        final int getY(){
            return y;
        }
    }
    private List<Position> cardsPosition;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mGridLayout=(GridLayout)findViewById(R.id.gridlayout);
        mGameMain=(GameMain) findViewById(R.id.movable_card_layer);
        scoreText=(TextView)findViewById(R.id.score);
        reStart=(Button)findViewById(R.id.restart);

        cardsPosition=new ArrayList<Position>();

        mGameMain.combine(scoreText);
        mGameMain.combine(reStart);
        mGridLayout.setZ(1);
        mGameMain.setZ(2);
        setGridLayoutAndFrameLayoutSize();
        getAllGridsPos();
        mGameMain.setPositions(cardsPosition);
        Log.d("mGridLayout.getChildCount:", String.valueOf(mGridLayout.getChildCount()));
    }
    private void setGridLayoutAndFrameLayoutSize(){
        Resources resources=this.getResources();
        DisplayMetrics displayMetrics=resources.getDisplayMetrics();
        int screenWidth=displayMetrics.widthPixels;
        int screenHeight=displayMetrics.heightPixels;
        int layoutWidth=Math.min(screenHeight,screenWidth);
        ViewGroup.LayoutParams params1= mGridLayout.getLayoutParams();
        params1.width=params1.height=layoutWidth;
        mGridLayout.setLayoutParams(params1);
        ViewGroup.LayoutParams params2=mGameMain.getLayoutParams();
        params2.width=params2.height=layoutWidth;
        cardWidth=layoutWidth/4;
    }
    private void getAllGridsPos(){
        FrameLayout grid;
        for(int i=0;i<mGridLayout.getChildCount();i++){
            grid=(FrameLayout) mGridLayout.getChildAt(i);
            Log.d("getAllGridsPos:",grid.getId()+"");
            Log.d("getAllGridsPos:", (i%4*cardWidth)+" "+(i/4*cardWidth)+" "+cardWidth+" "+cardWidth);
            cardsPosition.add(new Position(i%4*cardWidth,i/4*cardWidth));
        }
    }
}
