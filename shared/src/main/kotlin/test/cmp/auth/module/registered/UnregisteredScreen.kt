package test.cmp.auth.module.registered

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicText
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import test.cmp.auth.App

@Composable
internal fun UnregisteredScreen() {
    val logics = App.logics<UnregisteredLogics>()
    val passphrases = remember { mutableStateOf("") }
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.Center),
        ) {
            BasicText(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                text = "enter your passphrase",
            )
            BasicTextField(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
                    .background(color = Color.LightGray, shape = RoundedCornerShape(16.dp))
                    .padding(horizontal = 16.dp, vertical = 8.dp)
                    .wrapContentHeight(),
                value = passphrases.value,
                onValueChange = { value ->
                    if (value.length < 32) {
                        passphrases.value = value
                    }
                },
                textStyle = TextStyle(fontFamily = FontFamily.Monospace),
            )
            BasicText(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(enabled = passphrases.value.isNotEmpty()) {
                        logics.register(passphrase = passphrases.value)
                    }
                    .padding(16.dp)
                    .wrapContentSize(),
                text = "register",
            )
        }
    }
}
