package com.example.firetictactoe;

import android.app.Dialog;
import android.content.Context;

import androidx.annotation.NonNull;

public class WinDialog extends Dialog {
    private final String message;
    public WinDialog(@NonNull Context context, String message) {
        super(context);
        this.message = message;
    }
}
