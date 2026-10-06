package com.example.social

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.MeetingRoom
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.ChatMessageEntity
import com.example.data.local.FriendEntity
import com.example.data.local.PrivateRoomEntity
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
fun SocialScreen(
  currentUserId: String,
  currentUserName: String,
  friends: List<FriendEntity>,
  globalChat: List<ChatMessageEntity>,
  privateRooms: List<PrivateRoomEntity>,
  onSendMessage: (channel: String, text: String) -> Unit,
  onAddFriend: (username: String) -> Unit,
  onCreateRoom: (name: String, gameTitle: String) -> Unit,
  onJoinRoomGame: (gameTitle: String) -> Unit,
  onJoinFriendGame: (gameTitle: String) -> Unit,
  modifier: Modifier = Modifier
) {
  var selectedTab by remember { mutableIntStateOf(0) } // 0: Chat Global, 1: Amigos, 2: Salas Privadas
  var chatInputText by remember { mutableStateOf("") }
  var showAddFriendDialog by remember { mutableStateOf(false) }
  var friendUsernameInput by remember { mutableStateOf("") }
  var showCreateRoomDialog by remember { mutableStateOf(false) }
  var roomNameInput by remember { mutableStateOf("") }
  var selectedRoomGame by remember { mutableStateOf("Obby de Lava & Cristales 3D") }

  Column(
    modifier = modifier
      .fillMaxSize()
      .background(BloxDarkBackground)
      .padding(16.dp)
  ) {
    // Header
    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically
    ) {
      Column {
        Text("Comunidad & Social", color = TextPrimary, fontSize = 22.sp, fontWeight = FontWeight.Black)
        Text("Conoce jugadores, chatea y juega en salas privadas", color = TextSecondary, fontSize = 12.sp)
      }
    }

    Spacer(modifier = Modifier.height(14.dp))

    // Social Tabs
    TabRow(
      selectedTabIndex = selectedTab,
      containerColor = BloxDarkSurface,
      contentColor = BloxCyanPrimary
    ) {
      Tab(
        selected = selectedTab == 0,
        onClick = { selectedTab = 0 },
        text = { Text("Chat Global 💬", fontWeight = FontWeight.Bold, fontSize = 12.sp) }
      )
      Tab(
        selected = selectedTab == 1,
        onClick = { selectedTab = 1 },
        text = { Text("Amigos (${friends.size})", fontWeight = FontWeight.Bold, fontSize = 12.sp) }
      )
      Tab(
        selected = selectedTab == 2,
        onClick = { selectedTab = 2 },
        text = { Text("Salas Privadas 🔒", fontWeight = FontWeight.Bold, fontSize = 12.sp) }
      )
    }

    Spacer(modifier = Modifier.height(12.dp))

    when (selectedTab) {
      0 -> {
        // --- CHAT GLOBAL ---
        Column(modifier = Modifier.fillMaxSize()) {
          LazyColumn(
            modifier = Modifier
              .weight(1f)
              .fillMaxWidth(),
            reverseLayout = false,
            verticalArrangement = Arrangement.spacedBy(10.dp)
          ) {
            items(globalChat) { msg ->
              val isMe = msg.senderId == currentUserId
              Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = if (isMe) Arrangement.End else Arrangement.Start
              ) {
                Card(
                  shape = RoundedCornerShape(14.dp),
                  colors = CardDefaults.cardColors(
                    containerColor = if (isMe) BloxPurpleAccent.copy(alpha = 0.35f) else BloxDarkCard
                  ),
                  border = androidx.compose.foundation.BorderStroke(
                    1.dp,
                    if (isMe) BloxPurpleAccent else Color(0xFF28324A)
                  ),
                  modifier = Modifier.fillMaxWidth(0.85f)
                ) {
                  Column(modifier = Modifier.padding(10.dp)) {
                    Row(
                      horizontalArrangement = Arrangement.SpaceBetween,
                      modifier = Modifier.fillMaxWidth()
                    ) {
                      Text(
                        msg.senderName,
                        color = if (isMe) BloxCyanPrimary else BloxGoldCoins,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp
                      )
                      Text(
                        "En línea",
                        color = BloxEmeraldGreen,
                        fontSize = 10.sp
                      )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                      msg.message,
                      color = TextPrimary,
                      fontSize = 13.sp
                    )
                  }
                }
              }
            }
          }

          Spacer(modifier = Modifier.height(10.dp))

          // Send message input
          Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
          ) {
            OutlinedTextField(
              value = chatInputText,
              onValueChange = { chatInputText = it },
              placeholder = { Text("Escribe en el chat global...", color = TextSecondary, fontSize = 12.sp) },
              modifier = Modifier
                .weight(1f)
                .testTag("chat_input_field"),
              shape = RoundedCornerShape(14.dp),
              colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White,
                focusedBorderColor = BloxCyanPrimary,
                unfocusedBorderColor = Color(0xFF2C3550)
              ),
              singleLine = true
            )
            Spacer(modifier = Modifier.width(8.dp))
            IconButton(
              onClick = {
                if (chatInputText.isNotBlank()) {
                  onSendMessage("GLOBAL", chatInputText.trim())
                  chatInputText = ""
                }
              },
              modifier = Modifier
                .background(BloxCyanPrimary, RoundedCornerShape(12.dp))
                .size(48.dp)
                .testTag("send_chat_button")
            ) {
              Icon(Icons.Default.Send, contentDescription = "Enviar", tint = Color.Black)
            }
          }
        }
      }

      1 -> {
        // --- AMIGOS ---
        Column(modifier = Modifier.fillMaxSize()) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Text("Lista de Amigos Conectados", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 14.sp)
            Button(
              onClick = { showAddFriendDialog = true },
              colors = ButtonDefaults.buttonColors(containerColor = BloxCyanPrimary),
              shape = RoundedCornerShape(10.dp),
              contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 10.dp, vertical = 4.dp),
              modifier = Modifier.testTag("open_add_friend_dialog")
            ) {
              Icon(Icons.Default.PersonAdd, contentDescription = "Añadir", tint = Color.Black, modifier = Modifier.size(16.dp))
              Spacer(modifier = Modifier.width(4.dp))
              Text("Agregar Amigo", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 11.sp)
            }
          }

          Spacer(modifier = Modifier.height(12.dp))

          if (friends.isEmpty()) {
            Card(
              shape = RoundedCornerShape(16.dp),
              colors = CardDefaults.cardColors(containerColor = BloxDarkCard),
              modifier = Modifier.fillMaxWidth().padding(top = 20.dp)
            ) {
              Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
              ) {
                Icon(Icons.Default.People, contentDescription = null, tint = BloxCyanPrimary, modifier = Modifier.size(48.dp))
                Spacer(modifier = Modifier.height(8.dp))
                Text("No tienes amigos agregados aún", color = TextPrimary, fontWeight = FontWeight.Bold)
                Text("Agrega a otros jugadores por su usuario para unirte a sus juegos o invitarlos a salas privadas.", color = TextSecondary, fontSize = 12.sp, textAlign = androidx.compose.ui.text.style.TextAlign.Center)
              }
            }
          } else {
            LazyColumn(
              verticalArrangement = Arrangement.spacedBy(10.dp),
              modifier = Modifier.fillMaxSize()
            ) {
              items(friends) { friend ->
                FriendCard(
                  friend = friend,
                  onJoinGame = { onJoinFriendGame(friend.currentGame) }
                )
              }
            }
          }
        }
      }

      2 -> {
        // --- SALAS PRIVADAS ---
        Column(modifier = Modifier.fillMaxSize()) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Text("Salas Privadas para Jugar", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 14.sp)
            Button(
              onClick = { showCreateRoomDialog = true },
              colors = ButtonDefaults.buttonColors(containerColor = BloxPurpleAccent),
              shape = RoundedCornerShape(10.dp),
              modifier = Modifier.testTag("open_create_room_dialog")
            ) {
              Icon(Icons.Default.Add, contentDescription = "Crear Sala", modifier = Modifier.size(16.dp))
              Spacer(modifier = Modifier.width(4.dp))
              Text("Crear Sala Privada", fontWeight = FontWeight.Bold, fontSize = 11.sp)
            }
          }

          Spacer(modifier = Modifier.height(12.dp))

          if (privateRooms.isEmpty()) {
            Card(
              shape = RoundedCornerShape(16.dp),
              colors = CardDefaults.cardColors(containerColor = BloxDarkCard),
              modifier = Modifier.fillMaxWidth().padding(top = 20.dp)
            ) {
              Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
              ) {
                Icon(Icons.Default.MeetingRoom, contentDescription = null, tint = BloxPurpleAccent, modifier = Modifier.size(48.dp))
                Spacer(modifier = Modifier.height(8.dp))
                Text("No hay salas privadas activas", color = TextPrimary, fontWeight = FontWeight.Bold)
                Text("Crea una sala privada con código exclusivo para jugar únicamente con tus amigos.", color = TextSecondary, fontSize = 12.sp, textAlign = androidx.compose.ui.text.style.TextAlign.Center)
              }
            }
          } else {
            LazyColumn(
              verticalArrangement = Arrangement.spacedBy(10.dp),
              modifier = Modifier.fillMaxSize()
            ) {
              items(privateRooms) { room ->
                Card(
                  shape = RoundedCornerShape(14.dp),
                  colors = CardDefaults.cardColors(containerColor = BloxDarkCard),
                  border = androidx.compose.foundation.BorderStroke(1.dp, BloxPurpleAccent.copy(alpha = 0.5f)),
                  modifier = Modifier.fillMaxWidth()
                ) {
                  Row(
                    modifier = Modifier.padding(14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                  ) {
                    Column {
                      Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Lock, contentDescription = "Privada", tint = BloxGoldCoins, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(room.roomName, color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                      }
                      Text("Juego: ${room.gameTitle}", color = BloxCyanPrimary, fontSize = 12.sp)
                      Text("Código: ${room.roomCode} • Anfitrión: ${room.hostName}", color = TextSecondary, fontSize = 11.sp)
                    }

                    Button(
                      onClick = { onJoinRoomGame(room.gameTitle) },
                      colors = ButtonDefaults.buttonColors(containerColor = BloxEmeraldGreen),
                      shape = RoundedCornerShape(10.dp),
                      modifier = Modifier.testTag("join_room_${room.roomCode}")
                    ) {
                      Icon(Icons.Default.PlayArrow, contentDescription = "Unirse", modifier = Modifier.size(16.dp))
                      Spacer(modifier = Modifier.width(4.dp))
                      Text("Unirse", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }
                  }
                }
              }
            }
          }
        }
      }
    }
  }

  // Add Friend Dialog
  if (showAddFriendDialog) {
    AlertDialog(
      onDismissRequest = { showAddFriendDialog = false },
      title = { Text("Agregar Nuevo Amigo", color = TextPrimary, fontWeight = FontWeight.Black) },
      text = {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
          Text("Ingresa el nombre de usuario de tu amigo para agregarlo a tu lista:", color = TextSecondary, fontSize = 12.sp)
          OutlinedTextField(
            value = friendUsernameInput,
            onValueChange = { friendUsernameInput = it },
            label = { Text("Usuario del Jugador") },
            placeholder = { Text("Ej: AlexGamer99 o LunaCraft") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth().testTag("add_friend_input")
          )
        }
      },
      confirmButton = {
        Button(
          onClick = {
            if (friendUsernameInput.isNotBlank()) {
              onAddFriend(friendUsernameInput.trim())
              showAddFriendDialog = false
              friendUsernameInput = ""
            }
          },
          colors = ButtonDefaults.buttonColors(containerColor = BloxCyanPrimary),
          modifier = Modifier.testTag("confirm_add_friend_button")
        ) {
          Text("Agregar", color = Color.Black, fontWeight = FontWeight.Bold)
        }
      },
      dismissButton = {
        OutlinedButton(onClick = { showAddFriendDialog = false }) {
          Text("Cancelar")
        }
      },
      containerColor = BloxDarkSurface
    )
  }

  // Create Private Room Dialog
  if (showCreateRoomDialog) {
    AlertDialog(
      onDismissRequest = { showCreateRoomDialog = false },
      title = { Text("Crear Sala Privada 🔒", color = TextPrimary, fontWeight = FontWeight.Black) },
      text = {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
          Text("Elige un nombre y el mundo 3D que jugarán juntos:", color = TextSecondary, fontSize = 12.sp)
          OutlinedTextField(
            value = roomNameInput,
            onValueChange = { roomNameInput = it },
            label = { Text("Nombre de la Sala") },
            placeholder = { Text("Ej: Obby con Amigos VIP") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth().testTag("create_room_name_input")
          )

          Text("Seleccionar Mundo 3D:", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 12.sp)
          val games = listOf("Obby de Lava & Cristales 3D", "Blox City: Parque & Monedas", "Isla Flotante del Dragón")
          games.forEach { g ->
            Surface(
              shape = RoundedCornerShape(10.dp),
              color = if (selectedRoomGame == g) BloxCyanPrimary.copy(alpha = 0.2f) else BloxDarkCard,
              border = androidx.compose.foundation.BorderStroke(1.dp, if (selectedRoomGame == g) BloxCyanPrimary else Color(0xFF2A344A)),
              modifier = Modifier.fillMaxWidth().clickable { selectedRoomGame = g }
            ) {
              Text(
                g,
                color = if (selectedRoomGame == g) BloxCyanPrimary else TextSecondary,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(10.dp)
              )
            }
          }
        }
      },
      confirmButton = {
        Button(
          onClick = {
            val rName = if (roomNameInput.isNotBlank()) roomNameInput.trim() else "Sala de $currentUserName"
            onCreateRoom(rName, selectedRoomGame)
            showCreateRoomDialog = false
            roomNameInput = ""
          },
          colors = ButtonDefaults.buttonColors(containerColor = BloxPurpleAccent),
          modifier = Modifier.testTag("confirm_create_room_button")
        ) {
          Text("Crear Sala")
        }
      },
      dismissButton = {
        OutlinedButton(onClick = { showCreateRoomDialog = false }) {
          Text("Cancelar")
        }
      },
      containerColor = BloxDarkSurface
    )
  }
}

