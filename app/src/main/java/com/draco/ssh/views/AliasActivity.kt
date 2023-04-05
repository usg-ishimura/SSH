package com.draco.ssh.views

import android.os.Bundle
import android.view.ContextThemeWrapper
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import com.draco.ssh.R
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout


class AliasActivity : AppCompatActivity() {

    private lateinit var plus: ExtendedFloatingActionButton
    private lateinit var ll: LinearLayout
    private lateinit var imgv: ImageView
    private var id: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_alias)
        plus = findViewById(R.id.addalias)
        ll = findViewById(R.id.dynamiclinearlayout)


        plus.setOnClickListener {
            // counter
            id++

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
            tilShellcommand.boxBackgroundColor = ContextCompat.getColor(tilVoicealias.context, android.R.color.transparent)
            tilShellcommand.hint = "Shell command"

            val clpTextInputLayoutSC = ConstraintLayout.LayoutParams(
                ConstraintLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT
            )
            tilShellcommand.layoutParams = clpTextInputLayoutSC

            clpTextInputLayoutSC.topMargin = 0
            clpTextInputLayoutSC.marginStart = 16
            clpTextInputLayoutSC.marginEnd = 16

            val edtShellcommand = TextInputEditText(tilShellcommand.context)
            tilShellcommand.addView(edtShellcommand)

            tilShellcommand.setBoxCornerRadii(10f, 10f, 10f, 10f)

            // setting arrow down programmatically

            imgv = ImageView(this)
            imgv.setImageResource(R.drawable.baseline_arrow_downward_24)
            imgv.layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )

            val valueTV = TextView(this)
            valueTV.text = id.toString() + "."
            val clpTextInputLayoutTV = ConstraintLayout.LayoutParams(
                ConstraintLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT
            )
            valueTV.layoutParams = clpTextInputLayoutTV
            clpTextInputLayoutTV.topMargin = 10
            clpTextInputLayoutTV.marginStart = 16

            // adding elements

            ll.addView(valueTV)
            ll.addView(tilVoicealias)
            ll.addView(imgv)
            ll.addView(tilShellcommand)
        }

    }
}