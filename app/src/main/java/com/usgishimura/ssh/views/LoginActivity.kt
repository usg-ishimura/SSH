package com.usgishimura.ssh.views

import android.content.Intent
import android.content.SharedPreferences
import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import com.usgishimura.ssh.BuildConfig
import com.usgishimura.ssh.R
import com.usgishimura.ssh.viewmodels.LoginActivityViewModel
import com.google.android.material.button.MaterialButton
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import kotlinx.android.synthetic.main.activity_login.*
import java.io.File

class LoginActivity : AppCompatActivity() {
    private val viewModel: LoginActivityViewModel by viewModels()

    private lateinit var address: TextInputEditText
    private lateinit var port: TextInputEditText
    private lateinit var username: TextInputEditText
    private lateinit var password: TextInputEditText
    private lateinit var pubkey: MaterialButton
    private lateinit var start: ExtendedFloatingActionButton
    private lateinit var tiladdress: TextInputLayout
    private lateinit var tilport: TextInputLayout
    private lateinit var tilusername: TextInputLayout
    private lateinit var tilpassword: TextInputLayout
    private lateinit var encryptedSharedPrefs: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        address = findViewById(R.id.address)
        port = findViewById(R.id.port)
        username = findViewById(R.id.username)
        password = findViewById(R.id.password)
        pubkey = findViewById(R.id.pubkey)
        start = findViewById(R.id.start)
        tiladdress = findViewById(R.id.tiladdress)
        tilport = findViewById(R.id.tilport)
        tilusername = findViewById(R.id.tilusername)
        tilpassword = findViewById(R.id.tilpassword)
        val states = arrayOf(
            intArrayOf(-android.R.attr.state_focused), // unfocused
            intArrayOf(android.R.attr.state_focused) //focused
        )
        val colorsHint = intArrayOf(
            Color.rgb(131,125,125),
            Color.rgb(52,152,219)
        )
        val myHintList = ColorStateList(states, colorsHint)
        tiladdress.defaultHintTextColor = myHintList;
        tilport.defaultHintTextColor = myHintList;
        tilusername.defaultHintTextColor = myHintList;
        tilpassword.defaultHintTextColor = myHintList;
        val masterKeyAlias = MasterKey.Builder(this, MasterKey.DEFAULT_MASTER_KEY_ALIAS)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build()

        encryptedSharedPrefs = EncryptedSharedPreferences.create(
            this,
            MasterKey.DEFAULT_MASTER_KEY_ALIAS,
            masterKeyAlias,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )

        viewModel.prepareKeys()

        with(encryptedSharedPrefs) {
            address.setText(getString("address", "192.168.0.1"))
            port.setText(getString("port", "22"))
            username.setText(getString("username", "root"))
            password.setText(getString("password", ""))
        }

        pubkey.setOnClickListener {
            try {
                val uri = FileProvider.getUriForFile(
                    this,
                    BuildConfig.APPLICATION_ID + ".provider",
                    File("$filesDir/id_rsa.pub")
                )
                val intent = Intent(Intent.ACTION_SEND)
                with(intent) {
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                    putExtra(Intent.EXTRA_STREAM, uri)
                    type = "*/*"
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                }
                startActivity(intent)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        start.setOnClickListener {
            val intent = Intent(this, ShellActivity::class.java)
                .putExtra("address", address.text.toString())
                .putExtra("port", port.text.toString())
                .putExtra("username", username.text.toString())
                .putExtra("password", password.text.toString())
            startActivity(intent)
            /*val intent2 = Intent(this, ShellActivity::class.java)
                .putExtra("address", address.text.toString())
                .putExtra("port", port.text.toString())
                .putExtra("username", username.text.toString())
                .putExtra("password", password.text.toString())
            startActivity(intent2)*/
        }
        //Log.d("TAG", sharedPreference.getString("autostart", "defaultVal").toString())
        /*println("message 1: "+sharedPreference.getString("autostart", "defaultVal"))
        if(sharedPreference.getString("autostart", "defaultVal") == "performAutoClick") {
            editor.putString("autostart","defaultVal")
            editor.commit()
            //Log.d("TAG", sharedPreference.getString("autostart", "defaultVal").toString())
            println("message 2: "+sharedPreference.getString("autostart", "defaultVal"))
            val intent = Intent(this, ShellActivity::class.java)
                .putExtra("address", address.text.toString())
                .putExtra("port", port.text.toString())
                .putExtra("username", username.text.toString())
                .putExtra("password", password.text.toString())
            startActivity(intent)
        }*/
    }
    override fun onPause() {
        super.onPause()
        with(encryptedSharedPrefs.edit()) {
            putString("address", address.text.toString())
            putString("port", port.text.toString())
            putString("username", username.text.toString())
            putString("password", password.text.toString())
            apply()
        }
    }
}