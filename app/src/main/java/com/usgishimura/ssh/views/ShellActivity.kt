package com.usgishimura.ssh.views

import android.Manifest
import android.annotation.SuppressLint
import android.content.ContentValues.TAG
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.text.method.ScrollingMovementMethod
import android.util.Log
import android.view.*
import android.widget.*
import androidx.activity.viewModels
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.FileProvider
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.google.android.material.textview.MaterialTextView
import com.google.api.gax.rpc.ApiStreamObserver
import com.google.auth.oauth2.GoogleCredentials
import com.google.cloud.speech.v1.*
import com.jcraft.jsch.*
import com.usgishimura.ssh.BuildConfig
import com.usgishimura.ssh.R
import com.usgishimura.ssh.viewmodels.ShellActivityViewModel
import kotlinx.coroutines.*
import java.util.*
import java.util.concurrent.atomic.AtomicBoolean


class ShellActivity : AppCompatActivity() {
    private val viewModel: ShellActivityViewModel by viewModels()
    private lateinit var progress: ProgressBar
    private lateinit var output: MaterialTextView
    private lateinit var outputScrollView: ScrollView
    private lateinit var tilcommand: TextInputLayout
    //private var flag: Boolean = false
    private lateinit var errorDialog: AlertDialog
    // on below line we are creating variables
    // for text input edit
    private lateinit var command: TextInputEditText
    // on below line we are creating a constant value
    private val REQUEST_CODE_SPEECH_INPUT = 1
    private var listening : Boolean = false
    companion object {
        private val PERMISSIONS = arrayOf(Manifest.permission.RECORD_AUDIO)
        private const val REQUEST_RECORD_AUDIO_PERMISSION = 200
    }

