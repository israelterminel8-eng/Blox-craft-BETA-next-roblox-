package com.example.rewards

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CardGiftcard
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.MonetizationOn
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.BloxCyanPrimary
import com.example.ui.theme.BloxDarkCard
import com.example.ui.theme.BloxDarkSurface
import com.example.ui.theme.BloxEmeraldGreen
import com.example.ui.theme.BloxGoldCoins
import com.example.ui.theme.BloxPurpleAccent
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

data class PlaytimeMilestone(
  val secondsRequired: Long,
  val label: String,
  val coinReward: Long,
  val isClaimed: Boolean
)

@Composable
fun PlaytimeRewardSheet(
  totalPlaytimeSeconds: Long,
  currentIntervalSeconds: Long,
  onClaimMilestone: (PlaytimeMilestone) -> Unit,
  onDismiss: () -> Unit
) {
  val minutes = totalPlaytimeSeconds / 60
  val seconds = totalPlaytimeSeconds % 60
  val nextRewardIn = 60 - (currentIntervalSeconds % 60)
  val progress = (currentIntervalSeconds % 60) / 60f

  val milestones = listOf(
    PlaytimeMilestone(60L, "1 Minuto Activo", 25L, totalPlaytimeSeconds >= 60L),
    PlaytimeMilestone(300L, "5 Minutos Jugando", 60L, totalPlaytimeSeconds >= 300L),
    PlaytimeMilestone(900L, "15 Minutos Jugando", 150L, totalPlaytimeSeconds >= 900L),
    PlaytimeMilestone(1800L, "30 Minutos Jugando", 300L, totalPlaytimeSeconds >= 1800L)
  )

  Card(
    shape = RoundedCornerShape(24.dp),
    colors = CardDefaults.cardColors(containerColor = BloxDarkSurface),
    border = androidx.compose.foundation.BorderStroke(1.dp, BloxGoldCoins.copy(alpha = 0.5f)),
    modifier = Modifier
      .fillMaxWidth()
      .padding(16.dp)
  ) {
    Column(
      modifier = Modifier.padding(20.dp),
      horizontalAlignment = Alignment.CenterHorizontally
    ) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
          Icon(Icons.Default.Timer, contentDescription = null, tint = BloxGoldCoins, modifier = Modifier.size(24.dp))
          Spacer(modifier = Modifier.width(8.dp))
          Text("Recompensas por Tiempo", color = TextPrimary, fontWeight = FontWeight.Black, fontSize = 16.sp)
        }

        Surface(
          shape = RoundedCornerShape(12.dp),
          color = BloxGoldCoins.copy(alpha = 0.2f)
        ) {
          Text(
            "+15 🪙 cada 60s",
            color = BloxGoldCoins,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
          )
        }
      }

      Spacer(modifier = Modifier.height(16.dp))

      // Current Time Banner
      Surface(
        shape = RoundedCornerShape(16.dp),
        color = BloxDarkCard,
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF28324A)),
        modifier = Modifier.fillMaxWidth()
      ) {
        Column(modifier = Modifier.padding(14.dp)) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
          ) {
            Text("Tiempo Total Jugado:", color = TextSecondary, fontSize = 12.sp)
            Text(
              "${minutes}m ${seconds}s",
              color = BloxCyanPrimary,
              fontWeight = FontWeight.Bold,
              fontSize = 13.sp
            )
          }

          Spacer(modifier = Modifier.height(8.dp))

          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
          ) {
            Text("Próximo depósito de +15 BloxCoins:", color = TextSecondary, fontSize = 11.sp)
            Text("en ${nextRewardIn}s", color = BloxGoldCoins, fontWeight = FontWeight.Bold, fontSize = 11.sp)
          }

          Spacer(modifier = Modifier.height(6.dp))

          LinearProgressIndicator(
            progress = { progress },
            modifier = Modifier
              .fillMaxWidth()
              .height(6.dp)
              .clip(RoundedCornerShape(3.dp)),
            color = BloxGoldCoins,
            trackColor = Color(0xFF1E2840)
          )
        }
      }

      Spacer(modifier = Modifier.height(16.dp))

      Text(
        "Cajas de Bonificación por Permanencia:",
        color = TextPrimary,
        fontWeight = FontWeight.Bold,
        fontSize = 13.sp,
        modifier = Modifier.align(Alignment.Start)
      )

      Spacer(modifier = Modifier.height(10.dp))

      // Milestone List
      milestones.forEach { m ->
        val canClaim = totalPlaytimeSeconds >= m.secondsRequired
        Card(
          shape = RoundedCornerShape(12.dp),
          colors = CardDefaults.cardColors(containerColor = BloxDarkCard),
          modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
        ) {
          Row(
            modifier = Modifier.padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
              Box(
                modifier = Modifier
                  .size(34.dp)
                  .clip(CircleShape)
                  .background(if (canClaim) BloxEmeraldGreen.copy(alpha = 0.2f) else Color(0xFF1E2538)),
                contentAlignment = Alignment.Center
              ) {
                Icon(
                  Icons.Default.CardGiftcard,
                  contentDescription = null,
                  tint = if (canClaim) BloxEmeraldGreen else Color.Gray,
                  modifier = Modifier.size(18.dp)
                )
              }
              Spacer(modifier = Modifier.width(10.dp))
              Column {
                Text(m.label, color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                Text("+${m.coinReward} BloxCoins 🪙", color = BloxGoldCoins, fontSize = 11.sp)
              }
            }

            if (canClaim) {
              Surface(
                shape = RoundedCornerShape(8.dp),
                color = BloxEmeraldGreen.copy(alpha = 0.2f)
              ) {
                Row(
                  modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                  verticalAlignment = Alignment.CenterVertically
                ) {
                  Icon(Icons.Default.Check, contentDescription = null, tint = BloxEmeraldGreen, modifier = Modifier.size(14.dp))
                  Spacer(modifier = Modifier.width(2.dp))
                  Text("Desbloqueado", color = BloxEmeraldGreen, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                }
              }
            } else {
              val remaining = m.secondsRequired - totalPlaytimeSeconds
              Text("Faltan ${remaining / 60}m", color = Color.Gray, fontSize = 10.sp)
            }
          }
        }
      }

      Spacer(modifier = Modifier.height(14.dp))

      Button(
        onClick = onDismiss,
        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2C3550)),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.fillMaxWidth().testTag("close_rewards_button")
      ) {
        Text("Cerrar")
      }
    }
  }
}
