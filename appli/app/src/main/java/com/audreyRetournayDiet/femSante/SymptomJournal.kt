package com.audreyRetournayDiet.femSante

import android.os.Bundle
import android.widget.CalendarView
import androidx.appcompat.app.AppCompatActivity
import com.audreyRetournayDiet.femSante.R.*
import java.util.*

class SymptomJournal : AppCompatActivity() {

    private var calendar : CalendarView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(layout.activity_symptom_journal)
        calendar = findViewById(id.calendar)
        calendar!!.date = Calendar.getInstance().timeInMillis
    }
}