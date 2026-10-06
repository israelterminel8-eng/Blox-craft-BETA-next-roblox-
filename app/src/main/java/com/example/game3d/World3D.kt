package com.example.game3d

import androidx.compose.ui.graphics.Color
import kotlin.math.cos
import kotlin.math.max
import kotlin.math.sin

class World3D(
  val title: String,
  val type: String,
  val blocks: MutableList<Box3D> = mutableListOf(),
  val spawnPoint: Vec3 = Vec3(0f, 1f, 0f),
  var skyColor: Color = Color(0xFF0F172A),
  var groundColor: Color = Color(0xFF1E293B)
) {
  var playerVelocityY: Float = 0f
  var isGrounded: Boolean = false
  var checkpoint: Vec3 = spawnPoint

  val otherPlayers = mutableListOf<Character3D>()

  companion object {
    fun createObbyLava(): World3D {
      val world = World3D(
        title = "Obby de Lava & Cristales 3D",
        type = "OBBY",
        spawnPoint = Vec3(0f, 1.2f, 0f),
        skyColor = Color(0xFF1A0B2E),
        groundColor = Color(0xFF2A0808)
      )

      // 1. Lava Lake base (huge deadly surface below)
      world.blocks.add(
        Box3D(
          center = Vec3(0f, -2f, 25f),
          size = Vec3(50f, 1f, 80f),
          baseColor = Color(0xFFDC2626),
          isLava = true
        )
      )

      // 2. Start Spawn Platform (Safe zone)
      world.blocks.add(
        Box3D(
          center = Vec3(0f, 0f, 0f),
          size = Vec3(6f, 1f, 6f),
          baseColor = Color(0xFF10B981) // Emerald green
        )
      )

      // 3. Stepping Platforms
      val platformData = listOf(
        Vec3(0f, 0.5f, 6f),
        Vec3(2.5f, 1.2f, 11f),
        Vec3(-2.5f, 1.8f, 16f),
        Vec3(0f, 2.5f, 21f)
      )
      for ((idx, pos) in platformData.withIndex()) {
        world.blocks.add(
          Box3D(
            center = pos,
            size = Vec3(2.8f, 0.6f, 2.8f),
            baseColor = if (idx % 2 == 0) Color(0xFF00E5FF) else Color(0xFFA855F7)
          )
        )
        // Add floating gold coin
        world.blocks.add(
          Box3D(
            center = Vec3(pos.x, pos.y + 1.2f, pos.z),
            size = Vec3(0.5f, 0.5f, 0.5f),
            baseColor = Color(0xFFFFC400),
            isCoin = true
          )
        )
      }

      // 4. Trampoline Pad (bounces player high!)
      world.blocks.add(
        Box3D(
          center = Vec3(0f, 3f, 27f),
          size = Vec3(3.5f, 0.6f, 3.5f),
          baseColor = Color(0xFFEC4899),
          isTrampoline = true
        )
      )

      // 5. Midpoint Checkpoint Island with RARE COLLECTIBLE ITEM!
      world.blocks.add(
        Box3D(
          center = Vec3(0f, 7.5f, 35f),
          size = Vec3(7f, 1f, 7f),
          baseColor = Color(0xFF3B82F6)
        )
      )
      // Rare in-game collectible: Gema del Vacío
      world.blocks.add(
        Box3D(
          center = Vec3(0f, 8.8f, 35f),
          size = Vec3(0.7f, 0.9f, 0.7f),
          baseColor = Color(0xFF8B5CF6),
          isRareItem = true,
          rareItemName = "Gema del Vacío Mística",
          rareItemType = "WORLD_COLLECTIBLE",
          rareItemRarity = "EPICO"
        )
      )

      // 6. High Sky Pillars
      val highPlatforms = listOf(
        Vec3(-3f, 8.5f, 43f),
        Vec3(3f, 9.5f, 49f),
        Vec3(0f, 11f, 56f)
      )
      for (pos in highPlatforms) {
        world.blocks.add(
          Box3D(
            center = pos,
            size = Vec3(2.4f, 0.6f, 2.4f),
            baseColor = Color(0xFF00E5FF)
          )
        )
        world.blocks.add(
          Box3D(
            center = Vec3(pos.x, pos.y + 1.2f, pos.z),
            size = Vec3(0.5f, 0.5f, 0.5f),
            baseColor = Color(0xFFFFC400),
            isCoin = true
          )
        )
      }

      // 7. Victory Goal Platform & Portal
      world.blocks.add(
        Box3D(
          center = Vec3(0f, 12f, 65f),
          size = Vec3(8f, 1f, 8f),
          baseColor = Color(0xFFFFD700),
          isGoal = true
        )
      )
      // Second rare item at victory summit: Cáliz de Cristal
      world.blocks.add(
        Box3D(
          center = Vec3(0f, 13.5f, 65f),
          size = Vec3(0.8f, 1.2f, 0.8f),
          baseColor = Color(0xFFFBBF24),
          isRareItem = true,
          rareItemName = "Cáliz Dorado de la Victoria",
          rareItemType = "TROPHY",
          rareItemRarity = "LEGENDARIO"
        )
      )

      // Add a friendly friend avatar also playing this Obby!
      world.otherPlayers.add(
        Character3D(
          position = Vec3(0f, 7.5f, 36.5f),
          name = "AlexGamer99",
          config = AvatarCustomization(
            torsoColor = Color(0xFFFF9100),
            legsColor = Color(0xFF0F172A),
            hatId = "SPACE_HELMET"
          )
        )
      )

      return world
    }

    fun createBloxCity(): World3D {
      val world = World3D(
        title = "Blox City: Parque & Monedas",
        type = "CIUDAD",
        spawnPoint = Vec3(0f, 1f, 0f),
        skyColor = Color(0xFF0C192C),
        groundColor = Color(0xFF1E293B)
      )

      // Main City Ground
      world.blocks.add(
        Box3D(
          center = Vec3(0f, 0f, 0f),
          size = Vec3(60f, 0.8f, 60f),
          baseColor = Color(0xFF334155) // Asphalt grey
        )
      )

      // Central Park Green Zone
      world.blocks.add(
        Box3D(
          center = Vec3(0f, 0.2f, 0f),
          size = Vec3(18f, 0.6f, 18f),
          baseColor = Color(0xFF059669) // Park grass
        )
      )

      // Park Trampoline
      world.blocks.add(
        Box3D(
          center = Vec3(0f, 0.7f, 0f),
          size = Vec3(4f, 0.5f, 4f),
          baseColor = Color(0xFFEC4899),
          isTrampoline = true
        )
      )

      // High Skyscraper 1
      world.blocks.add(
        Box3D(
          center = Vec3(-15f, 7f, -12f),
          size = Vec3(8f, 14f, 8f),
          baseColor = Color(0xFF1E3A8A)
        )
      )
      // Skyscraper 2
      world.blocks.add(
        Box3D(
          center = Vec3(15f, 9f, -10f),
          size = Vec3(9f, 18f, 9f),
          baseColor = Color(0xFF312E81)
        )
      )
      // Skyscraper 3
      world.blocks.add(
        Box3D(
          center = Vec3(-14f, 6f, 15f),
          size = Vec3(8f, 12f, 8f),
          baseColor = Color(0xFF065F46)
        )
      )

      // Rooftop Rare Item on Skyscraper 1: Botas Antigravedad
      world.blocks.add(
        Box3D(
          center = Vec3(-15f, 14.8f, -12f),
          size = Vec3(0.8f, 0.8f, 0.8f),
          baseColor = Color(0xFF00E5FF),
          isRareItem = true,
          rareItemName = "Botas Neón Antigravedad",
          rareItemType = "GEAR",
          rareItemRarity = "EPICO"
        )
      )

      // Gold Coins scattered in the park and on rooftops
      val coinPositions = listOf(
        Vec3(4f, 1.2f, 4f),
        Vec3(-4f, 1.2f, 4f),
        Vec3(4f, 1.2f, -4f),
        Vec3(-4f, 1.2f, -4f),
        Vec3(0f, 8f, 0f), // in air above trampoline
        Vec3(15f, 18.8f, -10f), // top of tower 2
        Vec3(-14f, 12.8f, 15f) // top of tower 3
      )
      for (cPos in coinPositions) {
        world.blocks.add(
          Box3D(
            center = cPos,
            size = Vec3(0.6f, 0.6f, 0.6f),
            baseColor = Color(0xFFFFC400),
            isCoin = true
          )
        )
      }

      // Friendly bot avatar walking in the park
      world.otherPlayers.add(
        Character3D(
          position = Vec3(5f, 1.2f, 2f),
          name = "LunaCraft",
          config = AvatarCustomization(
            torsoColor = Color(0xFFEC4899),
            legsColor = Color(0xFF1E293B),
            hatId = "CROWN"
          )
        )
      )

      return world
    }

    fun createDragonIsland(): World3D {
      val world = World3D(
        title = "Isla Flotante del Dragón",
        type = "AVENTURA",
        spawnPoint = Vec3(0f, 1f, 0f),
        skyColor = Color(0xFF1E1035),
        groundColor = Color(0xFF2E1065)
      )

      // Main floating island
      world.blocks.add(
        Box3D(
          center = Vec3(0f, 0f, 0f),
          size = Vec3(14f, 1.5f, 14f),
          baseColor = Color(0xFF4C1D95)
        )
      )

      // Bridge
      world.blocks.add(
        Box3D(
          center = Vec3(0f, 0.5f, 12f),
          size = Vec3(2.5f, 0.5f, 10f),
          baseColor = Color(0xFF7C3AED)
        )
      )

      // Dragon Island
      world.blocks.add(
        Box3D(
          center = Vec3(0f, 2f, 26f),
          size = Vec3(18f, 2f, 18f),
          baseColor = Color(0xFF581C87)
        )
      )

      // Legendary Relic: Espada del Sol
      world.blocks.add(
        Box3D(
          center = Vec3(0f, 3.8f, 26f),
          size = Vec3(0.8f, 1.6f, 0.8f),
          baseColor = Color(0xFFEF4444),
          isRareItem = true,
          rareItemName = "Espada del Sol Místico",
          rareItemType = "GEAR",
          rareItemRarity = "LEGENDARIO"
        )
      )

      // Golden Coins
      for (z in 2..8 step 2) {
        world.blocks.add(
          Box3D(
            center = Vec3(0f, 1.5f, z.toFloat()),
            size = Vec3(0.5f, 0.5f, 0.5f),
            baseColor = Color(0xFFFFC400),
            isCoin = true
          )
        )
      }

      return world
    }

    fun createFromLuau(title: String, luauCode: String): World3D {
      val world = World3D(
        title = title,
        type = "COMMUNITY",
        spawnPoint = Vec3(0f, 1.2f, 0f),
        skyColor = Color(0xFF0F172A),
        groundColor = Color(0xFF1E293B)
      )

      // Base platform
      world.blocks.add(
        Box3D(
          center = Vec3(0f, 0f, 0f),
          size = Vec3(8f, 1f, 8f),
          baseColor = Color(0xFF00E5FF)
        )
      )

      // Parse simple properties from the Luau script!
      val count = if (luauCode.contains("for i = 1,")) {
        val match = Regex("""for i\s*=\s*1\s*,\s*(\d+)""").find(luauCode)
        match?.groupValues?.get(1)?.toIntOrNull()?.coerceIn(3, 16) ?: 8
      } else 8

      val hasTrampoline = luauCode.contains("Trampoline") || luauCode.contains("Bounce")
      val hasLava = luauCode.contains("Lava") || luauCode.contains("Kill")

      if (hasLava) {
        world.blocks.add(
          Box3D(
            center = Vec3(0f, -2f, 20f),
            size = Vec3(40f, 1f, 60f),
            baseColor = Color(0xFFEF4444),
            isLava = true
          )
        )
      }

      for (i in 1..count) {
        val y = i * 1.5f
        val z = i * 4.2f
        val x = sin(i.toFloat()) * 3f
        world.blocks.add(
          Box3D(
            center = Vec3(x, y, z),
            size = Vec3(3f, 0.6f, 3f),
            baseColor = if (i % 2 == 0) Color(0xFFA855F7) else Color(0xFF00E5FF)
          )
        )
        // Add coin
        world.blocks.add(
          Box3D(
            center = Vec3(x, y + 1.2f, z),
            size = Vec3(0.5f, 0.5f, 0.5f),
            baseColor = Color(0xFFFFC400),
            isCoin = true
          )
        )
      }

      if (hasTrampoline) {
        world.blocks.add(
          Box3D(
            center = Vec3(0f, (count * 1.5f) + 1.5f, (count * 4.2f) + 5f),
            size = Vec3(4f, 0.6f, 4f),
            baseColor = Color(0xFFEC4899),
            isTrampoline = true
          )
        )
      }

      // Goal
      val goalZ = (count * 4.2f) + 12f
      val goalY = (count * 1.5f) + 3f
      world.blocks.add(
        Box3D(
          center = Vec3(0f, goalY, goalZ),
          size = Vec3(7f, 1f, 7f),
          baseColor = Color(0xFFFFD700),
          isGoal = true
        )
      )

      return world
    }
  }

  /**
   * Physics update tick.
   * Handles 3D collisions, gravity, jump, coins, trampolines, lava, and collectibles.
   */
  fun updatePhysics(
    player: Character3D,
    inputMoveX: Float,
    inputMoveZ: Float,
    cameraYaw: Float,
    dt: Float,
    onCoinCollected: (Int) -> Unit,
    onRareItemCollected: (String, String, String) -> Unit,
    onLavaHit: () -> Unit,
    onGoalReached: () -> Unit
  ) {
    val speed = 9.0f

    // Transform input vector by camera horizontal angle
    val cosCam = cos(cameraYaw)
    val sinCam = sin(cameraYaw)
    val worldDx = (inputMoveX * cosCam + inputMoveZ * sinCam) * speed * dt
    val worldDz = (-inputMoveX * sinCam + inputMoveZ * cosCam) * speed * dt

    // Apply movement
    player.position = Vec3(
      player.position.x + worldDx,
      player.position.y,
      player.position.z + worldDz
    )

    if (inputMoveX != 0f || inputMoveZ != 0f) {
      player.isMoving = true
      // Face towards movement direction
      val targetYaw = kotlin.math.atan2(-worldDx, -worldDz)
      player.yaw = targetYaw
      player.animTime += dt
    } else {
      player.isMoving = false
    }

    // Apply gravity
    val gravity = -24.0f
    playerVelocityY += gravity * dt
    val nextY = player.position.y + playerVelocityY * dt
    player.position = Vec3(player.position.x, nextY, player.position.z)

    // Player bounding box (center around waist)
    val playerRadius = 0.5f
    val playerHeight = 2.4f
    val playerFeetY = player.position.y
    val playerHeadY = player.position.y + playerHeight

    isGrounded = false

    // Check collisions against world blocks
    for (block in blocks) {
      if (block.isCollected) continue

      val bMin = block.boundingBox.min
      val bMax = block.boundingBox.max

      // Check horizontal overlap
      val overlapX = (player.position.x + playerRadius >= bMin.x) && (player.position.x - playerRadius <= bMax.x)
      val overlapZ = (player.position.z + playerRadius >= bMin.z) && (player.position.z - playerRadius <= bMax.z)

      if (overlapX && overlapZ) {
        // Collectible Coin
        if (block.isCoin) {
          if (playerFeetY <= bMax.y && playerHeadY >= bMin.y) {
            block.isCollected = true
            onCoinCollected(10)
          }
          continue
        }

        // Rare Item Collectible!
        if (block.isRareItem) {
          if (playerFeetY <= bMax.y && playerHeadY >= bMin.y) {
            block.isCollected = true
            onRareItemCollected(block.rareItemName, block.rareItemType, block.rareItemRarity)
          }
          continue
        }

        // Lava (Deadly hazard)
        if (block.isLava) {
          if (playerFeetY <= bMax.y + 0.3f && playerHeadY >= bMin.y) {
            // Respawn at checkpoint
            player.position = checkpoint
            playerVelocityY = 0f
            onLavaHit()
            return
          }
          continue
        }

        // Trampoline (Bounce pad)
        if (block.isTrampoline) {
          if (playerFeetY <= bMax.y + 0.4f && playerFeetY >= bMax.y - 0.8f && playerVelocityY <= 0f) {
            player.position = Vec3(player.position.x, bMax.y, player.position.z)
            playerVelocityY = 22.0f // High bounce boost!
            isGrounded = true
            continue
          }
        }

        // Solid Platform surface
        if (playerFeetY <= bMax.y + 0.35f && playerFeetY >= bMax.y - 0.7f && playerVelocityY <= 0f) {
          player.position = Vec3(player.position.x, bMax.y, player.position.z)
          playerVelocityY = 0f
          isGrounded = true

          // Checkpoint update on high platforms
          if (block.center.y > checkpoint.y && !block.isGoal) {
            checkpoint = Vec3(block.center.x, block.center.y + 1f, block.center.z)
          }

          if (block.isGoal) {
            onGoalReached()
          }
        }
      }
    }

    // Fall below world safeguard
    if (player.position.y < -15f) {
      player.position = checkpoint
      playerVelocityY = 0f
      onLavaHit()
    }
  }

  fun jump() {
    if (isGrounded) {
      playerVelocityY = 11.5f
      isGrounded = false
    }
  }
}
