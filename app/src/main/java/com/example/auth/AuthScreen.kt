package com.example.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.draw.clip
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Gamepad
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.MonetizationOn
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.BloxCyanPrimary
import com.example.ui.theme.BloxDarkBackground
import com.example.ui.theme.BloxDarkCard
import com.example.ui.theme.BloxDarkSurface
import com.example.ui.theme.BloxEmeraldGreen
import com.example.ui.theme.BloxGoldCoins
import com.example.ui.theme.BloxPurpleAccent
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@Composable
fun AuthScreen(
  onLogin: (username: String, pass: String) -> Unit,
  onRegister: (username: String, pass: String, displayName: String) -> Unit,
  errorMessage: String?,
  modifier: Modifier = Modifier
) {
  var isRegisterTab by remember { mutableStateOf(false) }

  var username by remember { mutableStateOf("") }
  var password by remember { mutableStateOf("") }
  var displayName by remember { mutableStateOf("") }
  var showPassword by remember { mutableStateOf(false) }

  Box(
    modifier = modifier
      .fillMaxSize()
      .background(
        Brush.verticalGradient(
          listOf(Color(0xFF0F172A), BloxDarkBackground, Color(0xFF130E26))
        )
      ),
    contentAlignment = Alignment.Center
  ) {
    Card(
      shape = RoundedCornerShape(24.dp),
      colors = CardDefaults.cardColors(containerColor = BloxDarkSurface),
      border = androidx.compose.foundation.BorderStroke(1.dp, BloxPurpleAccent.copy(alpha = 0.5f)),
      modifier = Modifier
        .fillMaxWidth(0.92f)
        .padding(16.dp)
    ) {
      Column(
        modifier = Modifier.padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
      ) {
        // App Title & Logo
        Row(verticalAlignment = Alignment.CenterVertically) {
          Icon(
            Icons.Default.Gamepad,
            contentDescription = "BloxCraft",
            tint = BloxCyanPrimary,
            modifier = Modifier.size(36.dp)
          )
          Spacer(modifier = Modifier.width(8.dp))
          Text(
            text = "BloxCraft 3D",
            color = TextPrimary,
            fontSize = 26.sp,
            fontWeight = FontWeight.Black
          )
        }

        Spacer(modifier = Modifier.height(4.dp))
        Text(
          text = if (isRegisterTab) "Crea tu cuenta para guardar tu progreso" else "Inicia sesión con tu usuario y contraseña",
          color = TextSecondary,
          fontSize = 12.sp,
          textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(18.dp))

        // Tabs: Iniciar Sesión vs Registro
        TabRow(
          selectedTabIndex = if (isRegisterTab) 1 else 0,
          containerColor = BloxDarkCard,
          contentColor = BloxCyanPrimary,
          modifier = Modifier.clip(RoundedCornerShape(12.dp))
        ) {
          Tab(
            selected = !isRegisterTab,
            onClick = { isRegisterTab = false },
            text = { Text("Iniciar Sesión", fontWeight = FontWeight.Bold, fontSize = 13.sp) },
            modifier = Modifier.testTag("tab_login")
          )
          Tab(
            selected = isRegisterTab,
            onClick = { isRegisterTab = true },
            text = { Text("Crear Cuenta", fontWeight = FontWeight.Bold, fontSize = 13.sp) },
            modifier = Modifier.testTag("tab_register")
          )
        }

        Spacer(modifier = Modifier.height(18.dp))

        // Bonus Welcome Banner
        if (isRegisterTab) {
          Surface(
            shape = RoundedCornerShape(12.dp),
            color = BloxGoldCoins.copy(alpha = 0.15f),
            border = androidx.compose.foundation.BorderStroke(1.dp, BloxGoldCoins.copy(alpha = 0.5f)),
            modifier = Modifier.fillMaxWidth()
          ) {
            Row(
              modifier = Modifier.padding(10.dp),
              verticalAlignment = Alignment.CenterVertically
            ) {
              Icon(Icons.Default.MonetizationOn, contentDescription = null, tint = BloxGoldCoins, modifier = Modifier.size(20.dp))
              Spacer(modifier = Modifier.width(6.dp))
              Text(
                "¡Bono de Bienvenida: +250 BloxCoins 🪙 y objetos iniciales gratis!",
                color = BloxGoldCoins,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold
              )
            }
          }
          Spacer(modifier = Modifier.height(12.dp))
        }

        // Input Fields
        OutlinedTextField(
          value = username,
          onValueChange = { username = it },
          label = { Text("Usuario") },
          placeholder = { Text("Ej: BloxMaster22") },
          leadingIcon = { Icon(Icons.Default.Person, contentDescription = null, tint = BloxCyanPrimary) },
          singleLine = true,
          modifier = Modifier.fillMaxWidth().testTag("auth_username_input"),
          shape = RoundedCornerShape(14.dp),
          colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = BloxCyanPrimary,
            unfocusedBorderColor = Color(0xFF28324A)
          )
        )

        if (isRegisterTab) {
          Spacer(modifier = Modifier.height(10.dp))
          OutlinedTextField(
            value = displayName,
            onValueChange = { displayName = it },
            label = { Text("Nombre para Mostrar") },
            placeholder = { Text("Ej: Tu Apodo Gamer") },
            leadingIcon = { Icon(Icons.Default.AccountCircle, contentDescription = null, tint = BloxPurpleAccent) },
            singleLine = true,
            modifier = Modifier.fillMaxWidth().testTag("auth_displayname_input"),
            shape = RoundedCornerShape(14.dp),
            colors = OutlinedTextFieldDefaults.colors(
              focusedBorderColor = BloxPurpleAccent,
              unfocusedBorderColor = Color(0xFF28324A)
            )
          )
        }

        Spacer(modifier = Modifier.height(10.dp))

        OutlinedTextField(
          value = password,
          onValueChange = { password = it },
          label = { Text("Contraseña") },
          placeholder = { Text("Mínimo 4 caracteres") },
          leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null, tint = BloxGoldCoins) },
          trailingIcon = {
            IconButton(onClick = { showPassword = !showPassword }) {
              Icon(
                if (showPassword) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                contentDescription = null,
                tint = TextSecondary
              )
            }
          },
          visualTransformation = if (showPassword) VisualTransformation.None else PasswordVisualTransformation(),
          keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
          singleLine = true,
          modifier = Modifier.fillMaxWidth().testTag("auth_password_input"),
          shape = RoundedCornerShape(14.dp),
          colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = BloxCyanPrimary,
            unfocusedBorderColor = Color(0xFF28324A)
          )
        )

        // Error message display
        if (!errorMessage.isNullOrBlank()) {
          Spacer(modifier = Modifier.height(8.dp))
          Text(
            text = errorMessage,
            color = Color(0xFFEF4444),
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
          )
        }

        Spacer(modifier = Modifier.height(18.dp))

        // Main Action Button
        Button(
          onClick = {
            if (isRegisterTab) {
              onRegister(username, password, displayName)
            } else {
              onLogin(username, password)
            }
          },
          colors = ButtonDefaults.buttonColors(
            containerColor = if (isRegisterTab) BloxPurpleAccent else BloxCyanPrimary
          ),
          shape = RoundedCornerShape(14.dp),
          modifier = Modifier
            .fillMaxWidth()
            .height(50.dp)
            .testTag("auth_submit_button")
        ) {
          Text(
            text = if (isRegisterTab) "Crear Cuenta & Entrar" else "Iniciar Sesión",
            color = if (isRegisterTab) Color.White else Color.Black,
            fontSize = 15.sp,
            fontWeight = FontWeight.Black
          )
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Quick demo login helper
        Surface(
          shape = RoundedCornerShape(10.dp),
          color = Color(0xFF1E2538),
          modifier = Modifier.padding(top = 4.dp)
        ) {
          Text(
            "Tus progresos, monedas e inventario quedan guardados permanentemente.",
            color = TextSecondary,
            fontSize = 11.sp,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(8.dp)
          )
        }
      }
    }
  }
}
