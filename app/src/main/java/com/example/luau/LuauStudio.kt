package com.example.luau

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.MonetizationOn
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Publish
import androidx.compose.material.icons.filled.ThumbUp
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.PublishedGameEntity
import com.example.ui.theme.BloxCyanPrimary
import com.example.ui.theme.BloxDarkBackground
import com.example.ui.theme.BloxDarkCard
import com.example.ui.theme.BloxDarkSurface
import com.example.ui.theme.BloxEmeraldGreen
import com.example.ui.theme.BloxGoldCoins
import com.example.ui.theme.BloxPurpleAccent
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@Composable
fun LuauStudioScreen(
  userPublishedGames: List<PublishedGameEntity>,
  onTestPlayGame: (title: String, luauCode: String) -> Unit,
  onPublishGame: (title: String, desc: String, genre: String, luauCode: String) -> Unit,
  onClaimPayout: (PublishedGameEntity) -> Unit,
  modifier: Modifier = Modifier
) {
  var selectedTab by remember { mutableIntStateOf(0) } // 0: Editor Luau, 1: Mis Juegos & Ganancias

  val defaultLuauScript = remember {
    """-- ============================================
-- BloxCraft Studio - Motor de Scripts Luau v1.4
-- Crea tu propio juego 3D con plataformas y física
-- ============================================

local Workspace = game.Workspace
Workspace.Gravity = 22.0
Workspace.PlayerSpeed = 9.0

-- Generar plataformas flotantes del Obby
local numPlatforms = 10
for i = 1, numPlatforms do
    local part = Instance.new("Part")
    part.Position = Vector3.new(math.sin(i) * 3, i * 1.6, i * 4.2)
    part.Size = Vector3.new(3.0, 0.6, 3.0)
    part.Color = Color3.fromRGB(0, 229, 255)
    part.Anchored = true
end

-- Añadir lago de lava peligroso abajo
local lavaFloor = Instance.new("Part")
lavaFloor.Name = "LavaHazard"
lavaFloor.Position = Vector3.new(0, -2, 20)
lavaFloor.Size = Vector3.new(50, 1, 60)
lavaFloor.Color = Color3.fromRGB(239, 68, 68)

-- Trampolín de propulsión alta
local jumpPad = Instance.new("Part")
jumpPad.Name = "TrampolineBounce"
jumpPad.Position = Vector3.new(0, 8, 22)
jumpPad.Color = Color3.fromRGB(236, 72, 153)

-- Recompensa de meta por terminar el mapa
Workspace.OnGoalReached = function(player)
    player.AddCoins(50)
    print("¡Felicidades! Has completado el mapa de " .. player.Name)
end
""".trimIndent()
  }

  var scriptCode by remember { mutableStateOf(defaultLuauScript) }
  var showPublishDialog by remember { mutableStateOf(false) }
  var publishTitle by remember { mutableStateOf("") }
  var publishDesc by remember { mutableStateOf("") }
  var publishGenre by remember { mutableStateOf("OBBY") }

  Column(
    modifier = modifier
      .fillMaxSize()
      .background(BloxDarkBackground)
      .padding(16.dp)
  ) {
    // Studio Header
    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically
    ) {
      Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
          Icons.Default.Code,
          contentDescription = "Luau Studio",
          tint = BloxPurpleAccent,
          modifier = Modifier.size(28.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Column {
          Text(
            text = "Estudio Luau 3D",
            color = TextPrimary,
            fontSize = 20.sp,
            fontWeight = FontWeight.Black
          )
          Text(
            text = "Crea mundos, publica y monetiza con BloxCoins",
            color = TextSecondary,
            fontSize = 11.sp
          )
        }
      }

      Surface(
        shape = RoundedCornerShape(12.dp),
        color = BloxPurpleAccent.copy(alpha = 0.15f),
        border = androidx.compose.foundation.BorderStroke(1.dp, BloxPurpleAccent)
      ) {
        Text(
          "Luau Engine 3D",
          color = BloxPurpleAccent,
          fontWeight = FontWeight.Bold,
          fontSize = 11.sp,
          modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
        )
      }
    }

    Spacer(modifier = Modifier.height(14.dp))

    // Studio Navigation Tabs
    TabRow(
      selectedTabIndex = selectedTab,
      containerColor = BloxDarkSurface,
      contentColor = BloxCyanPrimary
    ) {
      Tab(
        selected = selectedTab == 0,
        onClick = { selectedTab = 0 },
        text = { Text("Editor Luau", fontWeight = FontWeight.Bold) }
      )
      Tab(
        selected = selectedTab == 1,
        onClick = { selectedTab = 1 },
        text = {
          Text("Mis Juegos (${userPublishedGames.size})", fontWeight = FontWeight.Bold)
        }
      )
    }

    Spacer(modifier = Modifier.height(12.dp))

    if (selectedTab == 0) {
      // --- TAB 0: SCRIPT EDITOR ---

      // Snippet Chips
      Text(
        "Fragmentos rápidos de código Luau:",
        color = TextSecondary,
        fontSize = 11.sp,
        fontWeight = FontWeight.Medium
      )
      Spacer(modifier = Modifier.height(4.dp))
      LazyRow(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.fillMaxWidth()
      ) {
        item {
          SnippetChip("+ Trampolín") {
            scriptCode += "\n-- Trampolín de Salto\nlocal pad = Instance.new(\"Trampoline\")\npad.BounceForce = 25.0\n"
          }
        }
        item {
          SnippetChip("+ Piso de Lava") {
            scriptCode += "\n-- Bloque de Lava Mortal\nlocal lava = Instance.new(\"LavaHazard\")\nlava.Color = Color3.fromRGB(239, 68, 68)\n"
          }
        }
        item {
          SnippetChip("+ Generar Monedas") {
            scriptCode += "\n-- Generador de Monedas\nfor c = 1, 5 do\n    Workspace.SpawnCoin(Vector3.new(0, c * 2, c * 4))\nend\n"
          }
        }
        item {
          SnippetChip("+ Velocidad Jugador") {
            scriptCode += "\nWorkspace.PlayerSpeed = 12.0\n"
          }
        }
      }

      Spacer(modifier = Modifier.height(10.dp))

      // Luau Code Editor Card
      Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF090D16)),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF232D42)),
        modifier = Modifier
          .fillMaxWidth()
          .weight(1f)
      ) {
        Column(modifier = Modifier.fillMaxSize().padding(8.dp)) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Text(
              "main.luau (Script de Juego)",
              color = BloxCyanPrimary,
              fontSize = 12.sp,
              fontFamily = FontFamily.Monospace,
              fontWeight = FontWeight.Bold
            )
            Text(
              "${scriptCode.lines().size} líneas",
              color = TextMuted,
              fontSize = 11.sp
            )
          }

          Spacer(modifier = Modifier.height(6.dp))

          OutlinedTextField(
            value = scriptCode,
            onValueChange = { scriptCode = it },
            modifier = Modifier
              .fillMaxSize()
              .testTag("luau_script_input"),
            textStyle = androidx.compose.ui.text.TextStyle(
              fontFamily = FontFamily.Monospace,
              fontSize = 12.sp,
              color = Color(0xFFE2E8F0),
              lineHeight = 18.sp
            ),
            colors = OutlinedTextFieldDefaults.colors(
              focusedContainerColor = Color(0xFF060910),
              unfocusedContainerColor = Color(0xFF060910),
              focusedBorderColor = BloxPurpleAccent,
              unfocusedBorderColor = Color(0xFF1E293B)
            )
          )
        }
      }

      Spacer(modifier = Modifier.height(12.dp))

      // Action Buttons (Playtest 3D & Publish)
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
      ) {
        Button(
          onClick = {
            onTestPlayGame("Prueba de Juego Luau", scriptCode)
          },
          colors = ButtonDefaults.buttonColors(containerColor = BloxEmeraldGreen),
          shape = RoundedCornerShape(12.dp),
          modifier = Modifier.weight(1f).testTag("test_play_button")
        ) {
          Icon(Icons.Default.PlayArrow, contentDescription = "Probar")
          Spacer(modifier = Modifier.width(6.dp))
          Text("Probar en 3D", fontWeight = FontWeight.Bold)
        }

        Button(
          onClick = { showPublishDialog = true },
          colors = ButtonDefaults.buttonColors(containerColor = BloxPurpleAccent),
          shape = RoundedCornerShape(12.dp),
          modifier = Modifier.weight(1f).testTag("publish_game_button")
        ) {
          Icon(Icons.Default.Publish, contentDescription = "Publicar")
          Spacer(modifier = Modifier.width(6.dp))
          Text("Publicar Juego", fontWeight = FontWeight.Bold)
        }
      }

    } else {
      // --- TAB 1: MIS JUEGOS Y MONETIZACIÓN DE CREADOR ---
      if (userPublishedGames.isEmpty()) {
        Card(
          shape = RoundedCornerShape(16.dp),
          colors = CardDefaults.cardColors(containerColor = BloxDarkCard),
          modifier = Modifier.fillMaxWidth().padding(top = 24.dp)
        ) {
          Column(
            modifier = Modifier.padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
          ) {
            Icon(
              Icons.Default.Code,
              contentDescription = "Sin juegos",
              tint = BloxCyanPrimary,
              modifier = Modifier.size(56.dp)
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
              "Aún no has publicado juegos",
              color = TextPrimary,
              fontSize = 17.sp,
              fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
              "Escribe tu código en el Editor Luau y presiona 'Publicar Juego'. ¡Ganarás BloxCoins cuando otros jugadores visiten tu mundo!",
              color = TextSecondary,
              fontSize = 13.sp,
              textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
            Spacer(modifier = Modifier.height(16.dp))
            Button(
              onClick = { selectedTab = 0 },
              colors = ButtonDefaults.buttonColors(containerColor = BloxCyanPrimary)
            ) {
              Text("Ir al Editor Luau", color = Color.Black, fontWeight = FontWeight.Bold)
            }
          }
        }
      } else {
        LazyColumn(
          verticalArrangement = Arrangement.spacedBy(12.dp),
          modifier = Modifier.fillMaxSize()
        ) {
          item {
            Card(
              shape = RoundedCornerShape(14.dp),
              colors = CardDefaults.cardColors(containerColor = BloxDarkSurface),
              border = androidx.compose.foundation.BorderStroke(1.dp, BloxGoldCoins.copy(alpha = 0.5f))
            ) {
              Row(
                modifier = Modifier.padding(14.dp),
                verticalAlignment = Alignment.CenterVertically
              ) {
                Icon(
                  Icons.Default.MonetizationOn,
                  contentDescription = "Monedas",
                  tint = BloxGoldCoins,
                  modifier = Modifier.size(36.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                  Text(
                    "Ingresos de Creador de Juegos",
                    color = BloxGoldCoins,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                  )
                  Text(
                    "Cada visita y 'Me Gusta' de la comunidad genera pagos automáticos en BloxCoins para ti.",
                    color = TextSecondary,
                    fontSize = 12.sp
                  )
                }
              }
            }
          }

          items(userPublishedGames) { game ->
            PublishedGameCard(
              game = game,
              onPlay = { onTestPlayGame(game.title, game.luauCode) },
              onClaim = { onClaimPayout(game) }
            )
          }
        }
      }
    }
  }

  // Publish Dialog
  if (showPublishDialog) {
    AlertDialog(
      onDismissRequest = { showPublishDialog = false },
      title = {
        Text("Publicar Juego en la Comunidad", color = TextPrimary, fontWeight = FontWeight.Black)
      },
      text = {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
          Text(
            "Tu juego estará visible en el catálogo de Descubrir para todos los jugadores. Ganarás +30 BloxCoins por publicarlo y regalías por cada visita.",
            color = TextSecondary,
            fontSize = 12.sp
          )
          OutlinedTextField(
            value = publishTitle,
            onValueChange = { publishTitle = it },
            label = { Text("Título del Juego") },
            placeholder = { Text("Ej: Obby Espacial de Neón") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth().testTag("publish_title_input")
          )
          OutlinedTextField(
            value = publishDesc,
            onValueChange = { publishDesc = it },
            label = { Text("Descripción") },
            placeholder = { Text("Explica cómo superar los obstáculos...") },
            maxLines = 3,
            modifier = Modifier.fillMaxWidth().testTag("publish_desc_input")
          )
        }
      },
      confirmButton = {
        Button(
          onClick = {
            if (publishTitle.isNotBlank()) {
              onPublishGame(
                publishTitle.trim(),
                if (publishDesc.isNotBlank()) publishDesc.trim() else "Juego creado con Luau",
                publishGenre,
                scriptCode
              )
              showPublishDialog = false
              publishTitle = ""
              publishDesc = ""
              selectedTab = 1
            }
          },
          colors = ButtonDefaults.buttonColors(containerColor = BloxPurpleAccent),
          modifier = Modifier.testTag("confirm_publish_button")
        ) {
          Text("Publicar Ahora (+30 🪙)")
        }
      },
      dismissButton = {
        OutlinedButton(onClick = { showPublishDialog = false }) {
          Text("Cancelar")
        }
      },
      containerColor = BloxDarkSurface
    )
  }
}

