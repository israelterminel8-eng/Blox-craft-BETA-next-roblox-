package com.example.shop

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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Checkroom
import androidx.compose.material.icons.filled.MonetizationOn
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
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
import com.example.data.local.CatalogAccessoryEntity
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
fun ShopScreen(
  userCoins: Long,
  accessories: List<CatalogAccessoryEntity>,
  ownedItemIds: Set<String>,
  onBuyItem: (CatalogAccessoryEntity) -> Unit,
  onOpenPlaytimeRewards: () -> Unit,
  modifier: Modifier = Modifier
) {
  var selectedCategory by remember { mutableStateOf("ALL") }
  var itemToBuy by remember { mutableStateOf<CatalogAccessoryEntity?>(null) }
  var buyError by remember { mutableStateOf<String?>(null) }

  val categories = listOf(
    "ALL" to "Todos",
    "HAT" to "Sombreros",
    "WINGS" to "Alas",
    "GEAR" to "Armas",
    "PET" to "Mascotas",
    "FACE" to "Visores"
  )

  val filteredAccessories = accessories.filter {
    selectedCategory == "ALL" || it.category == selectedCategory
  }

  Column(
    modifier = modifier
      .fillMaxSize()
      .background(BloxDarkBackground)
      .padding(16.dp)
  ) {
    // Top Bar with Wallet & Playtime Reward Shortcut
    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically
    ) {
      Column {
        Text("Tienda & Catálogo", color = TextPrimary, fontSize = 22.sp, fontWeight = FontWeight.Black)
        Text("Consigue accesorios para tu avatar", color = TextSecondary, fontSize = 12.sp)
      }

      Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
        // Playtime tip
        Surface(
          shape = RoundedCornerShape(14.dp),
          color = BloxPurpleAccent.copy(alpha = 0.2f),
          border = androidx.compose.foundation.BorderStroke(1.dp, BloxPurpleAccent),
          modifier = Modifier.clickable { onOpenPlaytimeRewards() }
        ) {
          Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
          ) {
            Icon(Icons.Default.Timer, contentDescription = "Tiempo", tint = BloxCyanPrimary, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(4.dp))
            Text("Ganar Monedas", color = BloxCyanPrimary, fontSize = 11.sp, fontWeight = FontWeight.Bold)
          }
        }

        // Live Coin Balance
        Surface(
          shape = RoundedCornerShape(16.dp),
          color = BloxDarkSurface,
          border = androidx.compose.foundation.BorderStroke(1.dp, BloxGoldCoins)
        ) {
          Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
          ) {
            Icon(Icons.Default.MonetizationOn, contentDescription = "BloxCoins", tint = BloxGoldCoins, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(4.dp))
            Text("$userCoins", color = BloxGoldCoins, fontWeight = FontWeight.Black, fontSize = 14.sp)
          }
        }
      }
    }

    Spacer(modifier = Modifier.height(14.dp))

    // Category Filter Chips
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
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)
          )
        }
      }
    }

    Spacer(modifier = Modifier.height(12.dp))

    // Items Grid
    LazyVerticalGrid(
      columns = GridCells.Fixed(2),
      horizontalArrangement = Arrangement.spacedBy(12.dp),
      verticalArrangement = Arrangement.spacedBy(12.dp),
      contentPadding = PaddingValues(bottom = 16.dp),
      modifier = Modifier.fillMaxSize()
    ) {
      items(filteredAccessories) { item ->
        val isOwned = ownedItemIds.contains(item.id)
        CatalogItemCard(
          item = item,
          isOwned = isOwned,
          onClick = {
            if (!isOwned) {
              itemToBuy = item
              buyError = null
            }
          }
        )
      }
    }
  }

  // Buy Modal Dialog
  itemToBuy?.let { item ->
    AlertDialog(
      onDismissRequest = { itemToBuy = null },
      title = {
        Text("Comprar Accesorio 3D", color = TextPrimary, fontWeight = FontWeight.Black)
      },
      text = {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
          Text("¿Deseas adquirir '${item.name}' por ${item.price} BloxCoins?", color = TextSecondary, fontSize = 13.sp)
          Text("Creador: ${item.creatorName}", color = BloxPurpleAccent, fontSize = 12.sp, fontWeight = FontWeight.Bold)
          Row(verticalAlignment = Alignment.CenterVertically) {
            Text("Tu balance: $userCoins 🪙", color = BloxGoldCoins, fontWeight = FontWeight.Bold)
          }

          if (userCoins < item.price) {
            Text(
              "¡Te faltan ${item.price - userCoins} BloxCoins! Puedes conseguirlas jugando por tiempo en cualquier juego.",
              color = Color(0xFFEF4444),
              fontSize = 12.sp
            )
          }
        }
      },
      confirmButton = {
        Button(
          onClick = {
            if (userCoins >= item.price) {
              onBuyItem(item)
              itemToBuy = null
            } else {
              buyError = "Monedas insuficientes"
            }
          },
          enabled = userCoins >= item.price,
          colors = ButtonDefaults.buttonColors(containerColor = BloxEmeraldGreen),
          modifier = Modifier.testTag("confirm_buy_button")
        ) {
          Text("Comprar por ${item.price} 🪙", fontWeight = FontWeight.Bold)
        }
      },
      dismissButton = {
        OutlinedButton(onClick = { itemToBuy = null }) {
          Text("Cancelar")
        }
      },
      containerColor = BloxDarkSurface
    )
  }
}