    private var mPermissionToRecord = false
    private var mAudioEmitter: AudioEmitter? = null
    private lateinit var mTextView: TextSwitcher
    private lateinit var requestStream :ApiStreamObserver<StreamingRecognizeRequest>
    private val mSpeechClient by lazy {
        // NOTE: The line below uses an embedded credential (res/raw/sa.json).
        //       You should not package a credential with real application.
        //       Instead, you should get a credential securely from a server.
        applicationContext.resources.openRawResource(R.raw.credential).use {
            SpeechClient.create(
                SpeechSettings.newBuilder()
                .setCredentialsProvider { GoogleCredentials.fromStream(it) }
                .build())
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        ActivityCompat.requestPermissions(
            this, PERMISSIONS, REQUEST_RECORD_AUDIO_PERMISSION)
        setContentView(R.layout.activity_shell)
        progress = findViewById(R.id.progress)
        command = findViewById(R.id.command)
        output = findViewById(R.id.output)
        outputScrollView = findViewById(R.id.output_scrollview)
        tilcommand = findViewById(R.id.tilcommand)
        val states = arrayOf(
            intArrayOf(-android.R.attr.state_focused), // unfocused
            intArrayOf(android.R.attr.state_focused) //focused
        )
        val colorsHint = intArrayOf(
            Color.rgb(131,125,125),
            Color.rgb(52,152,219)
        )
        val myHintList = ColorStateList(states, colorsHint)
        tilcommand.defaultHintTextColor = myHintList;
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

    @RequiresApi(Build.VERSION_CODES.O)
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
                output.isFocusable = false
                output.text = text + "\n" + it
                //output.isFocusable = true
                //output.isFocusableInTouchMode = true
                //output.setTextIsSelectable(true)
                outputScrollView.post(java.lang.Runnable { outputScrollView.fullScroll(ScrollView.FOCUS_DOWN) })
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
    // on below line we are calling on activity result method.
    /*override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
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
                        command.setTextColor(Color.parseColor("#00FF00"))
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
    }*/
    @RequiresApi(Build.VERSION_CODES.M)
    @SuppressLint("RestrictedApi")
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.share -> {
                try {
                    var content = viewModel.shell.outputBufferFile.readText()
                    content = content.replace("(\\x1b\\x5b|\\x9b)[\\x30-\\x3f]*[\\x20-\\x2f]*[\\x40-\\x7e]".toRegex(),"");
                    viewModel.shell.outputBufferFile.writeText(content)
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
                if(listening){
                    mAudioEmitter?.stop()
                    mAudioEmitter = null
                    requestStream.onCompleted()
                    item.setIcon(R.drawable.baseline_mic_24)
                    listening = false
                    var aliasCommand: String = command.text.toString();
                    val fromJSON: ArrayList<String> =
                        AliasActivity().readFromJSON(applicationContext)
                    val sentence: String =
                        command.text.toString().lowercase().filter { !it.isWhitespace() }
                    val defaultTextColor = command.textColors.defaultColor
                    for (i in 1 until fromJSON.size + 1) {
                        if (i % 2 != 0 && sentence == fromJSON.get(i - 1).toString().lowercase()
                                .filter { !it.isWhitespace() }
                        ) {
                            command.setTextColor(Color.parseColor("#00FF00"))
                            aliasCommand = fromJSON.get(i)
                            break
                        }
                    }
                    // on below line we are setting data
                    // to our output text view.
                    /*command.setText(
                    command.text.toString()
                    )*/
                    Handler().postDelayed({
                        viewModel.shell.send(aliasCommand)
                        command.setText("")
                        command.setTextColor(defaultTextColor)
                    }, 1500)
                } else {
                    Resume()
                    item.setIcon(R.drawable.baseline_mic_red_24)
                    listening = true
                }
                true
            }
            R.id.ctrlc -> {
                var outputbuffertext: String = viewModel.shell.outputBufferFile.readText()
                outputbuffertext = outputbuffertext.replace("(\\x1b\\x5b|\\x9b)[\\x30-\\x3f]*[\\x20-\\x2f]*[\\x40-\\x7e]".toRegex(),"");
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
    @RequiresApi(Build.VERSION_CODES.M)
    fun Resume() {
        //super.onResume()
        // kick-off recording process, if we're allowed
        if (mPermissionToRecord) {
            val isFirstRequest = AtomicBoolean(true)
            mAudioEmitter = AudioEmitter()
            // start streaming the data to the server and collect responses
            requestStream = mSpeechClient.streamingRecognizeCallable()
                .bidiStreamingCall(object : ApiStreamObserver<StreamingRecognizeResponse> {
                    override fun onNext(value: StreamingRecognizeResponse) {
                        runOnUiThread {
                            when {
                                value.resultsCount > 0 ->{ command.setText(value.getResults(0).getAlternatives(0).transcript); command.text?.let {
                                    command.setSelection(
                                        it.length)
                                };}
                                else -> command.setText("Sorry, there was a problem!")
                            }
                        }
                    }

                    override fun onError(t: Throwable) {
                        Log.e(TAG, "an error occurred", t)
                    }

                    override fun onCompleted() {
                        Log.d(TAG, "stream closed")
                    }
                })
            // monitor the input stream and send requests as audio data becomes available
            mAudioEmitter!!.start { bytes ->
                val builder = StreamingRecognizeRequest.newBuilder()
                    .setAudioContent(bytes)

                // if first time, include the config
                if (isFirstRequest.getAndSet(false)) {
                    builder.streamingConfig = StreamingRecognitionConfig.newBuilder()
                        .setConfig(
                            RecognitionConfig.newBuilder()
                            .setLanguageCode("it-IT")
                            .setEncoding(RecognitionConfig.AudioEncoding.LINEAR16)
                            .setSampleRateHertz(16000)
                            .build())
                        .setInterimResults(true)
                        .setSingleUtterance(false)
                        .build()
                }

                // send the next request
                requestStream.onNext(builder.build())
            }
        } else {
            Log.e(TAG, "No permission to record! Please allow and then relaunch the app!")
        }
    }

    override fun onPause() {
        super.onPause()

        // ensure mic data stops
        mAudioEmitter?.stop()
        mAudioEmitter = null
    }

    override fun onDestroy() {
        super.onDestroy()
        // cleanup
        mSpeechClient.shutdown()
        errorDialog.dismiss()
    }

    @RequiresApi(Build.VERSION_CODES.M)
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode == REQUEST_RECORD_AUDIO_PERMISSION) {
            mPermissionToRecord = grantResults[0] == PackageManager.PERMISSION_GRANTED
        }

        // bail out if audio recording is not available
        if (!mPermissionToRecord) {
            finish()
        }
    }
}