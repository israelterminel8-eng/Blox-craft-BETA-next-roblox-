package com.example.game3d

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.MonetizationOn
import androidx.compose.material.icons.filled.Navigation
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.BloxCyanPrimary
import com.example.ui.theme.BloxDarkBackground
import com.example.ui.theme.BloxDarkSurface
import com.example.ui.theme.BloxEmeraldGreen
import com.example.ui.theme.BloxGoldCoins
import com.example.ui.theme.BloxLavaRed
import com.example.ui.theme.BloxPurpleAccent
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

data class InGameChatMessage(val sender: String, val text: String, val timestamp: Long)

@Composable
fun Game3DCanvas(
  world: World3D,
  playerConfig: AvatarCustomization,
  playerName: String,
  onExitGame: () -> Unit,
  onCoinsEarned: (Long) -> Unit,
  onRareItemFound: (name: String, category: String, rarity: String, sourceGame: String) -> Unit,
  modifier: Modifier = Modifier
) {
  val player = remember {
    Character3D(
      position = world.spawnPoint,
      name = playerName,
      config = playerConfig
    )
  }

  val camera = remember { Camera3D(target = player.position) }

  // Input states
  var joystickOffset by remember { mutableStateOf(Offset.Zero) }
  val joystickRadius = 85f
  var isJumpingRequested by remember { mutableStateOf(false) }

  // Game UI notifications
  var notificationText by remember { mutableStateOf<String?>(null) }
  var notificationSubtext by remember { mutableStateOf<String?>(null) }
  var showGoalVictory by remember { mutableStateOf(false) }
  var collectedCoinsSession by remember { mutableIntStateOf(0) }
  var showInGameChat by remember { mutableStateOf(false) }
  var chatInputText by remember { mutableStateOf("") }
  val inGameMessages = remember {
    mutableStateListOf(
      InGameChatMessage("Sistema", "¡Bienvenido a ${world.title}!", System.currentTimeMillis()),
      InGameChatMessage("AlexGamer99", "¡Cuidado con la lava en el tramo medio!", System.currentTimeMillis() - 5000)
    )
  }

  // Camera Orbit angles
  var camYaw by remember { mutableFloatStateOf(0f) }
  var camPitch by remember { mutableFloatStateOf(0.35f) }

  // Main 60FPS Game Loop
  LaunchedEffect(Unit) {
    var lastNanoTime = System.nanoTime()
    while (true) {
      withFrameNanos { now ->
        val dt = ((now - lastNanoTime) / 1_000_000_000f).coerceIn(0.001f, 0.05f)
        lastNanoTime = now

        // Calculate normalized joystick movement (-1f to 1f)
        val moveX = (joystickOffset.x / joystickRadius).coerceIn(-1f, 1f)
        val moveZ = (joystickOffset.y / joystickRadius).coerceIn(-1f, 1f)

        if (isJumpingRequested) {
          world.jump()
          isJumpingRequested = false
        }

        // Physics update
        world.updatePhysics(
          player = player,
          inputMoveX = moveX,
          inputMoveZ = -moveZ,
          cameraYaw = camYaw,
          dt = dt,
          onCoinCollected = { coins ->
            collectedCoinsSession += coins
            onCoinsEarned(coins.toLong())
          },
          onRareItemCollected = { name, cat, rarity ->
            notificationText = "¡OBJETO DESBLOQUEADO!"
            notificationSubtext = "$name ($rarity) guardado en tu inventario"
            onRareItemFound(name, cat, rarity, world.title)
          },
          onLavaHit = {
            notificationText = "¡CUIDADO! HAS CAÍDO"
            notificationSubtext = "Reapareciendo en el último punto de control..."
          },
          onGoalReached = {
            if (!showGoalVictory) {
              showGoalVictory = true
              onCoinsEarned(50L)
              collectedCoinsSession += 50
            }
          }
        )

        // Camera follow avatar smoothly
        camera.target = Vec3(
          player.position.x,
          player.position.y + 1.6f,
          player.position.z
        )
        camera.yaw = camYaw
        camera.pitch = camPitch
      }
    }
  }

  BoxWithConstraints(
    modifier = modifier
      .fillMaxSize()
      .background(world.skyColor)
  ) {
    val screenW = maxWidth.value
    val screenH = maxHeight.value

    // Touch detector for Camera Orbit (Right half of the screen)
    Box(
      modifier = Modifier
        .fillMaxSize()
        .pointerInput(Unit) {
          detectDragGestures { change, dragAmount ->
            // If dragging on right side of screen, rotate camera
            if (change.position.x > size.width * 0.35f) {
              change.consume()
              camYaw += dragAmount.x * 0.007f
              camPitch = (camPitch - dragAmount.y * 0.005f).coerceIn(0.05f, 1.25f)
            }
          }
        }
    )

    // 3D Rendering Canvas
    Canvas(modifier = Modifier.fillMaxSize()) {
      val w = size.width
      val h = size.height

      // Render Horizon & Sky Gradient
      drawRect(
        brush = Brush.verticalGradient(
          colors = listOf(world.skyColor, world.groundColor),
          startY = 0f,
          endY = h
        )
      )

      // Collect all 3D faces from:
      // 1. World platform blocks
      // 2. Main player avatar meshes
      // 3. Other players' avatars in world
      val allFaces = mutableListOf<PolygonFace>()

      for (block in world.blocks) {
        allFaces.addAll(block.generateFaces(camera, w, h))
      }

      for (box in player.generateMeshBoxes()) {
        allFaces.addAll(box.generateFaces(camera, w, h))
      }

      for (other in world.otherPlayers) {
        for (box in other.generateMeshBoxes()) {
          allFaces.addAll(box.generateFaces(camera, w, h))
        }
      }

      // Painter's Algorithm: Sort faces from farthest to closest (descending depth)
      allFaces.sortByDescending { it.avgDepth }

      // Draw all 3D shaded polygon faces!
      renderPolygonFaces(allFaces)
    }

    // --- HUD OVERLAYS ---

    // Top Game Header
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 16.dp, vertical = 12.dp)
        .align(Alignment.TopCenter),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically
    ) {
      Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
          .background(Color.Black.copy(alpha = 0.6f), RoundedCornerShape(24.dp))
          .padding(horizontal = 8.dp, vertical = 4.dp)
      ) {
        IconButton(
          onClick = onExitGame,
          modifier = Modifier.size(36.dp).testTag("exit_game_button")
        ) {
          Icon(Icons.Default.ArrowBack, contentDescription = "Salir", tint = Color.White)
        }
        Spacer(modifier = Modifier.width(6.dp))
        Text(
          text = world.title,
          color = Color.White,
          fontSize = 14.sp,
          fontWeight = FontWeight.Bold,
          modifier = Modifier.padding(end = 8.dp)
        )
      }

      Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
      ) {
        // Session Coins
        Surface(
          shape = RoundedCornerShape(20.dp),
          color = Color(0xFF1E293B).copy(alpha = 0.85f),
          border = androidx.compose.foundation.BorderStroke(1.dp, BloxGoldCoins)
        ) {
          Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
          ) {
            Icon(Icons.Default.MonetizationOn, contentDescription = "Monedas", tint = BloxGoldCoins, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(4.dp))
            Text(
              text = "+$collectedCoinsSession 🪙",
              color = BloxGoldCoins,
              fontSize = 13.sp,
              fontWeight = FontWeight.Bold
            )
          }
        }

        // Chat Toggle Button
        FilledIconButton(
          onClick = { showInGameChat = !showInGameChat },
          colors = IconButtonDefaults.filledIconButtonColors(containerColor = BloxDarkSurface.copy(alpha = 0.85f)),
          modifier = Modifier.size(40.dp)
        ) {
          Icon(Icons.Default.Chat, contentDescription = "Chat del Juego", tint = BloxCyanPrimary)
        }
      }
    }

    // In-Game Notification Banner (Item unlocked / hazard alert)
    AnimatedVisibility(
      visible = notificationText != null,
      enter = fadeIn() + slideInVertically(),
      exit = fadeOut() + slideOutVertically(),
      modifier = Modifier
        .align(Alignment.TopCenter)
        .padding(top = 70.dp)
    ) {
      Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = BloxPurpleAccent.copy(alpha = 0.95f)),
        elevation = CardDefaults.cardElevation(8.dp),
        modifier = Modifier.padding(horizontal = 24.dp)
      ) {
        Column(
          modifier = Modifier.padding(horizontal = 20.dp, vertical = 10.dp),
          horizontalAlignment = Alignment.CenterHorizontally
        ) {
          Text(
            text = notificationText ?: "",
            color = Color.White,
            fontWeight = FontWeight.Black,
            fontSize = 14.sp
          )
          notificationSubtext?.let {
            Text(text = it, color = Color.White.copy(alpha = 0.9f), fontSize = 12.sp)
          }
        }
      }
    }

    // Auto dismiss notifications after 3 seconds
    LaunchedEffect(notificationText) {
      if (notificationText != null) {
        kotlinx.coroutines.delay(3200)
        notificationText = null
        notificationSubtext = null
      }
    }

    // Virtual Joystick on Bottom-Left
    Box(
      modifier = Modifier
        .align(Alignment.BottomStart)
        .padding(start = 24.dp, bottom = 32.dp)
        .size(170.dp)
        .pointerInput(Unit) {
          detectDragGestures(
            onDragEnd = { joystickOffset = Offset.Zero },
            onDragCancel = { joystickOffset = Offset.Zero },
            onDrag = { change, dragAmount ->
              change.consume()
              val newOffset = joystickOffset + dragAmount
              val dist = sqrt(newOffset.x * newOffset.x + newOffset.y * newOffset.y)
              joystickOffset = if (dist <= joystickRadius) {
                newOffset
              } else {
                val angle = atan2(newOffset.y, newOffset.x)
                Offset(cos(angle) * joystickRadius, sin(angle) * joystickRadius)
              }
            }
          )
        },
      contentAlignment = Alignment.Center
    ) {
      // Joystick Base Outer Circle
      Canvas(modifier = Modifier.fillMaxSize()) {
        val center = Offset(size.width / 2f, size.height / 2f)
        drawCircle(
          color = Color.Black.copy(alpha = 0.35f),
          radius = joystickRadius,
          center = center
        )
        drawCircle(
          color = BloxCyanPrimary.copy(alpha = 0.5f),
          radius = joystickRadius,
          center = center,
          style = Stroke(width = 2.dp.toPx())
        )
      }

      // Joystick Thumb Knob
      Box(
        modifier = Modifier
          .offset { IntOffset(joystickOffset.x.toInt(), joystickOffset.y.toInt()) }
          .size(56.dp)
          .clip(CircleShape)
          .background(
            Brush.radialGradient(
              listOf(BloxCyanPrimary, Color(0xFF007799))
            )
          ),
        contentAlignment = Alignment.Center
      ) {
        Icon(
          Icons.Default.Navigation,
          contentDescription = "Mover",
          tint = Color.White,
          modifier = Modifier.size(24.dp)
        )
      }
    }

    // Jump Action Button on Bottom-Right
    Box(
      modifier = Modifier
        .align(Alignment.BottomEnd)
        .padding(end = 28.dp, bottom = 44.dp)
    ) {
      FilledIconButton(
        onClick = { isJumpingRequested = true },
        colors = IconButtonDefaults.filledIconButtonColors(
          containerColor = BloxEmeraldGreen
        ),
        modifier = Modifier
          .size(68.dp)
          .testTag("jump_button")
      ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
          Icon(
            Icons.Default.Refresh,
            contentDescription = "Saltar",
            tint = Color.White,
            modifier = Modifier.size(32.dp)
          )
          Text("SALTAR", color = Color.White, fontSize = 9.sp, fontWeight = FontWeight.Black)
        }
      }
    }

    // In-Game Chat Floating Panel
    AnimatedVisibility(
      visible = showInGameChat,
      enter = fadeIn() + slideInVertically(initialOffsetY = { it / 2 }),
      exit = fadeOut(),
      modifier = Modifier
        .align(Alignment.BottomCenter)
        .padding(bottom = 120.dp, start = 16.dp, end = 16.dp)
    ) {
      Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = BloxDarkSurface.copy(alpha = 0.95f)),
        border = androidx.compose.foundation.BorderStroke(1.dp, BloxPurpleAccent),
        modifier = Modifier
          .fillMaxWidth(0.92f)
          .height(220.dp)
      ) {
        Column(modifier = Modifier.fillMaxSize().padding(12.dp)) {
          Text(
            "Chat del Juego 💬",
            color = BloxCyanPrimary,
            fontWeight = FontWeight.Bold,
            fontSize = 13.sp
          )
          Spacer(modifier = Modifier.height(6.dp))

          // Messages list
          Column(
            modifier = Modifier
              .weight(1f)
              .fillMaxWidth()
          ) {
            inGameMessages.takeLast(4).forEach { msg ->
              Row(modifier = Modifier.padding(vertical = 2.dp)) {
                Text(
                  "${msg.sender}: ",
                  color = BloxGoldCoins,
                  fontSize = 12.sp,
                  fontWeight = FontWeight.Bold
                )
                Text(
                  msg.text,
                  color = Color.White,
                  fontSize = 12.sp
                )
              }
            }
          }

          // Chat input row
          Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
          ) {
            OutlinedTextField(
              value = chatInputText,
              onValueChange = { chatInputText = it },
              placeholder = { Text("Escribe un mensaje...", fontSize = 12.sp) },
              modifier = Modifier.weight(1f),
              shape = RoundedCornerShape(12.dp),
              colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White,
                focusedBorderColor = BloxCyanPrimary,
                unfocusedBorderColor = Color.Gray
              ),
              singleLine = true
            )
            Spacer(modifier = Modifier.width(6.dp))
            IconButton(
              onClick = {
                if (chatInputText.isNotBlank()) {
                  inGameMessages.add(
                    InGameChatMessage(playerName, chatInputText.trim(), System.currentTimeMillis())
                  )
                  chatInputText = ""
                }
              },
              modifier = Modifier
                .background(BloxCyanPrimary, RoundedCornerShape(10.dp))
                .size(44.dp)
            ) {
              Icon(Icons.Default.Send, contentDescription = "Enviar", tint = Color.Black)
            }
          }
        }
      }
    }

    // Goal Victory Dialog
    if (showGoalVictory) {
      Box(
        modifier = Modifier
          .fillMaxSize()
          .background(Color.Black.copy(alpha = 0.75f)),
        contentAlignment = Alignment.Center
      ) {
        Card(
          shape = RoundedCornerShape(24.dp),
          colors = CardDefaults.cardColors(containerColor = BloxDarkSurface),
          border = androidx.compose.foundation.BorderStroke(2.dp, BloxGoldCoins),
          modifier = Modifier.padding(32.dp)
        ) {
          Column(
            modifier = Modifier.padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
          ) {
            Icon(
              Icons.Default.EmojiEvents,
              contentDescription = "Victoria",
              tint = BloxGoldCoins,
              modifier = Modifier.size(64.dp)
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
              "¡NIVEL COMPLETADO!",
              color = Color.White,
              fontSize = 20.sp,
              fontWeight = FontWeight.Black
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
              "Has superado el circuito y desbloqueado recompensas.",
              color = Color.LightGray,
              fontSize = 13.sp,
              textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(16.dp))
            Surface(
              shape = RoundedCornerShape(12.dp),
              color = BloxGoldCoins.copy(alpha = 0.15f),
              border = androidx.compose.foundation.BorderStroke(1.dp, BloxGoldCoins)
            ) {
              Text(
                "🏆 Recompensa: +50 BloxCoins",
                color = BloxGoldCoins,
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
              )
            }
            Spacer(modifier = Modifier.height(20.dp))
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
              Button(
                onClick = {
                  player.position = world.spawnPoint
                  showGoalVictory = false
                },
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF334155))
              ) {
                Text("Reintentar")
              }
              Button(
                onClick = onExitGame,
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.buttonColors(containerColor = BloxEmeraldGreen)
              ) {
                Text("Volver al Menú", fontWeight = FontWeight.Bold)
              }
            }
          }
        }
      }
    }
  }
}