@Composable
fun CatalogItemCard(
  item: CatalogAccessoryEntity,
  isOwned: Boolean,
  onClick: () -> Unit
) {
  Card(
    shape = RoundedCornerShape(16.dp),
    colors = CardDefaults.cardColors(containerColor = BloxDarkCard),
    border = androidx.compose.foundation.BorderStroke(
      1.dp,
      if (isOwned) BloxEmeraldGreen.copy(alpha = 0.5f) else Color(0xFF28324A)
    ),
    modifier = Modifier
      .fillMaxWidth()
      .clickable { onClick() }
      .testTag("catalog_card_${item.id}")
  ) {
    Column(modifier = Modifier.padding(12.dp)) {
      // Color Preview Thumbnail
      Box(
        modifier = Modifier
          .fillMaxWidth()
          .height(90.dp)
          .clip(RoundedCornerShape(12.dp))
          .background(Color(0xFF0F1424)),
        contentAlignment = Alignment.Center
      ) {
        // Visual indicator of accessory shape & color
        Box(
          modifier = Modifier
            .size(46.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(Color(item.primaryColorHex))
            .border(2.dp, Color(item.accentColorHex), RoundedCornerShape(8.dp))
        )

        // Rarity Tag
        Surface(
          shape = RoundedCornerShape(6.dp),
          color = when (item.rarity) {
            "LEGENDARIO" -> BloxGoldCoins.copy(alpha = 0.85f)
            "EPICO" -> BloxPurpleAccent.copy(alpha = 0.85f)
            else -> BloxCyanPrimary.copy(alpha = 0.85f)
          },
          modifier = Modifier
            .align(Alignment.TopEnd)
            .padding(6.dp)
        ) {
          Text(
            item.rarity,
            color = Color.Black,
            fontSize = 9.sp,
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
        text = "Por ${item.creatorName}",
        color = TextSecondary,
        fontSize = 11.sp,
        maxLines = 1
      )

      Spacer(modifier = Modifier.height(8.dp))

      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        if (isOwned) {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.Check, contentDescription = "Adquirido", tint = BloxEmeraldGreen, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(2.dp))
            Text("En Propiedad", color = BloxEmeraldGreen, fontSize = 11.sp, fontWeight = FontWeight.Bold)
          }
        } else {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.MonetizationOn, contentDescription = "Precio", tint = BloxGoldCoins, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(3.dp))
            Text("${item.price}", color = BloxGoldCoins, fontWeight = FontWeight.Black, fontSize = 13.sp)
          }
          Text("${item.salesCount} ventas", color = Color.Gray, fontSize = 10.sp)
        }
      }
    }
  }
}
