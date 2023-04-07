package com.usgishimura.ssh.utils

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.usgishimura.ssh.R
import com.jcraft.jsch.ChannelShell
import com.jcraft.jsch.JSch
import com.jcraft.jsch.JSchException
import com.jcraft.jsch.Session
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.io.File
import java.io.PrintStream

class Shell(
    private val context: Context
) {
    companion object {
        const val MAX_CONNECTION_TIMEOUT = 60 * 1000
        const val MAX_OUTPUT_BUFFER_SIZE = 1024 * 8
        const val OUTPUT_BUFFER_DELAY_MS = 100L
    }

    private val ready = MutableLiveData<Boolean>()
    fun getReady(): LiveData<Boolean> = ready

    val error = SingleLiveEvent<String>()

    val outputBufferFile: File = File.createTempFile("buffer", ".txt").also {
        it.deleteOnExit()
    }

    private val jSch = JSch().also {
        it.addIdentity("${context.filesDir}/id_rsa")
    }

    lateinit var session: Session
    private lateinit var channel: ChannelShell

    fun initialize(
        username: String,
        address: String,
        port: Int,
        password: String
    ): Boolean {
        try {
            session = jSch.getSession(username, address, port).also {
                it.setPassword(password)
                it.setConfig("StrictHostKeyChecking", "no")
            }

            /* Connect the session */
            session.connect(MAX_CONNECTION_TIMEOUT)

            /* Initialize the shell channel */
            channel = (session.openChannel("shell") as ChannelShell).apply {
                outputStream = outputBufferFile.outputStream()
            }
            /* Connect the shell channel */
            channel.connect(MAX_CONNECTION_TIMEOUT)
            val fileBody = ""
            val fileName = "buffer.txt"
            context.openFileOutput(fileName, Context.MODE_PRIVATE).use { output ->
                output.write(fileBody.toByteArray())
            }
            ready.postValue(true)

            return true
        } catch (e: JSchException) {
            e.printStackTrace()
            error.postValue(e.message!!)

            return false
        }
    }

    fun send(command: String) {
        if (!session.isConnected) {
            error.postValue(context.getString(R.string.error_not_connected))
            return
        }

        GlobalScope.launch(Dispatchers.IO) {
            PrintStream(channel.outputStream).apply {
                //println(command+" & pid=$! && wait \$pid")
                println(command)
                flush()
            }
        }
    }

    fun deinitialize() {
        if (this::channel.isInitialized)
            channel.disconnect()
        if (this::session.isInitialized)
            session.disconnect()
        ready.postValue(false)
        outputBufferFile.delete()
    }
}