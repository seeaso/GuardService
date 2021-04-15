package com.example.view;

import android.content.Context;
import android.os.Parcelable;
import android.util.AttributeSet;

import com.fenjuly.library.ArrowDownloadButton;

public class MyArrowDownloadButton extends ArrowDownloadButton {

    public MyArrowDownloadButton(Context context) {
        super(context);
    }
    public MyArrowDownloadButton(Context context, AttributeSet attrs) {
        super(context, attrs);
    }
    public MyArrowDownloadButton(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void onRestoreInstanceState(Parcelable state) {
        try {
            super.onRestoreInstanceState(state);
        }catch (Exception e){}
        state=null;
    }

}
