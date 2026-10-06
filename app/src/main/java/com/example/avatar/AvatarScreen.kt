package com.example.avatar

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Brush
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Checkroom
import androidx.compose.material.icons.filled.ColorLens
import androidx.compose.material.icons.filled.Construction
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Pets
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush as GBrush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.InventoryItemEntity
import com.example.data.local.UserEntity
import com.example.game3d.AvatarCustomization
import com.example.game3d.Camera3D
import com.example.game3d.Character3D
import com.example.game3d.PolygonFace
import com.example.game3d.Vec3
import com.example.game3d.renderPolygonFaces
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
fun AvatarScreen(
  currentUser: UserEntity?,
  inventory: List<InventoryItemEntity>,
  onSaveAvatarConfig: (skin: Long, torso: Long, legs: Long, hat: String?, wings: String?, gear: String?, shirt: String?, face: String?, pet: String?) -> Unit,
  onCreateCustomAccessory: (name: String, category: String, price: Int, rarity: String, primaryColor: Long, accentColor: Long, styleId: String, publishToCatalog: Boolean) -> Unit,
  modifier: Modifier = Modifier
) {
  var selectedTab by remember { mutableIntStateOf(0) } // 0: Personalizar 3D, 1: Diseñar Accesorio

  // Current Avatar State
  var skinColor by remember(currentUser) { mutableStateOf(Color(currentUser?.avatarSkinColor ?: 0xFFFFDBAC)) }
  var torsoColor by remember(currentUser) { mutableStateOf(Color(currentUser?.avatarTorsoColor ?: 0xFF00E5FF)) }
  var legsColor by remember(currentUser) { mutableStateOf(Color(currentUser?.avatarLegsColor ?: 0xFF1E293B)) }

  var equippedHat by remember(currentUser) { mutableStateOf(currentUser?.equippedHatId) }
  var equippedWings by remember(currentUser) { mutableStateOf(currentUser?.equippedWingsId) }
  var equippedGear by remember(currentUser) { mutableStateOf(currentUser?.equippedGearId) }
  var equippedFace by remember(currentUser) { mutableStateOf(currentUser?.equippedFaceId) }
  var equippedPet by remember(currentUser) { mutableStateOf(currentUser?.equippedPetId) }

  // 3D Preview Camera & Character
  var avatarYaw by remember { mutableFloatStateOf(0f) }
  var isAutoRotating by remember { mutableStateOf(true) }

  val previewCharacter = remember {
    Character3D(position = Vec3(0f, -0.6f, 0f), name = currentUser?.displayName ?: "Avatar")
  }
  val previewCamera = remember {
    Camera3D(
      target = Vec3(0f, 0.7f, 0f),
      yaw = 0f,
      pitch = 0.12f,
      distance = 5.2f,
      fovScale = 1.35f
    )
  }

  // Update preview character config
  previewCharacter.config = AvatarCustomization(
    skinColor = skinColor,
    torsoColor = torsoColor,
    legsColor = legsColor,
    hatId = equippedHat,
    wingsId = equippedWings,
    gearId = equippedGear,
    faceId = equippedFace,
    petId = equippedPet
  )
  previewCharacter.yaw = avatarYaw

  // Auto-rotation loop
  LaunchedEffect(isAutoRotating) {
    var lastNano = System.nanoTime()
    while (isAutoRotating) {
      withFrameNanos { now ->
        val dt = ((now - lastNano) / 1_000_000_000f).coerceIn(0.001f, 0.05f)
        lastNano = now
        avatarYaw += dt * 0.85f
        previewCharacter.yaw = avatarYaw
        previewCharacter.animTime += dt
      }
    }
  }

  Column(
    modifier = modifier
      .fillMaxSize()
      .background(BloxDarkBackground)
  ) {
    // 3D Avatar Preview Viewport Card (Top 38% of screen)
    Card(
      shape = RoundedCornerShape(bottomStart = 24.dp, bottomEnd = 24.dp),
      colors = CardDefaults.cardColors(containerColor = Color(0xFF0F1424)),
      border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF232D47)),
      modifier = Modifier
        .fillMaxWidth()
        .height(280.dp)
    ) {
      BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val w = maxWidth.value
        val h = maxHeight.value

        // Touch Drag to rotate avatar in 360 degrees
        Box(
          modifier = Modifier
            .fillMaxSize()
            .pointerInput(Unit) {
              detectDragGestures(
                onDragStart = { isAutoRotating = false },
                onDragEnd = { isAutoRotating = true },
                onDrag = { change, dragAmount ->
                  change.consume()
                  avatarYaw -= dragAmount.x * 0.015f
                  previewCharacter.yaw = avatarYaw
                }
              )
            }
        )

        // 3D Rendering Canvas of Avatar
        Canvas(modifier = Modifier.fillMaxSize()) {
          drawRect(
            brush = GBrush.radialGradient(
              listOf(Color(0xFF1E284A), Color(0xFF0A0D17)),
              radius = size.minDimension * 0.8f
            )
          )

          // Floor circular shadow/pedestal
          val pedestalBoxes = previewCharacter.generateMeshBoxes()
          val allFaces = mutableListOf<PolygonFace>()
          for (box in pedestalBoxes) {
            allFaces.addAll(box.generateFaces(previewCamera, size.width, size.height))
          }
          allFaces.sortByDescending { it.avgDepth }
          renderPolygonFaces(allFaces)
        }

        // 360 Indicator & Controls Overlay
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .padding(14.dp)
            .align(Alignment.TopCenter),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Surface(
            shape = RoundedCornerShape(12.dp),
            color = Color.Black.copy(alpha = 0.6f)
          ) {
            Text(
              "Arrastra para girar 360° 🔄",
              color = BloxCyanPrimary,
              fontSize = 11.sp,
              fontWeight = FontWeight.Bold,
              modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
            )
          }

          Button(
            onClick = {
              onSaveAvatarConfig(
                skinColor.toArgb().toLong(),
                torsoColor.toArgb().toLong(),
                legsColor.toArgb().toLong(),
                equippedHat,
                equippedWings,
                equippedGear,
                "shirt_default",
                equippedFace,
                equippedPet
              )
            },
            colors = ButtonDefaults.buttonColors(containerColor = BloxEmeraldGreen),
            shape = RoundedCornerShape(10.dp),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 12.dp, vertical = 4.dp),
            modifier = Modifier.testTag("save_avatar_button")
          ) {
            Icon(Icons.Default.Save, contentDescription = "Guardar", modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(4.dp))
            Text("Guardar", fontSize = 12.sp, fontWeight = FontWeight.Bold)
          }
        }
      }
    }

    // Tabs: Personalizar vs Diseñar Accesorio
    TabRow(
      selectedTabIndex = selectedTab,
      containerColor = BloxDarkSurface,
      contentColor = BloxCyanPrimary
    ) {
      Tab(
        selected = selectedTab == 0,
        onClick = { selectedTab = 0 },
        text = { Text("Personalizar Avatar", fontWeight = FontWeight.Bold) }
      )
      Tab(
        selected = selectedTab == 1,
        onClick = { selectedTab = 1 },
        text = { Text("Diseñar Accesorio ✨", fontWeight = FontWeight.Bold) }
      )
    }

    // Tab Contents
    if (selectedTab == 0) {
      // --- TAB 0: CUSTOMIZER ---
      Column(
        modifier = Modifier
          .fillMaxSize()
          .verticalScroll(rememberScrollState())
          .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
      ) {
        // 1. Color de Piel
        ColorSelectorSection(
          title = "Color de Piel",
          colors = listOf(
            Color(0xFFFFDBAC), // Clásica
            Color(0xFFF1C27D), // Morena clara
            Color(0xFFC68642), // Bronceada
            Color(0xFF8D5524), // Oscura
            Color(0xFF00E5FF), // Cyber Cyan
            Color(0xFF10B981)  // Alien Green
          ),
          selectedColor = skinColor,
          onSelect = { skinColor = it }
        )

        // 2. Color de Torso / Camiseta
        ColorSelectorSection(
          title = "Color de Torso (Camiseta)",
          colors = listOf(
            Color(0xFF00E5FF),
            Color(0xFFEF4444),
            Color(0xFFA855F7),
            Color(0xFFFFC400),
            Color(0xFF10B981),
            Color(0xFF1E293B)
          ),
          selectedColor = torsoColor,
          onSelect = { torsoColor = it }
        )

        // 3. Color de Piernas / Pantalones
        ColorSelectorSection(
          title = "Color de Piernas (Pantalones)",
          colors = listOf(
            Color(0xFF1E293B),
            Color(0xFF0F172A),
            Color(0xFF3B82F6),
            Color(0xFFDC2626),
            Color(0xFFE2E8F0),
            Color(0xFF059669)
          ),
          selectedColor = legsColor,
          onSelect = { legsColor = it }
        )

        // 4. Equipar Sombreros del Inventario
        AccessoryEquipSection(
          title = "Sombreros & Cascos",
          currentEquipped = equippedHat,
          options = listOf(
            Pair("Ninguno", null),
            Pair("Corona de Oro", "CROWN"),
            Pair("Casco Astral", "SPACE_HELMET"),
            Pair("Gorra Neón", "HAT_CAP")
          ),
          onSelect = { equippedHat = it }
        )

        // 5. Equipar Alas / Mochila
        AccessoryEquipSection(
          title = "Alas & Mochila",
          currentEquipped = equippedWings,
          options = listOf(
            Pair("Ninguna", null),
            Pair("Alas Neón Cyber", "CYBER_WINGS")
          ),
          onSelect = { equippedWings = it }
        )

        // 6. Equipar Armas & Espadas
        AccessoryEquipSection(
          title = "Arma / Objeto de Mano",
          currentEquipped = equippedGear,
          options = listOf(
            Pair("Ninguna", null),
            Pair("Espada Neón", "FIRE_SWORD"),
            Pair("Espada Láser", "LASER_SWORD")
          ),
          onSelect = { equippedGear = it }
        )

        // 7. Mascota Compañera
        AccessoryEquipSection(
          title = "Mascota Compañera 3D",
          currentEquipped = equippedPet,
          options = listOf(
            Pair("Sin Mascota", null),
            Pair("Robo-Gatito 3D", "ROBO_CAT")
          ),
          onSelect = { equippedPet = it }
        )
      }
    } else {
      // --- TAB 1: ACCESSORY STUDIO CREATOR ---
      AccessoryStudioCreator(
        onCreate = { name, cat, price, rarity, pColor, aColor, style, publish ->
          onCreateCustomAccessory(name, cat, price, rarity, pColor, aColor, style, publish)
          // Also preview newly created style
          when (cat) {
            "HAT" -> equippedHat = style
            "WINGS" -> equippedWings = style
            "GEAR" -> equippedGear = style
            "PET" -> equippedPet = style
          }
          selectedTab = 0
        }
      )
    }
  }
}