@Composable
fun FriendCard(
  friend: FriendEntity,
  onJoinGame: () -> Unit
) {
  Card(
    shape = RoundedCornerShape(14.dp),
    colors = CardDefaults.cardColors(containerColor = BloxDarkCard),
    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF28324A)),
    modifier = Modifier.fillMaxWidth()
  ) {
    Row(
      modifier = Modifier.padding(12.dp),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically
    ) {
      Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
          modifier = Modifier
            .size(40.dp)
            .clip(CircleShape)
            .background(Color(friend.avatarColorHex)),
          contentAlignment = Alignment.Center
        ) {
          Text(
            friend.friendDisplayName.take(1).uppercase(),
            color = Color.Black,
            fontWeight = FontWeight.Black,
            fontSize = 16.sp
          )
        }

        Spacer(modifier = Modifier.width(10.dp))

        Column {
          Text(friend.friendDisplayName, color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 14.sp)
          Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
              modifier = Modifier
                .size(8.dp)
                .clip(CircleShape)
                .background(BloxEmeraldGreen)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text("Jugando: ${friend.currentGame}", color = TextSecondary, fontSize = 11.sp)
          }
        }
      }

      Button(
        onClick = onJoinGame,
        colors = ButtonDefaults.buttonColors(containerColor = BloxCyanPrimary),
        shape = RoundedCornerShape(10.dp),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 10.dp, vertical = 4.dp),
        modifier = Modifier.testTag("join_friend_${friend.id}")
      ) {
        Text("Unirse 3D", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 11.sp)
      }
    }
  }
}
