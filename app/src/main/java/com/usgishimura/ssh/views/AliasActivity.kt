package com.usgishimura.ssh.views

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.ContextThemeWrapper
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import com.usgishimura.ssh.R
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken


class AliasActivity : AppCompatActivity() {

    private lateinit var plus: ExtendedFloatingActionButton
    private lateinit var ll: LinearLayout
    private lateinit var imgv: ImageView
    private var resIDVASC: Int = 0
    private lateinit var tietVASC: TextInputEditText
    private lateinit var tietVA: TextInputEditText
    private lateinit var tietSC: TextInputEditText
    private var lunghezzaLista: Int = 0
    private var saveArrayList: ArrayList<String> = ArrayList<String>()
    private var savedArrayList: ArrayList<String> = ArrayList<String>()
    private val gson = Gson()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_alias)
        plus = findViewById(R.id.addalias)
        ll = findViewById(R.id.dynamiclinearlayout)
        savedArrayList = readFromJSON(applicationContext)
        lunghezzaLista = savedArrayList.size
        Log.d("TAG", "Saved Array List: $savedArrayList")
        if(lunghezzaLista === 0) {
            addJustOneElemProgrammatically(0)

            lunghezzaLista += 2

        } else {
            addMultipleElemsProgrammatically(lunghezzaLista)

            setMultipleElemsText(lunghezzaLista, savedArrayList)
        }

        plus.setOnClickListener {

            addJustOneElemProgrammatically(lunghezzaLista)
            lunghezzaLista += 2
        }
    }
    fun addJustOneElemProgrammatically(lunghezzaLista: Int) {

            // setting voice alias programmatically
            val tilVoicealias = TextInputLayout(
                ContextThemeWrapper(
                    this,
                    R.style.Widget_MaterialComponents_TextInputLayout_OutlinedBox
                )
            )

            tilVoicealias.boxBackgroundMode = TextInputLayout.BOX_BACKGROUND_OUTLINE
            tilVoicealias.boxBackgroundColor =
                ContextCompat.getColor(tilVoicealias.context, android.R.color.transparent)
            tilVoicealias.hint = "Voice alias"

            val clpTextInputLayout = ConstraintLayout.LayoutParams(
                ConstraintLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT
            )
            tilVoicealias.layoutParams = clpTextInputLayout

            clpTextInputLayout.topMargin = 10
            clpTextInputLayout.marginStart = 16
            clpTextInputLayout.marginEnd = 16

            val edtVoicealias = TextInputEditText(tilVoicealias.context)
            edtVoicealias.id = lunghezzaLista + 1
            tilVoicealias.addView(edtVoicealias)

            tilVoicealias.setBoxCornerRadii(10f, 10f, 10f, 10f)

            // setting shell command programmatically

            val tilShellcommand = TextInputLayout(
                ContextThemeWrapper(
                    this,
                    R.style.Widget_MaterialComponents_TextInputLayout_OutlinedBox
                )
            )

            tilShellcommand.boxBackgroundMode = TextInputLayout.BOX_BACKGROUND_OUTLINE
            tilShellcommand.boxBackgroundColor =
                ContextCompat.getColor(tilVoicealias.context, android.R.color.transparent)
            tilShellcommand.hint = "Shell command"

            val clpTextInputLayoutSC = ConstraintLayout.LayoutParams(
                ConstraintLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT
            )
            tilShellcommand.layoutParams = clpTextInputLayoutSC

            clpTextInputLayoutSC.topMargin = 0
            clpTextInputLayoutSC.marginStart = 16
            clpTextInputLayoutSC.marginEnd = 16

            val edtShellcommand = TextInputEditText(tilShellcommand.context)
            edtShellcommand.id = lunghezzaLista + 2
            tilShellcommand.addView(edtShellcommand)

            tilShellcommand.setBoxCornerRadii(10f, 10f, 10f, 10f)

            // setting arrow down programmatically

            imgv = ImageView(this)
            imgv.setImageResource(R.drawable.baseline_arrow_downward_24)
            val imgvParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            imgv.layoutParams = imgvParams
            imgvParams.topMargin = 15
            val valueTV = TextView(this)
            valueTV.text = (lunghezzaLista/2 + 1).toString() + "."
            val clpTextInputLayoutTV = ConstraintLayout.LayoutParams(
                ConstraintLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT
            )
            valueTV.layoutParams = clpTextInputLayoutTV
            clpTextInputLayoutTV.topMargin = 20
            clpTextInputLayoutTV.marginStart = 16

            // adding elements

            ll.addView(valueTV)
            ll.addView(tilVoicealias)
            ll.addView(imgv)
            ll.addView(tilShellcommand)

    }
    fun setMultipleElemsText(lunghezzaLista: Int, fromJSON: ArrayList<String>){
        for (i in 1 until lunghezzaLista + 1) {
            resIDVASC = resources.getIdentifier(
                "$i",
                "id", packageName
            )
            tietVASC = findViewById(resIDVASC)
            tietVASC.setText(fromJSON.get(i-1))
        }
    }
    fun addMultipleElemsProgrammatically(lunghezzaLista: Int) {
        var i2 = 0
        for (i in 0 until lunghezzaLista/2) {
            addJustOneElemProgrammatically(i2)
            i2 += 2
        }
    }
    fun saveToJSON(aliases: ArrayList<String>) {

        val path = "aliases.json"
        try {
            applicationContext.openFileOutput(path, Context.MODE_PRIVATE).use { output ->
                val gson = Gson()
                val jsonString = gson.toJson(aliases)
                output.write(jsonString.toByteArray())
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
    fun readFromJSON(appContext: Context): ArrayList<String>{
        var text: String = ""
        val fileName = "aliases.json"
        try {
            appContext.openFileInput(fileName).use { stream ->
                text = stream.bufferedReader().use {
                    it.readText()
                }
            }

        } catch (e: Exception) {
            e.printStackTrace()
        }
        val arrayType = object : TypeToken<ArrayList<String>>() {}.type
        var fromJson: ArrayList<String> = ArrayList<String>()
        if(text != "") fromJson = gson.fromJson(text, arrayType)
        return fromJson
    }
    override fun onPause() {
        super.onPause()
        var i2 = 1
        for (i in 1 until lunghezzaLista/2 + 1) {
            //var idLunghezza = i2
            val resVA = resources.getIdentifier(
                "$i2",
                "id", packageName
            )
            tietVA = findViewById(resVA)
            i2++
            val resSC = resources.getIdentifier(
                "$i2",
                "id", packageName
            )
            tietSC = findViewById(resSC)
            if(tietVA.text.toString() != "" || tietSC.text.toString() != "") {
                saveArrayList.add(tietVA.text.toString())
                saveArrayList.add(tietSC.text.toString())
            }
            i2++
        }
        saveToJSON(saveArrayList)
        Log.d("TAG", "Saving Array List: $saveArrayList")
    }
}