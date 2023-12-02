package com.audreyRetournayDiet.femSante.utilitaires

import android.app.Activity
import android.app.AlertDialog
import android.app.Dialog
import com.audreyRetournayDiet.femSante.R

class LoadingAlert(myActivity: Activity) {

    private var activity: Activity = myActivity
    private lateinit var dialog: Dialog

    fun startAlertDialog() {
        val builder = AlertDialog.Builder(activity)

        builder.setView(activity.layoutInflater.inflate(R.layout.alert_internet_dialog, null))
        builder.setCancelable(false)

        dialog = builder.create()
        dialog.show()
    }

    fun closeAlertDialog() {
        dialog.dismiss()
    }

}