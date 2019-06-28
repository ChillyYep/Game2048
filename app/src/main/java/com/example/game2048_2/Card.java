package com.example.game2048_2;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.Gravity;
import android.widget.FrameLayout;
import android.widget.TextView;

public class Card extends FrameLayout {
    private int num;
    private TextView label;
//    final static int FontSize=128;

    public Card(@NonNull Context context,@Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public Card(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    public Card(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init();
    }
    public Card(@NonNull Context context) {
        super(context);
        //this.arrayX=arrayX;
        //this.arrayY=arrayY;
        init();
    }
    public void setLeftMargin(int leftMargin){
        LayoutParams layoutParams=(LayoutParams) getLayoutParams();
        layoutParams.leftMargin=leftMargin;
        setLayoutParams(layoutParams);
    }

    public void setTopMargin(int topMargin){
        LayoutParams layoutParams=(LayoutParams)getLayoutParams();
        layoutParams.topMargin=topMargin;
        setLayoutParams(layoutParams);
    }

    private void init(){
        label=new TextView(getContext());
        label.setTextSize(32);
        label.setBackgroundColor(0x33ffffff);
        label.setGravity(Gravity.CENTER);

        LayoutParams lp=new LayoutParams(LayoutParams.MATCH_PARENT,LayoutParams.MATCH_PARENT);

        lp.setMargins(MainActivity.margin,MainActivity.margin,MainActivity.margin,MainActivity.margin);
        addView(label,lp);
    }
//    public void setFontSizeByGameScale(int GameScale){
//        label.setTextSize((int)(FontSize/GameScale));
//    }

    private void setTextAndBackgroundColor(int TextColor,int BackgroundColor){
        label.setTextColor(TextColor);
        label.setBackgroundColor(BackgroundColor);
    }
    public void setNum(int num) {
        this.num = num;
        if(num<=0)
        {
            label.setBackgroundColor(0x33ffffff);
            label.setText("");
        }
        else{
            switch (num){
                case 2:
                    setTextAndBackgroundColor(0xff050505,0xfffffff0);
                    break;
                case 4:
                    setTextAndBackgroundColor(0xff050505,0xfffffacd);
                    break;
                case 8:
                    setTextAndBackgroundColor(0xffffffff,0xffffa54f);
                    break;
                case 16:
                    setTextAndBackgroundColor(0xffffffff,0xffff6347);
                    break;
                case 32:
                    setTextAndBackgroundColor(0xffffffff,0xffff4500);
                    break;
                case 64:
                    setTextAndBackgroundColor(0xffffffff,0xffff0000);
                    break;
                case 128:
                    setTextAndBackgroundColor(0xffffffff,0xffeec900);;
                    break;
                case 256:
                    setTextAndBackgroundColor(0xffffffff,0xffeee450);
                    break;
                case 512:
                    setTextAndBackgroundColor(0xffffffff,0xffeeee00);
                    break;
                case 1024:
                    setTextAndBackgroundColor(0xffffffff,0xffeec900);
                    break;
                default:
                    setTextAndBackgroundColor(0xffffffff,0xff050505);
                    break;
            }
            label.setText(String.valueOf(num));
        }
    }

    public int getNum() {
        return num;
    }

    public boolean isEquals(Card card) {
        return num==card.getNum();
    }
}