@Composable
fun ColorSelectorSection(
  title: String,
  colors: List<Color>,
  selectedColor: Color,
  onSelect: (Color) -> Unit
) {
  Column {
    Text(title, color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 13.sp)
    Spacer(modifier = Modifier.height(8.dp))
    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
      colors.forEach { color ->
        val isSelected = color == selectedColor
        Box(
          modifier = Modifier
            .size(42.dp)
            .clip(CircleShape)
            .background(color)
            .border(
              width = if (isSelected) 3.dp else 1.dp,
              color = if (isSelected) Color.White else Color.Black.copy(alpha = 0.4f),
              shape = CircleShape
            )
            .clickable { onSelect(color) },
          contentAlignment = Alignment.Center
        ) {
          if (isSelected) {
            Icon(
              Icons.Default.Check,
              contentDescription = "Seleccionado",
              tint = if (color == Color(0xFFFFDBAC) || color == Color(0xFFFFC400) || color == Color(0xFFE2E8F0)) Color.Black else Color.White,
              modifier = Modifier.size(20.dp)
            )
          }
        }
      }
    }
  }
}

@Composable
fun AccessoryEquipSection(
  title: String,
  currentEquipped: String?,
  options: List<Pair<String, String?>>,
  onSelect: (String?) -> Unit
) {
  Column {
    Text(title, color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 13.sp)
    Spacer(modifier = Modifier.height(6.dp))
    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
      items(options) { (label, id) ->
        val isSelected = currentEquipped == id
        Surface(
          shape = RoundedCornerShape(12.dp),
          color = if (isSelected) BloxPurpleAccent.copy(alpha = 0.25f) else BloxDarkCard,
          border = androidx.compose.foundation.BorderStroke(
            1.dp,
            if (isSelected) BloxPurpleAccent else Color(0xFF28324A)
          ),
          modifier = Modifier.clickable { onSelect(id) }
        ) {
          Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
          ) {
            Text(
              label,
              color = if (isSelected) BloxCyanPrimary else TextSecondary,
              fontSize = 12.sp,
              fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
            )
          }
        }
      }
    }
  }
}

