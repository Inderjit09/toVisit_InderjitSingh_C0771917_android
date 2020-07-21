package com.tovisit_inderjitsingh_c0771917_android.models;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class RefreshDataBroadcastReceiver extends BroadcastReceiver {
    private NotifyData notifyData = null;

    public RefreshDataBroadcastReceiver() {

    }

    public RefreshDataBroadcastReceiver(NotifyData notifyData) {
        this.notifyData = notifyData;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        if (notifyData != null)
            notifyData.refreshData();
    }
}

