package com.ymsu.bike_compose

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel

class MainViewModel: ViewModel() {
    private val _text = mutableStateOf("Hello, Compose")
    val text: State<String> = _text

    fun updateText(newText: String){
        _text.value = newText
    }
}