package com.example.game3d

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import kotlin.math.cos
import kotlin.math.sin

data class PolygonFace(
  val points: List<Vec2>,
  val avgDepth: Float,
  val fillColor: Color,
  val strokeColor: Color? = null
)

class Box3D(
  var center: Vec3,
  val size: Vec3,
  var rotationYaw: Float = 0f,
  var baseColor: Color = Color.Cyan,
  val isLava: Boolean = false,
  val isTrampoline: Boolean = false,
  val isCoin: Boolean = false,
  val isRareItem: Boolean = false,
  val rareItemName: String = "",
  val rareItemType: String = "",
  val rareItemRarity: String = "EPICO",
  val isGoal: Boolean = false,
  var isCollected: Boolean = false
) {
  val boundingBox: BoundingBox3D
    get() = BoundingBox3D(
      min = Vec3(center.x - size.x / 2f, center.y - size.y / 2f, center.z - size.z / 2f),
      max = Vec3(center.x + size.x / 2f, center.y + size.y / 2f, center.z + size.z / 2f)
    )

  // Generate 8 vertices of the box with yaw rotation
  fun getVertices(): List<Vec3> {
    val hx = size.x / 2f
    val hy = size.y / 2f
    val hz = size.z / 2f

    val localOffsets = listOf(
      Vec3(-hx, -hy, -hz), // 0
      Vec3(hx, -hy, -hz),  // 1
      Vec3(hx, hy, -hz),   // 2
      Vec3(-hx, hy, -hz),  // 3
      Vec3(-hx, -hy, hz),  // 4
      Vec3(hx, -hy, hz),   // 5
      Vec3(hx, hy, hz),    // 6
      Vec3(-hx, hy, hz)    // 7
    )

    val cosY = cos(rotationYaw)
    val sinY = sin(rotationYaw)

    return localOffsets.map { offset ->
      val rx = offset.x * cosY - offset.z * sinY
      val rz = offset.x * sinY + offset.z * cosY
      Vec3(center.x + rx, center.y + offset.y, center.z + rz)
    }
  }

  // Create 6 faces of the box, project vertices and shade by normal direction
  fun generateFaces(
    camera: Camera3D,
    screenWidth: Float,
    screenHeight: Float
  ): List<PolygonFace> {
    if (isCollected) return emptyList()

    val verts = getVertices()
    val projected = verts.map { camera.project(it, screenWidth, screenHeight) }

    // If all vertices are behind camera, skip
    if (projected.none { it.isVisible }) return emptyList()

    // 6 faces defined by indices (CCW or CW)
    // Indices:
    // Front: 4, 5, 6, 7
    // Back: 1, 0, 3, 2
    // Top: 3, 2, 6, 7
    // Bottom: 0, 1, 5, 4
    // Left: 0, 4, 7, 3
    // Right: 5, 1, 2, 6
    val faceIndices = listOf(
      QuadIndices(listOf(4, 5, 6, 7), 1.0f),  // Front
      QuadIndices(listOf(1, 0, 3, 2), 0.75f), // Back
      QuadIndices(listOf(3, 2, 6, 7), 1.25f), // Top (lighter)
      QuadIndices(listOf(0, 1, 5, 4), 0.5f),  // Bottom (darker)
      QuadIndices(listOf(0, 4, 7, 3), 0.85f), // Left
      QuadIndices(listOf(5, 1, 2, 6), 0.95f)  // Right
    )

    val faces = mutableListOf<PolygonFace>()
    for (f in faceIndices) {
      val pts = f.indices.map { projected[it] }
      if (pts.any { !it.isVisible }) continue

      // Average depth for Painter's sorting
      val avgDepth = pts.map { it.depth }.average().toFloat()

      // Calculate 2D face normal/winding order to skip back-facing quads
      val v0 = pts[0]
      val v1 = pts[1]
      val v2 = pts[2]
      val cross = (v1.screenX - v0.screenX) * (v2.screenY - v0.screenY) -
                  (v1.screenY - v0.screenY) * (v2.screenX - v0.screenX)
      if (cross >= 0) {
        // Back-face culled
        continue
      }

      val lightFactor = f.brightnessFactor.coerceIn(0.4f, 1.4f)
      val shadedColor = Color(
        red = (baseColor.red * lightFactor).coerceIn(0f, 1f),
        green = (baseColor.green * lightFactor).coerceIn(0f, 1f),
        blue = (baseColor.blue * lightFactor).coerceIn(0f, 1f),
        alpha = baseColor.alpha
      )

      faces.add(
        PolygonFace(
          points = pts.map { Vec2(it.screenX, it.screenY) },
          avgDepth = avgDepth,
          fillColor = shadedColor,
          strokeColor = if (isCoin || isRareItem) Color.White.copy(alpha = 0.5f) else Color.Black.copy(alpha = 0.2f)
        )
      )
    }

    return faces
  }

  private data class QuadIndices(val indices: List<Int>, val brightnessFactor: Float)
}

fun DrawScope.renderPolygonFaces(faces: List<PolygonFace>) {
  for (face in faces) {
    if (face.points.size < 3) continue
    val path = Path().apply {
      moveTo(face.points[0].x, face.points[0].y)
      for (i in 1 until face.points.size) {
        lineTo(face.points[i].x, face.points[i].y)
      }
      close()
    }
    drawPath(path, face.fillColor, style = Fill)
    face.strokeColor?.let {
      drawPath(path, it, style = Stroke(width = 1.5f))
    }
  }
}