@Composable
fun AccessoryStudioCreator(
  onCreate: (name: String, category: String, price: Int, rarity: String, pColor: Long, aColor: Long, style: String, publish: Boolean) -> Unit
) {
  var name by remember { mutableStateOf("") }
  var category by remember { mutableStateOf("HAT") }
  var price by remember { mutableIntStateOf(75) }
  var rarity by remember { mutableStateOf("EPICO") }
  var primaryColor by remember { mutableStateOf(Color(0xFF00E5FF)) }
  var accentColor by remember { mutableStateOf(Color(0xFFFFC400)) }
  var publishToCatalog by remember { mutableStateOf(true) }

  Column(
    modifier = Modifier
      .fillMaxSize()
      .verticalScroll(rememberScrollState())
      .padding(16.dp),
    verticalArrangement = Arrangement.spacedBy(14.dp)
  ) {
    Card(
      shape = RoundedCornerShape(14.dp),
      colors = CardDefaults.cardColors(containerColor = BloxDarkSurface),
      border = androidx.compose.foundation.BorderStroke(1.dp, BloxPurpleAccent)
    ) {
      Row(
        modifier = Modifier.padding(14.dp),
        verticalAlignment = Alignment.CenterVertically
      ) {
        Icon(Icons.Default.AutoAwesome, contentDescription = "Crear", tint = BloxPurpleAccent, modifier = Modifier.size(28.dp))
        Spacer(modifier = Modifier.width(10.dp))
        Column {
          Text("Taller Creador de Accesorios 3D", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 14.sp)
          Text("Diseña tus propios accesorios, úsalos en tu avatar y publícalos en la tienda para ganar BloxCoins con cada venta.", color = TextSecondary, fontSize = 11.sp)
        }
      }
    }

    OutlinedTextField(
      value = name,
      onValueChange = { name = it },
      label = { Text("Nombre del Accesorio") },
      placeholder = { Text("Ej: Corona de Fuego Cósmico") },
      singleLine = true,
      modifier = Modifier.fillMaxWidth().testTag("custom_acc_name_input")
    )

    // Category Selector
    Text("Tipo de Accesorio:", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 13.sp)
    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
      val cats = listOf("HAT" to "Sombrero / Corona", "WINGS" to "Alas Neón", "GEAR" to "Espada / Arma", "PET" to "Mascota 3D")
      items(cats) { (catKey, catLabel) ->
        val isSelected = category == catKey
        Surface(
          shape = RoundedCornerShape(10.dp),
          color = if (isSelected) BloxCyanPrimary.copy(alpha = 0.2f) else BloxDarkCard,
          border = androidx.compose.foundation.BorderStroke(1.dp, if (isSelected) BloxCyanPrimary else Color(0xFF2A344A)),
          modifier = Modifier.clickable { category = catKey }
        ) {
          Text(
            catLabel,
            color = if (isSelected) BloxCyanPrimary else TextSecondary,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
          )
        }
      }
    }

    // Color Selector
    ColorSelectorSection(
      title = "Color Principal",
      colors = listOf(Color(0xFF00E5FF), Color(0xFFFFC400), Color(0xFFEF4444), Color(0xFFA855F7), Color(0xFF10B981)),
      selectedColor = primaryColor,
      onSelect = { primaryColor = it }
    )

    // Price in BloxCoins
    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically
    ) {
      Text("Precio de Venta en Catálogo:", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 13.sp)
      Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        listOf(50, 75, 120, 200).forEach { p ->
          Surface(
            shape = RoundedCornerShape(8.dp),
            color = if (price == p) BloxGoldCoins.copy(alpha = 0.25f) else BloxDarkCard,
            border = androidx.compose.foundation.BorderStroke(1.dp, if (price == p) BloxGoldCoins else Color(0xFF2C3550)),
            modifier = Modifier.clickable { price = p }
          ) {
            Text(
              "$p 🪙",
              color = if (price == p) BloxGoldCoins else TextSecondary,
              fontSize = 12.sp,
              fontWeight = FontWeight.Bold,
              modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp)
            )
          }
        }
      }
    }

    Spacer(modifier = Modifier.height(10.dp))

    Button(
      onClick = {
        val finalName = if (name.isNotBlank()) name.trim() else "Accesorio Creado #1"
        val style = when (category) {
          "HAT" -> "CROWN"
          "WINGS" -> "CYBER_WINGS"
          "GEAR" -> "FIRE_SWORD"
          "PET" -> "ROBO_CAT"
          else -> "CROWN"
        }
        onCreate(
          finalName,
          category,
          price,
          rarity,
          primaryColor.toArgb().toLong(),
          accentColor.toArgb().toLong(),
          style,
          publishToCatalog
        )
      },
      colors = ButtonDefaults.buttonColors(containerColor = BloxPurpleAccent),
      shape = RoundedCornerShape(12.dp),
      modifier = Modifier.fillMaxWidth().testTag("create_accessory_submit_button")
    ) {
      Icon(Icons.Default.AutoAwesome, contentDescription = "Crear")
      Spacer(modifier = Modifier.width(8.dp))
      Text("Crear & Publicar Accesorio", fontWeight = FontWeight.Bold)
    }
  }
}
