package com.usgishimura.ssh.viewmodels

import android.app.Application
import androidx.lifecycle.*
import com.usgishimura.ssh.utils.Shell
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.io.File

class ShellActivityViewModel(application: Application) : AndroidViewModel(application) {
    val shell = Shell(application.applicationContext)

    private val outputText = MutableLiveData<String>()
    fun getOutputText(): LiveData<String> = outputText

    fun connectClientAndStartOutputThread(
        username: String,
        address: String,
        port: Int,
        password: String
    ) {
        if (shell.getReady().value == true)
            return

        viewModelScope.launch(Dispatchers.IO) {
            shell.initialize(username, address, port, password)

            while (isActive && shell.session.isConnected) {
                updateOutputText(shell.outputBufferFile)
                Thread.sleep(Shell.OUTPUT_BUFFER_DELAY_MS)
            }
        }
    }

    private fun updateOutputText(file: File) {
        var out = readOutputFile(file)
        var currentText = getOutputText().value
        out = out.replace("(\\x1b\\x5b|\\x9b)[\\x30-\\x3f]*[\\x20-\\x2f]*[\\x40-\\x7e]".toRegex(),"");
        if (out != currentText)
            outputText.postValue(out)
    }

    private fun readOutputFile(file: File): String {
        val out = ByteArray(Shell.MAX_OUTPUT_BUFFER_SIZE)
        file.inputStream().use {
            val size = it.channel.size()

            if (size <= out.size)
                return String(it.readBytes())

            val newPos = (it.channel.size() - out.size)
            it.channel.position(newPos)
            it.read(out)
        }

        return String(out)
    }
}