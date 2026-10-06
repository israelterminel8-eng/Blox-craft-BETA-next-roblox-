package com.example.game3d

import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

data class Vec3(val x: Float, val y: Float, val z: Float) {
  operator fun plus(other: Vec3) = Vec3(x + other.x, y + other.y, z + other.z)
  operator fun minus(other: Vec3) = Vec3(x - other.x, y - other.y, z - other.z)
  operator fun times(scalar: Float) = Vec3(x * scalar, y * scalar, z * scalar)
  operator fun div(scalar: Float) = Vec3(x / scalar, y / scalar, z / scalar)

  fun length(): Float = sqrt(x * x + y * y + z * z)

  fun normalized(): Vec3 {
    val len = length()
    return if (len > 0.0001f) Vec3(x / len, y / len, z / len) else Vec3(0f, 0f, 0f)
  }

  fun distanceTo(other: Vec3): Float = (this - other).length()
}

data class Vec2(val x: Float, val y: Float)

data class ProjectedPoint(
  val screenX: Float,
  val screenY: Float,
  val depth: Float,
  val isVisible: Boolean
)

data class BoundingBox3D(
  val min: Vec3,
  val max: Vec3
) {
  fun intersects(other: BoundingBox3D): Boolean {
    return (min.x <= other.max.x && max.x >= other.min.x) &&
           (min.y <= other.max.y && max.y >= other.min.y) &&
           (min.z <= other.max.z && max.z >= other.min.z)
  }

  fun contains(point: Vec3): Boolean {
    return point.x in min.x..max.x &&
           point.y in min.y..max.y &&
           point.z in min.z..max.z
  }
}

class Camera3D(
  var target: Vec3 = Vec3(0f, 1.5f, 0f),
  var yaw: Float = 0f, // radians horizontal
  var pitch: Float = 0.35f, // radians vertical (looking slightly down)
  var distance: Float = 7.5f,
  var fovScale: Float = 1.3f
) {
  val position: Vec3
    get() {
      val horizontalDist = distance * cos(pitch)
      val verticalDist = distance * sin(pitch)
      val camX = target.x - horizontalDist * sin(yaw)
      val camZ = target.z - horizontalDist * cos(yaw)
      val camY = target.y + verticalDist
      return Vec3(camX, camY, camZ)
    }

  /**
   * Projects a 3D world coordinate into 2D screen coordinate with depth.
   */
  fun project(
    worldPos: Vec3,
    screenWidth: Float,
    screenHeight: Float
  ): ProjectedPoint {
    val camPos = position

    // Vector from camera to world point
    val dx = worldPos.x - camPos.x
    val dy = worldPos.y - camPos.y
    val dz = worldPos.z - camPos.z

    // Rotate by camera yaw (horizontal)
    val cosYaw = cos(-yaw)
    val sinYaw = sin(-yaw)
    val x1 = dx * cosYaw - dz * sinYaw
    val z1 = dx * sinYaw + dz * cosYaw

    // Rotate by camera pitch (vertical)
    val cosPitch = cos(-pitch)
    val sinPitch = sin(-pitch)
    val y2 = dy * cosPitch - z1 * sinPitch
    val z2 = dy * sinPitch + z1 * cosPitch

    // Check if behind camera
    if (z2 <= 0.2f) {
      return ProjectedPoint(0f, 0f, z2, isVisible = false)
    }

    val halfW = screenWidth / 2f
    val halfH = screenHeight / 2f
    val factor = (fovScale * halfH) / z2

    val screenX = halfW + x1 * factor
    val screenY = halfH - y2 * factor

    return ProjectedPoint(screenX, screenY, z2, isVisible = true)
  }
}
