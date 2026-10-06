package com.example.game3d

import androidx.compose.ui.graphics.Color
import com.example.data.local.UserEntity
import kotlin.math.cos
import kotlin.math.sin

data class AvatarCustomization(
  val skinColor: Color = Color(0xFFFFDBAC),
  val torsoColor: Color = Color(0xFF00E5FF),
  val legsColor: Color = Color(0xFF1E293B),
  val hatId: String? = "CROWN",
  val wingsId: String? = "CYBER_WINGS",
  val gearId: String? = "FIRE_SWORD",
  val faceId: String? = "VR_VISOR",
  val petId: String? = "ROBO_CAT"
) {
  companion object {
    fun fromUser(user: UserEntity?): AvatarCustomization {
      if (user == null) return AvatarCustomization()
      return AvatarCustomization(
        skinColor = Color(user.avatarSkinColor),
        torsoColor = Color(user.avatarTorsoColor),
        legsColor = Color(user.avatarLegsColor),
        hatId = user.equippedHatId,
        wingsId = user.equippedWingsId,
        gearId = user.equippedGearId,
        faceId = user.equippedFaceId,
        petId = user.equippedPetId
      )
    }
  }
}

class Character3D(
  var position: Vec3 = Vec3(0f, 1f, 0f),
  var yaw: Float = 0f,
  var isMoving: Boolean = false,
  var isJumping: Boolean = false,
  var animTime: Float = 0f,
  var name: String = "Jugador",
  var config: AvatarCustomization = AvatarCustomization()
) {
  // Generate all 3D mesh boxes representing the blocky character
  fun generateMeshBoxes(): List<Box3D> {
    val boxes = mutableListOf<Box3D>()

    // Limb swing rotation
    val swing = if (isMoving) sin(animTime * 10f) * 0.45f else 0f
    val cosYaw = cos(yaw)
    val sinYaw = sin(yaw)

    fun localToWorld(offsetX: Float, offsetY: Float, offsetZ: Float): Vec3 {
      val rx = offsetX * cosYaw - offsetZ * sinYaw
      val rz = offsetX * sinYaw + offsetZ * cosYaw
      return Vec3(position.x + rx, position.y + offsetY, position.z + rz)
    }

    // 1. Torso (Center body)
    val torsoPos = localToWorld(0f, 1.4f, 0f)
    boxes.add(
      Box3D(
        center = torsoPos,
        size = Vec3(1.1f, 1.3f, 0.6f),
        rotationYaw = yaw,
        baseColor = config.torsoColor
      )
    )

    // 2. Head
    val headPos = localToWorld(0f, 2.4f, 0f)
    boxes.add(
      Box3D(
        center = headPos,
        size = Vec3(0.75f, 0.75f, 0.75f),
        rotationYaw = yaw,
        baseColor = config.skinColor
      )
    )

    // 3. Face / Visor (if equipped)
    val face = config.faceId
    if (!face.isNullOrBlank()) {
      val facePos = localToWorld(0f, 2.45f, 0.4f)
      val visorColor = if (face.contains("VR")) Color(0xFF00E5FF) else Color(0xFF10B981)
      boxes.add(
        Box3D(
          center = facePos,
          size = Vec3(0.65f, 0.28f, 0.12f),
          rotationYaw = yaw,
          baseColor = visorColor
        )
      )
    }

    // 4. Hat / Crown (if equipped)
    val hat = config.hatId
    if (!hat.isNullOrBlank()) {
      val hatPos = localToWorld(0f, 2.85f, 0f)
      val hatColor = when {
        hat.contains("CROWN") -> Color(0xFFFFC400)
        hat.contains("SPACE") -> Color(0xFF6366F1)
        else -> Color(0xFFEC4899)
      }
      boxes.add(
        Box3D(
          center = hatPos,
          size = Vec3(0.85f, 0.25f, 0.85f),
          rotationYaw = yaw,
          baseColor = hatColor
        )
      )
      // Crown spikes or hat crest
      val spikePos = localToWorld(0f, 3.02f, 0f)
      boxes.add(
        Box3D(
          center = spikePos,
          size = Vec3(0.45f, 0.18f, 0.45f),
          rotationYaw = yaw,
          baseColor = Color(0xFFFF9100)
        )
      )
    }

    // 5. Wings (if equipped)
    val wings = config.wingsId
    if (!wings.isNullOrBlank()) {
      val leftWingPos = localToWorld(-0.85f, 1.6f, -0.4f)
      val rightWingPos = localToWorld(0.85f, 1.6f, -0.4f)
      val wingColor = Color(0xFF00E5FF)
      boxes.add(
        Box3D(
          center = leftWingPos,
          size = Vec3(0.9f, 1.1f, 0.1f),
          rotationYaw = yaw - 0.35f,
          baseColor = wingColor
        )
      )
      boxes.add(
        Box3D(
          center = rightWingPos,
          size = Vec3(0.9f, 1.1f, 0.1f),
          rotationYaw = yaw + 0.35f,
          baseColor = wingColor
        )
      )
    }

    // 6. Left Arm
    val leftArmPos = localToWorld(-0.78f, 1.4f, -swing * 0.4f)
    boxes.add(
      Box3D(
        center = leftArmPos,
        size = Vec3(0.45f, 1.2f, 0.45f),
        rotationYaw = yaw,
        baseColor = config.skinColor
      )
    )

    // 7. Right Arm (Holding Gear/Sword)
    val rightArmPos = localToWorld(0.78f, 1.4f, swing * 0.4f)
    boxes.add(
      Box3D(
        center = rightArmPos,
        size = Vec3(0.45f, 1.2f, 0.45f),
        rotationYaw = yaw,
        baseColor = config.skinColor
      )
    )

    // 8. Gear / Sword in Right Hand (if equipped)
    val gear = config.gearId
    if (!gear.isNullOrBlank()) {
      val swordPos = localToWorld(0.82f, 1.6f, 0.5f + swing * 0.4f)
      val swordBladeColor = when {
        gear.contains("FIRE") -> Color(0xFFEF4444)
        gear.contains("LASER") -> Color(0xFF00E5FF)
        else -> Color(0xFFFFC400)
      }
      boxes.add(
        Box3D(
          center = swordPos,
          size = Vec3(0.15f, 1.4f, 0.15f),
          rotationYaw = yaw,
          baseColor = swordBladeColor
        )
      )
    }

    // 9. Left Leg
    val leftLegPos = localToWorld(-0.28f, 0.65f, swing * 0.5f)
    boxes.add(
      Box3D(
        center = leftLegPos,
        size = Vec3(0.48f, 1.25f, 0.48f),
        rotationYaw = yaw,
        baseColor = config.legsColor
      )
    )

    // 10. Right Leg
    val rightLegPos = localToWorld(0.28f, 0.65f, -swing * 0.5f)
    boxes.add(
      Box3D(
        center = rightLegPos,
        size = Vec3(0.48f, 1.25f, 0.48f),
        rotationYaw = yaw,
        baseColor = config.legsColor
      )
    )

    // 11. Pet (Companion hovering beside avatar)
    if (!config.petId.isNullOrBlank()) {
      val petFloat = sin(animTime * 4f) * 0.2f
      val petPos = localToWorld(1.3f, 1.8f + petFloat, -0.6f)
      boxes.add(
        Box3D(
          center = petPos,
          size = Vec3(0.42f, 0.42f, 0.42f),
          rotationYaw = yaw + (animTime * 1.5f),
          baseColor = Color(0xFF10B981)
        )
      )
    }

    return boxes
  }
}
