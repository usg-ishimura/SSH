package com.usgishimura.ssh.views

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.os.Handler
import android.speech.RecognizerIntent
import android.view.KeyEvent
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.ProgressBar
import android.widget.ScrollView
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import com.usgishimura.ssh.BuildConfig
import com.usgishimura.ssh.R
import com.usgishimura.ssh.viewmodels.ShellActivityViewModel
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textview.MaterialTextView
import com.jcraft.jsch.*
import kotlinx.coroutines.*
import java.util.*
import kotlin.collections.ArrayList


class ShellActivity : AppCompatActivity() {
    private val viewModel: ShellActivityViewModel by viewModels()
    private lateinit var progress: ProgressBar
    private lateinit var output: MaterialTextView
    private lateinit var outputScrollView: ScrollView
    //private var flag: Boolean = false
    private lateinit var errorDialog: AlertDialog
    // on below line we are creating variables
    // for text input edit
    private lateinit var command: TextInputEditText
    //private lateinit var microphone: Item
    // on below line we are creating a constant value
    private val REQUEST_CODE_SPEECH_INPUT = 1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_shell)
        progress = findViewById(R.id.progress)
        command = findViewById(R.id.command)
        output = findViewById(R.id.output)
        outputScrollView = findViewById(R.id.output_scrollview)

        errorDialog = MaterialAlertDialogBuilder(this)
            .setTitle(R.string.error_title)
            .setPositiveButton(R.string.error_disconnect) { _, _ -> finish() }
            .setCancelable(false)
            .create()

        command.setOnKeyListener { _, keyCode, event ->
            if (keyCode == KeyEvent.KEYCODE_ENTER && event.action == KeyEvent.ACTION_DOWN) {
                val text = command.text.toString()
                command.text = null
                viewModel.shell.send(text)
                return@setOnKeyListener true
            }

            return@setOnKeyListener false
        }
        setupShell()
    }

    private fun setupShell() {
        val address = intent.getStringExtra("address")!!
        val port = try {
            Integer.parseInt(intent.getStringExtra("port")!!)
        } catch (_: Exception) { 22 }
        val username = intent.getStringExtra("username")!!
        val password = intent.getStringExtra("password")!!
        viewModel.connectClientAndStartOutputThread(username, address, port, password)
        viewModel.shell.getReady().observe(this) {
            if (it == true) {
                progress.visibility = View.INVISIBLE
                command.isEnabled = true
            }
        }
        viewModel.shell.error.observe(this) { error(it) }
        var text: String = ""
        val fileName = "buffer.txt"
        try {
            applicationContext.openFileInput(fileName).use { stream ->
                text = stream.bufferedReader().use {
                    it.readText()
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        viewModel.getOutputText().observe(this) {
                output.text = text + "\n" + it
                outputScrollView.post {
                    outputScrollView.fullScroll(View.FOCUS_DOWN)
                }
        }
    }

    private fun error(exceptionMessage: String) {
        errorDialog.run {
            setMessage(exceptionMessage)
            show()
        }
    }

    override fun onBackPressed() {
        viewModel.shell.deinitialize()
        super.onBackPressed()
    }

    override fun onDestroy() {
        errorDialog.dismiss()
        super.onDestroy()
    }
    // on below line we are calling on activity result method.
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        // in this method we are checking request
        // code with our result code.
        if (requestCode == REQUEST_CODE_SPEECH_INPUT) {
            // on below line we are checking if result code is ok
            if (resultCode == RESULT_OK && data != null) {

                // in that case we are extracting the
                // data from our array list
                val res: ArrayList<String> =
                    data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS) as ArrayList<String>

                var aliasCommand: String = Objects.requireNonNull(res)[0].toString()
                val fromJSON: ArrayList<String> = AliasActivity().readFromJSON(applicationContext)
                val sentence: String = Objects.requireNonNull(res)[0].toString().lowercase().filter { !it.isWhitespace() }
                val defaultTextColor = command.textColors.defaultColor
                for (i in 1 until fromJSON.size+1) {
                    if(i % 2 != 0 && sentence==fromJSON.get(i-1).toString().lowercase().filter { !it.isWhitespace() }){
                        command.setTextColor(Color.parseColor("#3498db"))
                        aliasCommand = fromJSON.get(i)
                        break
                    }
                }
                // on below line we are setting data
                // to our output text view.
                command.setText(
                    Objects.requireNonNull(res)[0]
                )
                Handler().postDelayed({
                    viewModel.shell.send(aliasCommand)
                    command.setText("")
                    command.setTextColor(defaultTextColor)
                }, 1500)
            }
        }
    }
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.share -> {
                try {
                    val uri = FileProvider.getUriForFile(
                        this,
                        BuildConfig.APPLICATION_ID + ".provider",
                        viewModel.shell.outputBufferFile
                    )
                    val intent = Intent(Intent.ACTION_SEND)
                    with (intent) {
                        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                        putExtra(Intent.EXTRA_STREAM, uri)
                        type = "file/*"
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK
                    }
                    startActivity(intent)
                } catch (e: Exception) {
                    e.printStackTrace()
                    Snackbar.make(output, getString(R.string.snackbar_intent_failed), Snackbar.LENGTH_SHORT)
                        .setAction(getString(R.string.dismiss)) {}
                        .show()
                }
                true
            }
            R.id.microphone -> {
                /*if(flag === false) {
                    item.setIcon(R.drawable.baseline_mic_24)
                    flag = true
                } else {
                    item.setIcon(R.drawable.baseline_mic_none_24)
                    flag = false
                }*/
                // on below line we are calling speech recognizer intent.
                val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH)

                // on below line we are passing language model
                // and model free form in our intent
                intent.putExtra(
                    RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                    RecognizerIntent.LANGUAGE_MODEL_FREE_FORM
                )

                // on below line we are passing our
                // language as a default language.
                intent.putExtra(
                    RecognizerIntent.EXTRA_LANGUAGE,
                    Locale.getDefault()
                )

                // on below line we are specifying a prompt
                // message as speak to text on below line.
                intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "Speak to text")
                //intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault().toLanguageTag().toString())
                //Log.d("TAG", Locale.getDefault().toLanguageTag().toString())
                // on below line we are specifying a try catch block.
                // in this block we are calling a start activity
                // for result method and passing our result code.
                try {
                    startActivityForResult(intent, REQUEST_CODE_SPEECH_INPUT)
                } catch (e: Exception) {
                    // on below line we are displaying error message in toast
                    Toast
                        .makeText(
                            this@ShellActivity, " " + e.message,
                            Toast.LENGTH_SHORT
                        )
                        .show()
                }
                true
            }
            R.id.ctrlc -> {
                var outputbuffertext: String = viewModel.shell.outputBufferFile.readText()
                val fileName = "buffer.txt"
                val fileBody = outputbuffertext
                applicationContext.openFileOutput(fileName, Context.MODE_PRIVATE).use {
                        output -> output.write(fileBody.toByteArray())
                }
                //println(fileBody)
                onBackPressed()
                val address = intent.getStringExtra("address")!!
                val port = try {
                    Integer.parseInt(intent.getStringExtra("port")!!)
                } catch (_: Exception) { 22 }
                val username = intent.getStringExtra("username")!!
                val password = intent.getStringExtra("password")!!
                val intent = Intent(this, ShellActivity::class.java)
                    .putExtra("address", address)
                    .putExtra("port", port.toString())
                    .putExtra("username", username)
                    .putExtra("password", password)
                startActivity(intent)
                true
            }
            R.id.alias -> {
                val intent = Intent(this, AliasActivity::class.java)
                startActivity(intent)
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.shell, menu)
        return super.onCreateOptionsMenu(menu)
    }
}