@Composable
fun SnippetChip(text: String, onClick: () -> Unit) {
  Surface(
    shape = RoundedCornerShape(16.dp),
    color = Color(0xFF1E2538),
    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF2F3B59)),
    modifier = Modifier.clickable { onClick() }
  ) {
    Text(
      text = text,
      color = BloxCyanPrimary,
      fontSize = 11.sp,
      fontWeight = FontWeight.Bold,
      modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
    )
  }
}

@Composable
fun PublishedGameCard(
  game: PublishedGameEntity,
  onPlay: () -> Unit,
  onClaim: () -> Unit
) {
  Card(
    shape = RoundedCornerShape(16.dp),
    colors = CardDefaults.cardColors(containerColor = BloxDarkCard),
    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF28324A)),
    modifier = Modifier.fillMaxWidth()
  ) {
    Column(modifier = Modifier.padding(16.dp)) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Column {
          Text(game.title, color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 16.sp)
          Text(game.description, color = TextSecondary, fontSize = 12.sp, maxLines = 2)
        }
        Surface(
          shape = RoundedCornerShape(8.dp),
          color = BloxPurpleAccent.copy(alpha = 0.2f)
        ) {
          Text(
            game.genre,
            color = BloxPurpleAccent,
            fontWeight = FontWeight.Bold,
            fontSize = 11.sp,
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
          )
        }
      }

      Spacer(modifier = Modifier.height(12.dp))

      // Stats row
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.Visibility, contentDescription = "Visitas", tint = BloxCyanPrimary, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(4.dp))
            Text("${game.visits} visitas", color = TextPrimary, fontSize = 12.sp)
          }
          Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.ThumbUp, contentDescription = "Likes", tint = BloxEmeraldGreen, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(4.dp))
            Text("${game.likes} likes", color = TextPrimary, fontSize = 12.sp)
          }
        }

        // Pending Payout
        if (game.pendingPayoutCoins > 0) {
          Button(
            onClick = onClaim,
            colors = ButtonDefaults.buttonColors(containerColor = BloxGoldCoins),
            shape = RoundedCornerShape(12.dp),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 10.dp, vertical = 4.dp),
            modifier = Modifier.testTag("claim_payout_button_${game.id}")
          ) {
            Text(
              "Cobrar +${game.pendingPayoutCoins} 🪙",
              color = Color.Black,
              fontWeight = FontWeight.Black,
              fontSize = 11.sp
            )
          }
        } else {
          Text(
            "Al día",
            color = TextMuted,
            fontSize = 12.sp
          )
        }
      }

      Spacer(modifier = Modifier.height(8.dp))

      Button(
        onClick = onPlay,
        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF26324A)),
        modifier = Modifier.fillMaxWidth()
      ) {
        Icon(Icons.Default.PlayArrow, contentDescription = "Jugar", modifier = Modifier.size(16.dp))
        Spacer(modifier = Modifier.width(6.dp))
        Text("Probar Mi Juego en 3D", fontSize = 12.sp)
      }
    }
  }
}
