package com.audreyRetournayDiet.femSante.alim

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.ImageButton
import android.widget.Spinner
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.res.ResourcesCompat
import com.audreyRetournayDiet.femSante.R
import com.audreyRetournayDiet.femSante.utilitaires.NothingSelectedSpinnerAdapter
import com.audreyRetournayDiet.femSante.utilitaires.PdfActivity
import com.audreyRetournayDiet.femSante.utilitaires.Utilitaires

class RecetteActivity : AppCompatActivity() {

    private lateinit var recettePdf: ImageButton
    private lateinit var title: TextView
    private lateinit var spinner: Spinner
    private lateinit var help: TextView

    @SuppressLint("NewApi")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_recette)

        recettePdf = findViewById(R.id.buttonRecette)
        title = findViewById(R.id.textViewTitre)
        spinner = findViewById(R.id.spinnerMeditation)
        help = findViewById(R.id.textHelp)

        val map: HashMap<*, *>? =
            intent.getSerializableExtra("map", HashMap::class.java)

        title.text = intent.extras!!.getString("Title")

        val list = ArrayList<String>()

        for (item in map!!) {
            list.add(item.value.toString())
        }

        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, list)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinner.prompt = "Liste des recettes"
        spinner.adapter =
            NothingSelectedSpinnerAdapter(adapter, R.layout.spinner_choice, this)
        var search: String? = null


        spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {

            @SuppressLint("DiscouragedApi")
            override fun onItemSelected(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) {
                if (spinner.selectedItemId < 0) {
                    help.visibility = View.INVISIBLE
                } else {
                    recettePdf.visibility = View.VISIBLE
                    help.visibility = View.VISIBLE
                    search = Utilitaires.cleanKey(map.filterValues { it == spinner.selectedItem.toString() }.keys.toString())

                    val resId = resources.getIdentifier(search, "drawable", packageName)
                    val drawable = ResourcesCompat.getDrawable(resources, resId, null)
                    recettePdf.setImageDrawable(drawable)
                    recettePdf.contentDescription = spinner.selectedItem.toString()
                }
            }

            override fun onNothingSelected(p0: AdapterView<*>?) {}
        }

        recettePdf.setOnClickListener {
            val intentTarget = Intent(this, PdfActivity::class.java)
            intentTarget.putExtra("PDF", "$search.pdf")
            startActivity(intentTarget)
        }

    }


}