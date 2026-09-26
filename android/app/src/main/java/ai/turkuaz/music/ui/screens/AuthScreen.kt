package ai.turkuaz.music.ui.screens

import ai.turkuaz.music.ui.components.TurkuazPrimaryButton
import ai.turkuaz.music.ui.theme.Turquoise
import ai.turkuaz.music.viewmodel.AuthUiState
import ai.turkuaz.music.viewmodel.AuthViewModel
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun AuthScreen(
    authViewModel: AuthViewModel,
    onAuthenticated: (isAdmin: Boolean) -> Unit
) {
    var isLoginMode by remember { mutableStateOf(true) }
    var email by remember { mutableStateOf("") }
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    val state by authViewModel.state.collectAsState()

    LaunchedEffect(state) {
        val s = state
        if (s is AuthUiState.Success) onAuthenticated(s.isAdmin)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 28.dp),
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            if (isLoginMode) "Giris yap" else "Hesap olustur",
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            "TURKUAZ MUSIC AI'a hosgeldin",
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(32.dp))

        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("E-posta") },
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(12.dp))

        if (!isLoginMode) {
            OutlinedTextField(
                value = username,
                onValueChange = { username = it },
                label = { Text("Kullanici adi") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(12.dp))
        }

        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Sifre") },
            singleLine = true,
            visualTransformation = PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(24.dp))

        if (state is AuthUiState.Error) {
            Text(
                (state as AuthUiState.Error).message,
                color = MaterialTheme.colorScheme.error,
                fontSize = 13.sp
            )
            Spacer(modifier = Modifier.height(12.dp))
        }

        TurkuazPrimaryButton(
            text = if (isLoginMode) "Giris yap" else "Kayit ol",
            enabled = state !is AuthUiState.Loading,
            modifier = Modifier.fillMaxWidth()
        ) {
            if (isLoginMode) authViewModel.login(email, password)
            else authViewModel.register(email, username, password)
        }

        if (state is AuthUiState.Loading) {
            Spacer(modifier = Modifier.height(16.dp))
            LinearProgressIndicator(
                modifier = Modifier.fillMaxWidth(),
                color = Turquoise
            )
        }

        Spacer(modifier = Modifier.height(20.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                if (isLoginMode) "Hesabin yok mu?" else "Zaten hesabin var mi?",
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            TextButton(onClick = { isLoginMode = !isLoginMode }) {
                Text(if (isLoginMode) "Kayit ol" else "Giris yap", color = Turquoise)
            }
        }
    }
}
