package com.example.game2048_2;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

public class GameMain extends FrameLayout {
    private final static int GameScale=4;
    private final static float Probability=0.1f;//生成4的概率
    private final static int minOffset=10;
    private final static int TranslateDuration=300;
    private final static int ScaleDuration=100;
    private final static float BeginScaled=0.7f;
    private final static int DeleteView=1;
    private final static int MergeView=2;

    private TextView tvScore;//分数面板
    private int score=0;//分数
    private boolean moveEnable=true;
    private Queue<Card> tempCards=new ConcurrentLinkedQueue<>();//线程安全队列
    private Queue<Pos> tempPosList=new ConcurrentLinkedQueue<>();

    private Handler mHandler=new Handler(){
        @Override
        public void handleMessage(Message message){
            synchronized (this){
                switch (message.what){
                    case DeleteView:
                        if(!tempCards.isEmpty()){
                            //tempCards.poll().setVisibility(View.GONE);
                            removeView(tempCards.poll());
                        }
                        break;
                    case MergeView:
                        if(!tempPosList.isEmpty()){
                            Pos pos=tempPosList.poll();
                            scaleAnimation(mCards[pos.y][pos.x]);
                        }
                        break;
                }
                super.handleMessage(message);
            }
        }
    };

    private List<MainActivity.Position> cardsPosition;
    private Card[][] mCards={
            {null,null,null,null},
            {null,null,null,null},
            {null,null,null,null},
            {null,null,null,null}
    };
    private List<Pos> emptyPoints;
    class Pos{
        int x;
        int y;
        Pos(int x,int y){
            this.x=x;
            this.y=y;
        }
    }

