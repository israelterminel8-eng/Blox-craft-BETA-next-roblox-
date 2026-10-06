package com.example.home

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.Gamepad
import androidx.compose.material.icons.filled.MonetizationOn
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.ThumbUp
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.PublishedGameEntity
import com.example.data.local.UserEntity
import com.example.ui.theme.BloxCyanPrimary
import com.example.ui.theme.BloxDarkBackground
import com.example.ui.theme.BloxDarkCard
import com.example.ui.theme.BloxDarkSurface
import com.example.ui.theme.BloxEmeraldGreen
import com.example.ui.theme.BloxGoldCoins
import com.example.ui.theme.BloxLavaRed
import com.example.ui.theme.BloxPurpleAccent
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@Composable
fun HomeScreen(
  currentUser: UserEntity,
  games: List<PublishedGameEntity>,
  playtimeSeconds: Long,
  onLaunchGame: (PublishedGameEntity) -> Unit,
  onLikeGame: (String) -> Unit,
  onOpenPlaytimeSheet: () -> Unit,
  onLogout: () -> Unit,
  modifier: Modifier = Modifier
) {
  LazyColumn(
    modifier = modifier
      .fillMaxSize()
      .background(BloxDarkBackground)
      .padding(horizontal = 16.dp),
    verticalArrangement = Arrangement.spacedBy(16.dp),
    contentPadding = PaddingValues(top = 16.dp, bottom = 24.dp)
  ) {
    // 1. Top Player Profile & Wallet Header
    item {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
          Box(
            modifier = Modifier
              .size(46.dp)
              .clip(CircleShape)
              .background(Color(currentUser.avatarTorsoColor))
              .border(2.dp, BloxCyanPrimary, CircleShape),
            contentAlignment = Alignment.Center
          ) {
            Text(
              currentUser.displayName.take(1).uppercase(),
              color = Color.Black,
              fontWeight = FontWeight.Black,
              fontSize = 20.sp
            )
          }

          Spacer(modifier = Modifier.width(10.dp))

          Column {
            Text(
              currentUser.displayName,
              color = TextPrimary,
              fontWeight = FontWeight.Black,
              fontSize = 17.sp
            )
            Text(
              "@${currentUser.username} • Nivel ${currentUser.level}",
              color = TextSecondary,
              fontSize = 11.sp
            )
          }
        }

        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
          // BloxCoins Badge
          Surface(
            shape = RoundedCornerShape(16.dp),
            color = BloxDarkSurface,
            border = androidx.compose.foundation.BorderStroke(1.dp, BloxGoldCoins),
            modifier = Modifier.clickable { onOpenPlaytimeSheet() }
          ) {
            Row(
              modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
              verticalAlignment = Alignment.CenterVertically
            ) {
              Icon(Icons.Default.MonetizationOn, contentDescription = "Monedas", tint = BloxGoldCoins, modifier = Modifier.size(18.dp))
              Spacer(modifier = Modifier.width(4.dp))
              Text("${currentUser.coins}", color = BloxGoldCoins, fontWeight = FontWeight.Black, fontSize = 14.sp)
            }
          }

          // Logout Button
          IconButton(
            onClick = onLogout,
            modifier = Modifier
              .background(BloxDarkSurface, CircleShape)
              .size(38.dp)
              .testTag("logout_button")
          ) {
            Icon(Icons.Default.ExitToApp, contentDescription = "Cerrar Sesión", tint = BloxLavaRed, modifier = Modifier.size(18.dp))
          }
        }
      }
    }

    // 2. Playtime Reward Live Banner
    item {
      Card(
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = BloxDarkSurface),
        border = androidx.compose.foundation.BorderStroke(1.dp, BloxGoldCoins.copy(alpha = 0.6f)),
        modifier = Modifier
          .fillMaxWidth()
          .clickable { onOpenPlaytimeSheet() }
          .testTag("playtime_banner")
      ) {
        Row(
          modifier = Modifier.padding(14.dp),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
              modifier = Modifier
                .size(42.dp)
                .clip(CircleShape)
                .background(BloxGoldCoins.copy(alpha = 0.2f)),
              contentAlignment = Alignment.Center
            ) {
              Icon(Icons.Default.Timer, contentDescription = null, tint = BloxGoldCoins, modifier = Modifier.size(24.dp))
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column {
              Text(
                "Ganando BloxCoins por Tiempo 🪙",
                color = BloxGoldCoins,
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp
              )
              val mins = playtimeSeconds / 60
              val secs = playtimeSeconds % 60
              Text(
                "Llevas ${mins}m ${secs}s jugando • Toca para reclamar",
                color = TextSecondary,
                fontSize = 11.sp
              )
            }
          }

          Surface(
            shape = RoundedCornerShape(8.dp),
            color = BloxGoldCoins
          ) {
            Text(
              "Ver Regalos",
              color = Color.Black,
              fontWeight = FontWeight.Black,
              fontSize = 10.sp,
              modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
            )
          }
        }
      }
    }

    // 3. Hero Featured 3D Game Banner
    item {
      games.firstOrNull()?.let { featured ->
        Card(
          shape = RoundedCornerShape(20.dp),
          colors = CardDefaults.cardColors(containerColor = Color(0xFF13192B)),
          border = androidx.compose.foundation.BorderStroke(1.dp, BloxCyanPrimary),
          modifier = Modifier
            .fillMaxWidth()
            .clickable { onLaunchGame(featured) }
            .testTag("featured_game_banner")
        ) {
          Column(modifier = Modifier.padding(16.dp)) {
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically
            ) {
              Surface(
                shape = RoundedCornerShape(8.dp),
                color = BloxCyanPrimary.copy(alpha = 0.2f)
              ) {
                Text(
                  "DESTACADO 3D",
                  color = BloxCyanPrimary,
                  fontWeight = FontWeight.Black,
                  fontSize = 10.sp,
                  modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                )
              }
              Text(
                "Por ${featured.creatorName}",
                color = TextSecondary,
                fontSize = 11.sp
              )
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
              featured.title,
              color = TextPrimary,
              fontWeight = FontWeight.Black,
              fontSize = 18.sp
            )

            Text(
              featured.description,
              color = TextSecondary,
              fontSize = 12.sp,
              maxLines = 2
            )

            Spacer(modifier = Modifier.height(12.dp))

            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically
            ) {
              Row(horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                  Icon(Icons.Default.Visibility, contentDescription = null, tint = BloxCyanPrimary, modifier = Modifier.size(15.dp))
                  Spacer(modifier = Modifier.width(4.dp))
                  Text("${featured.visits}", color = TextPrimary, fontSize = 12.sp)
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                  Icon(Icons.Default.ThumbUp, contentDescription = null, tint = BloxEmeraldGreen, modifier = Modifier.size(15.dp))
                  Spacer(modifier = Modifier.width(4.dp))
                  Text("${featured.likes}", color = TextPrimary, fontSize = 12.sp)
                }
              }

              Button(
                onClick = { onLaunchGame(featured) },
                colors = ButtonDefaults.buttonColors(containerColor = BloxEmeraldGreen),
                shape = RoundedCornerShape(12.dp)
              ) {
                Icon(Icons.Default.PlayArrow, contentDescription = "Jugar", modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("JUGAR EN 3D", fontWeight = FontWeight.Black, fontSize = 12.sp)
              }
            }
          }
        }
      }
    }

    // 4. Section: Explorar Mundos 3D de la Comunidad
    item {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
          Icon(Icons.Default.TrendingUp, contentDescription = null, tint = BloxPurpleAccent, modifier = Modifier.size(20.dp))
          Spacer(modifier = Modifier.width(6.dp))
          Text(
            "Mundos & Juegos 3D",
            color = TextPrimary,
            fontSize = 17.sp,
            fontWeight = FontWeight.Black
          )
        }
        Text("${games.size} juegos", color = TextSecondary, fontSize = 12.sp)
      }
    }

    // List of Games
    items(games) { game ->
      GameListItemCard(
        game = game,
        onPlay = { onLaunchGame(game) },
        onLike = { onLikeGame(game.id) }
      )
    }
  }
}

