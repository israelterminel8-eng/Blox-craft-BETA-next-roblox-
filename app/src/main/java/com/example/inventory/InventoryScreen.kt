package com.example.inventory

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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Diamond
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.InventoryItemEntity
import com.example.ui.theme.BloxCyanPrimary
import com.example.ui.theme.BloxDarkBackground
import com.example.ui.theme.BloxDarkCard
import com.example.ui.theme.BloxDarkSurface
import com.example.ui.theme.BloxEmeraldGreen
import com.example.ui.theme.BloxGoldCoins
import com.example.ui.theme.BloxPurpleAccent
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun InventoryScreen(
  items: List<InventoryItemEntity>,
  onEquipItem: (InventoryItemEntity) -> Unit,
  modifier: Modifier = Modifier
) {
  var selectedCategory by remember { mutableStateOf("ALL") }

  val categories = listOf(
    "ALL" to "Todos (${items.size})",
    "WORLD_COLLECTIBLE" to "Reliquias de Juegos 3D",
    "HAT" to "Sombreros",
    "GEAR" to "Armas",
    "WINGS" to "Alas",
    "PET" to "Mascotas"
  )

  val filteredItems = items.filter {
    selectedCategory == "ALL" || it.category == selectedCategory
  }

  Column(
    modifier = modifier
      .fillMaxSize()
      .background(BloxDarkBackground)
      .padding(16.dp)
  ) {
    // Inventory Header
    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically
    ) {
      Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
          Icons.Default.Inventory2,
          contentDescription = "Inventario",
          tint = BloxCyanPrimary,
          modifier = Modifier.size(28.dp)
        )
        Spacer(modifier = Modifier.width(10.dp))
        Column {
          Text("Inventario del Jugador", color = TextPrimary, fontSize = 20.sp, fontWeight = FontWeight.Black)
          Text("Objetos y reliquias conseguidas en tus partidas", color = TextSecondary, fontSize = 11.sp)
        }
      }

      Surface(
        shape = RoundedCornerShape(12.dp),
        color = BloxDarkSurface,
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF28324A))
      ) {
        Text(
          "${items.size} Objetos",
          color = BloxGoldCoins,
          fontWeight = FontWeight.Bold,
          fontSize = 12.sp,
          modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
        )
      }
    }

    Spacer(modifier = Modifier.height(14.dp))

    // Filter Chips
    LazyRow(
      horizontalArrangement = Arrangement.spacedBy(8.dp),
      modifier = Modifier.fillMaxWidth()
    ) {
      items(categories) { (catId, catLabel) ->
        val isSelected = selectedCategory == catId
        Surface(
          shape = RoundedCornerShape(12.dp),
          color = if (isSelected) BloxCyanPrimary.copy(alpha = 0.2f) else BloxDarkCard,
          border = androidx.compose.foundation.BorderStroke(1.dp, if (isSelected) BloxCyanPrimary else Color(0xFF28324A)),
          modifier = Modifier.clickable { selectedCategory = catId }
        ) {
          Text(
            catLabel,
            color = if (isSelected) BloxCyanPrimary else TextSecondary,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
            fontSize = 12.sp,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
          )
        }
      }
    }

    Spacer(modifier = Modifier.height(14.dp))

    if (filteredItems.isEmpty()) {
      Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = BloxDarkCard),
        modifier = Modifier.fillMaxWidth().padding(top = 24.dp)
      ) {
        Column(
          modifier = Modifier.padding(32.dp),
          horizontalAlignment = Alignment.CenterHorizontally
        ) {
          Icon(Icons.Default.Diamond, contentDescription = null, tint = BloxPurpleAccent, modifier = Modifier.size(54.dp))
          Spacer(modifier = Modifier.height(10.dp))
          Text("No hay objetos en esta categoría", color = TextPrimary, fontWeight = FontWeight.Bold)
          Spacer(modifier = Modifier.height(4.dp))
          Text(
            "¡Entra a los juegos 3D para encontrar gemas ocultas, trofeos y reliquias en los circuitos!",
            color = TextSecondary,
            fontSize = 12.sp,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
          )
        }
      }
    } else {
      LazyVerticalGrid(
        columns = GridCells.Fixed(2),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        contentPadding = PaddingValues(bottom = 16.dp),
        modifier = Modifier.fillMaxSize()
      ) {
        items(filteredItems) { item ->
          InventoryItemCard(
            item = item,
            onEquip = { onEquipItem(item) }
          )
        }
      }
    }
  }
}

@Composable
fun InventoryItemCard(
  item: InventoryItemEntity,
  onEquip: () -> Unit
) {
  val dateStr = remember(item.obtainedTimestamp) {
    val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
    sdf.format(Date(item.obtainedTimestamp))
  }

  Card(
    shape = RoundedCornerShape(16.dp),
    colors = CardDefaults.cardColors(containerColor = BloxDarkCard),
    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF28324A)),
    modifier = Modifier.fillMaxWidth().testTag("inventory_card_${item.id}")
  ) {
    Column(modifier = Modifier.padding(12.dp)) {
      // Item Thumbnail with Rarity Border
      Box(
        modifier = Modifier
          .fillMaxWidth()
          .height(80.dp)
          .clip(RoundedCornerShape(12.dp))
          .background(Color(0xFF0F1424)),
        contentAlignment = Alignment.Center
      ) {
        Box(
          modifier = Modifier
            .size(42.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(Color(item.colorHex))
            .border(2.dp, Color.White.copy(alpha = 0.4f), RoundedCornerShape(8.dp)),
          contentAlignment = Alignment.Center
        ) {
          Icon(
            when (item.category) {
              "WORLD_COLLECTIBLE" -> Icons.Default.Diamond
              "TROPHY" -> Icons.Default.EmojiEvents
              "GEAR" -> Icons.Default.Shield
              else -> Icons.Default.AutoAwesome
            },
            contentDescription = null,
            tint = Color.White,
            modifier = Modifier.size(24.dp)
          )
        }

        // Rarity Badge
        Surface(
          shape = RoundedCornerShape(6.dp),
          color = when (item.rarity) {
            "LEGENDARIO" -> BloxGoldCoins
            "EPICO" -> BloxPurpleAccent
            else -> BloxCyanPrimary
          },
          modifier = Modifier
            .align(Alignment.TopEnd)
            .padding(6.dp)
        ) {
          Text(
            item.rarity,
            color = Color.Black,
            fontSize = 8.sp,
            fontWeight = FontWeight.Black,
            modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
          )
        }
      }

      Spacer(modifier = Modifier.height(10.dp))

      Text(
        text = item.name,
        color = TextPrimary,
        fontWeight = FontWeight.Bold,
        fontSize = 13.sp,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis
      )

      Text(
        text = "Lugar: ${item.sourceGame}",
        color = BloxCyanPrimary,
        fontSize = 10.sp,
        maxLines = 1
      )

      Text(
        text = "Obtenido: $dateStr",
        color = TextSecondary,
        fontSize = 9.sp
      )

      Spacer(modifier = Modifier.height(8.dp))

      Button(
        onClick = onEquip,
        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF28344C)),
        shape = RoundedCornerShape(10.dp),
        contentPadding = PaddingValues(vertical = 4.dp),
        modifier = Modifier.fillMaxWidth().testTag("equip_item_${item.id}")
      ) {
        Icon(Icons.Default.Check, contentDescription = "Equipar", modifier = Modifier.size(14.dp))
        Spacer(modifier = Modifier.width(4.dp))
        Text("Equipar", fontSize = 11.sp, fontWeight = FontWeight.Bold)
      }
    }
  }
}