    public GameMain(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public GameMain(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    public GameMain(@NonNull Context context,@Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init();
    }

    public GameMain(@NonNull Context context) {
        super(context);
        init();
    }

    public void setPositions(List<MainActivity.Position> cardsPosition){
        this.cardsPosition=cardsPosition;
        startGame();
        addTouchEvent();
    }
    public void combine(TextView tv){
        tvScore=tv;
    }

    public void combine(Button reStart){
        reStart.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                startGame();
            }
        });
    }

    private void init(){
        emptyPoints=new ArrayList<Pos>();
    }

    private void addTouchEvent(){
        setOnTouchListener(new View.OnTouchListener() {
            private float startX, startY, offsetX, offsetY;

            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                switch (motionEvent.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        //手指按下，记录此时的坐标
                        startX = motionEvent.getX();
                        startY = motionEvent.getY();
                        break;
                    case MotionEvent.ACTION_UP:
                        //手指释放，和按下时的坐标点比较，计算偏移量
                        offsetX = motionEvent.getX() - startX;
                        offsetY = motionEvent.getY() - startY;
                        if(moveEnable){
                            //根据横纵坐标偏移量的大小，确定移动方向
                            if (Math.abs(offsetX) > Math.abs(offsetY)) {//横向移动
                                if (offsetX < -minOffset) {
                                    moveLeft();
                                } else if (offsetX > minOffset) {
                                    moveRight();
                                }
                            } else {//纵向移动
                                if (offsetY < -minOffset) {
                                    moveUp();
                                } else if (offsetY > minOffset) {
                                    moveDown();
                                }
                            }
                        }

                    default:
                        break;
                }

                return true;
            }
        });
    }
    private void startGame(){
        for(int row=0;row<GameScale;row++){
            for(int col=0;col<GameScale;col++){
                if(mCards[row][col]!=null){
                    removeView(mCards[row][col]);
                }
                mCards[row][col]=null;//清空卡片矩阵
            }
        }//初始化卡片空间的矩阵
        score=0;
        showScore();
        addRandomCard();
        addRandomCard();
    }

    private void addRandomCard(){
        emptyPoints.clear();
        for(int row=0;row<GameScale;row++){
            for(int col=0;col<GameScale;col++){
                if(null==mCards[row][col]){
                    emptyPoints.add(new Pos(row,col));//所有数字为空的点坐标
                }
            }
        }
        Pos pos=emptyPoints.remove((int)(Math.random()*emptyPoints.size()));
        Card card=addCard(pos.x,pos.y,Math.random()>Probability?2:4);

        if(emptyPoints.isEmpty()) {//如果所有坐标都有数字了，判断游戏是否结束了
            boolean isOver = true;
            TAG:
            {
                for (int row = 0; row < GameScale; row++) {
                    for (int col = 0; col < GameScale; col++) {
                        if (col < GameScale - 1 && mCards[row][col].isEquals(mCards[row][col+1])) {//横坐标正方向是否有相邻且相同的数字
                            isOver = false;
                            break TAG;
                        }
                        if (row < GameScale - 1 && mCards[row][col].isEquals(mCards[row+1][col])) {//纵坐标负方向是否有相邻且相同的数字
                            isOver = false;
                            break TAG;
                        }
                    }
                }
            }
            if (isOver) {
                alertDialog("挑战失败", "游戏结束，您未能完成挑战");
            }
        }
    }
    public void moveUp(){
        boolean isMove=false;
        boolean isMerge;
        for(int col=0;col<GameScale;col++){
            for(int row=0;row<GameScale;row++){
                isMerge=true;
                for(int row1=row+1;row1<GameScale;row1++) {
                    if(mCards[row1][col]!=null){

                        if(mCards[row][col]==null){
                            //如果是空的
                            translateByPos(col,col,row1,row);
                            isMove=true;
                        }
                        else if(!mCards[row1][col].isEquals(mCards[row][col])){
                            isMerge=false;
                        }
                        else if(isMerge && mCards[row][col].isEquals(mCards[row1][col])){
                            //如果又相同则合并
                            translateByPos(col,col,row1,row);
                            isMove=true;
                            break;
                        }
                    }
                }
            }
        }
        if(isMove){
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    synchronized (this){
                        addRandomCard();
                    }

                }
            },TranslateDuration);
        }
    }

    public void moveDown(){
        boolean isMove=false;
        boolean isMerge;
        for(int col=0;col<GameScale;col++){
            for(int row=GameScale-1;row>0;row--){
                isMerge=true;
                for(int row1=row-1;row1>=0;row1--) {
                    if(mCards[row1][col]!=null){

                        if(mCards[row][col]==null){
                            //如果是空的
                            translateByPos(col,col,row1,row);
                            isMove=true;
                        }
                        else if(!mCards[row1][col].isEquals(mCards[row][col])){
                            isMerge=false;
                        }
                        else if(isMerge && mCards[row1][col].isEquals(mCards[row][col])){
                            //如果又相同则合并
                            translateByPos(col,col,row1,row);
                            isMove=true;
                            break;
                        }
                    }
                }
            }
        }
        if(isMove){
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    synchronized (this){
                        addRandomCard();
                    }

                }
            },TranslateDuration);
        }
    }

    public void moveLeft(){
        boolean isMove=false;
        boolean isMerge;
        for(int row=0;row<GameScale;row++){
            for(int col=0;col<GameScale;col++){
                isMerge=true;
                for(int col1=col+1;col1<GameScale;col1++) {
                    if(mCards[row][col1]!=null){

                        if(mCards[row][col]==null){
                            //如果是空的
                            translateByPos(col1,col,row,row);
                            isMove=true;
                        }
                        else if(!mCards[row][col1].isEquals(mCards[row][col])){
                            isMerge=false;
                        }
                        else if(isMerge && mCards[row][col1].isEquals(mCards[row][col])){
                            //如果又相同则合并
                            translateByPos(col1,col,row,row);
                            isMove=true;
                            break;
                        }

                    }
                }
            }
        }
        if(isMove){
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    synchronized (this){
                        addRandomCard();
                    }

                }
            },TranslateDuration);
        }
    }

    public void moveRight(){
        boolean isMove=false;
        boolean isMerge;
        for(int row=0;row<GameScale;row++){
            for(int col=GameScale-1;col>0;col--){
                isMerge=true;
                for(int col1=col-1;col1>=0;col1--) {
                    if(mCards[row][col1]!=null){

                        if(mCards[row][col]==null){
                            //如果是空的
                            translateByPos(col1,col,row,row);
                            isMove=true;
                        }
                        else if(!mCards[row][col1].isEquals(mCards[row][col])){
                            isMerge=false;
                        }
                        else if(isMerge && mCards[row][col1].isEquals(mCards[row][col1])){
                            //如果又相同则合并
                            translateByPos(col1,col,row,row);
                            isMove=true;
                            break;
                        }

                    }
                }
            }
        }
        if(isMove){
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    synchronized (this){
                        addRandomCard();
                    }

                }
            },TranslateDuration);
        }
    }

    private void alertDialog(String title,String message){
        new AlertDialog.Builder(getContext()).setTitle(title).setMessage(message).setPositiveButton("重来",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        startGame();
                    }
                }).setNegativeButton("取消",null).show();
    }
    private void addScore(int s){
        score += s;
        showScore();
        //分数达到2048，获胜
        if(s==2048)
            alertDialog("恭喜！","恭喜您完成2048的目标！");

    }
    private Card addCard(int row,int col,int num){
        Card card=new Card(getContext());
        MainActivity.Position pos=cardsPosition.get(row*4+col);
        card.setNum(num);
        card.setZ(2);
        addView(card,MainActivity.cardWidth,MainActivity.cardWidth);
        LayoutParams layoutParams= (LayoutParams) card.getLayoutParams();
        layoutParams.leftMargin=pos.getX();
        layoutParams.topMargin=pos.getY();
        mCards[row][col]=card;
        return card;
    }

    //按卡片在矩阵序列中的位置为基准进行位移
    private void translateByPos(final int fromX,final int toX,final int fromY,final int toY){
        final int cardWidth=MainActivity.cardWidth;
        final Card card=mCards[fromY][fromX];
//        TranslateAnimation translateAnimation=new TranslateAnimation(0,(toX-fromX)*cardWidth,0,(toY-fromY)*cardWidth);
//        TranslateAnimation translateAnimation=new TranslateAnimation(Animation.RELATIVE_TO_SELF,0,Animation.RELATIVE_TO_SELF,
//                                                          (toX-fromX),Animation.RELATIVE_TO_SELF,0,Animation.RELATIVE_TO_SELF,(toY-fromY));
//        translateAnimation.setDuration(TranslateDuration);
//        translateAnimation.setRepeatCount(0);
//        translateAnimation.setFillAfter(true);

        ValueAnimator translateAnimation;
        if(toY-fromY==0){//如果是水平方向的移动
            translateAnimation=ValueAnimator.ofInt(fromX*cardWidth,toX*cardWidth);
            translateAnimation.setTarget(card);
            translateAnimation.setDuration(TranslateDuration);
            translateAnimation.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator animation) {
                    card.setLeftMargin((int)animation.getAnimatedValue());
                    Log.d("Animation",(int)animation.getAnimatedValue()+"");
                }
            });
        }
        else{
            translateAnimation=ValueAnimator.ofInt(fromY*cardWidth,toY*cardWidth);
            translateAnimation.setTarget(card);
            translateAnimation.setDuration(TranslateDuration);
            translateAnimation.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator animation) {
                    card.setTopMargin((int)animation.getAnimatedValue());
                    Log.d("Animation",(int)animation.getAnimatedValue()+"");
                }
            });
        }
        mCards[fromY][fromX]=null;
        if( mCards[toY][toX]==null){
            mCards[toY][toX]=card;
        }
        else {//合并
            addScore(mCards[toY][toX].getNum());
            tempCards.add(card);
            tempPosList.add(new Pos(toX, toY));
            mCards[toY][toX].setZ(10);
            translateAnimation.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    Message message=new Message();
                    message.what=DeleteView;//删除视图信号
                    mHandler.sendMessage(message);

                    mCards[toY][toX].setNum(card.getNum()*2);
                }
            });
            if(toY-fromY==0){
                translateAnimation.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                    boolean isScaled=false;
                    @Override
                    public void onAnimationUpdate(ValueAnimator animation) {
                        if((int)animation.getAnimatedValue()-toX*cardWidth<cardWidth*BeginScaled&&!isScaled){//isScaled用于控制消息值发送一次
                            Message message=new Message();
                            message.what=MergeView;//合并信号
                            mHandler.sendMessage(message);
                            isScaled=true;
                        }
                        Log.d("Animation",(int)animation.getAnimatedValue()+"");
                    }
                });
            }
            else{
                translateAnimation.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                    boolean isScaled=false;
                    @Override
                    public void onAnimationUpdate(ValueAnimator animation) {
                        if((int)animation.getAnimatedValue()-toY*cardWidth<cardWidth*BeginScaled&&!isScaled){
                            Message message=new Message();
                            message.what=MergeView;
                            mHandler.sendMessage(message);
                            isScaled=true;
                        }
                        Log.d("Animation",(int)animation.getAnimatedValue()+"");
                    }
                });
            }
        }
        translateAnimation.start();
    }

    private void scaleAnimation(final Card card){
        ValueAnimator scaleAnimator=ValueAnimator.ofFloat(1.0f,1.1f,1.0f);
        scaleAnimator.setTarget(card);
        scaleAnimator.setDuration(ScaleDuration);
        scaleAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                card.setScaleX((float)animation.getAnimatedValue());
                card.setScaleY((float)animation.getAnimatedValue());
            }
        });
        scaleAnimator.start();
        scaleAnimator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
            }
        });
    }

    private void showScore(){
        if(tvScore!=null) {
            tvScore.setText(String.valueOf(score));
        }
    }
}