@Composable
fun GameListItemCard(
  game: PublishedGameEntity,
  onPlay: () -> Unit,
  onLike: () -> Unit
) {
  Card(
    shape = RoundedCornerShape(16.dp),
    colors = CardDefaults.cardColors(containerColor = BloxDarkCard),
    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF28324A)),
    modifier = Modifier
      .fillMaxWidth()
      .testTag("game_card_${game.id}")
  ) {
    Column(modifier = Modifier.padding(14.dp)) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Column(modifier = Modifier.weight(1f)) {
          Text(
            game.title,
            color = TextPrimary,
            fontWeight = FontWeight.Bold,
            fontSize = 15.sp,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
          )
          Text(
            "Creador: ${game.creatorName} • ${game.genre}",
            color = BloxPurpleAccent,
            fontSize = 11.sp
          )
        }

        Surface(
          shape = RoundedCornerShape(8.dp),
          color = Color(0xFF1E2840)
        ) {
          Text(
            "Luau 3D",
            color = BloxCyanPrimary,
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
          )
        }
      }

      Spacer(modifier = Modifier.height(6.dp))

      Text(
        game.description,
        color = TextSecondary,
        fontSize = 12.sp,
        maxLines = 2
      )

      Spacer(modifier = Modifier.height(10.dp))

      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.Visibility, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(14.dp))
            Spacer(modifier = Modifier.width(3.dp))
            Text("${game.visits}", color = TextSecondary, fontSize = 11.sp)
          }

          Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.clickable { onLike() }
          ) {
            Icon(Icons.Default.ThumbUp, contentDescription = "Me gusta", tint = BloxEmeraldGreen, modifier = Modifier.size(14.dp))
            Spacer(modifier = Modifier.width(3.dp))
            Text("${game.likes}", color = TextSecondary, fontSize = 11.sp)
          }
        }

        Button(
          onClick = onPlay,
          colors = ButtonDefaults.buttonColors(containerColor = BloxCyanPrimary),
          shape = RoundedCornerShape(10.dp),
          contentPadding = PaddingValues(horizontal = 14.dp, vertical = 4.dp),
          modifier = Modifier.testTag("play_game_button_${game.id}")
        ) {
          Icon(Icons.Default.PlayArrow, contentDescription = "Jugar", tint = Color.Black, modifier = Modifier.size(16.dp))
          Spacer(modifier = Modifier.width(4.dp))
          Text("Jugar", color = Color.Black, fontWeight = FontWeight.Black, fontSize = 12.sp)
        }
      }
    }
  }
}
