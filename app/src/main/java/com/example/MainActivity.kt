package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
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
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Gamepad
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.auth.AuthScreen
import com.example.avatar.AvatarScreen
import com.example.game3d.AvatarCustomization
import com.example.game3d.Game3DCanvas
import com.example.home.HomeScreen
import com.example.inventory.InventoryScreen
import com.example.luau.LuauStudioScreen
import com.example.rewards.PlaytimeRewardSheet
import com.example.shop.ShopScreen
import com.example.social.SocialScreen
import com.example.ui.theme.BloxCyanPrimary
import com.example.ui.theme.BloxDarkBackground
import com.example.ui.theme.BloxDarkSurface
import com.example.ui.theme.BloxPurpleAccent
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

class MainActivity : ComponentActivity() {

  private val viewModel: MainViewModel by viewModels()

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()

    setContent {
      MyApplicationTheme {
        val currentUser by viewModel.currentUser.collectAsState()
        val authError by viewModel.authError.collectAsState()
        val activeWorld by viewModel.activeWorld3D.collectAsState()
        val currentScreen by viewModel.currentScreen.collectAsState()
        val playtimeSeconds by viewModel.playtimeSeconds.collectAsState()
        val currentIntervalSeconds by viewModel.currentIntervalSeconds.collectAsState()
        val showPlaytimeSheet by viewModel.showPlaytimeSheet.collectAsState()

        val allGames by viewModel.allGames.collectAsState()
        val userGames by viewModel.userGames.collectAsState()
        val inventory by viewModel.userInventory.collectAsState()
        val accessories by viewModel.catalogAccessories.collectAsState()
        val globalChat by viewModel.globalChat.collectAsState()
        val friends by viewModel.friendsList.collectAsState()
        val rooms by viewModel.privateRooms.collectAsState()

        // 1. If not logged in, show Auth Screen
        if (currentUser == null) {
          AuthScreen(
            onLogin = { user, pass -> viewModel.login(user, pass) },
            onRegister = { user, pass, display -> viewModel.register(user, pass, display) },
            errorMessage = authError,
            modifier = Modifier.fillMaxSize().statusBarsPadding().navigationBarsPadding()
          )
        } else {
          val user = currentUser!!

          // 2. If a 3D Game is actively running, render full-screen 3D Canvas!
          if (activeWorld != null) {
            BackHandler {
              viewModel.exitActiveGame()
            }
            Game3DCanvas(
              world = activeWorld!!,
              playerConfig = AvatarCustomization.fromUser(user),
              playerName = user.displayName,
              onExitGame = { viewModel.exitActiveGame() },
              onCoinsEarned = { amount -> viewModel.earnCoins(amount) },
              onRareItemFound = { name, cat, rarity, source ->
                viewModel.onRareItemFound(name, cat, rarity, source)
              },
              modifier = Modifier.fillMaxSize()
            )
          } else {
            // 3. Main Hub Navigation (Home, Studio, Avatar, Shop, Inventory, Social)
            Box(modifier = Modifier.fillMaxSize().background(BloxDarkBackground)) {
              Scaffold(
                modifier = Modifier.fillMaxSize(),
                containerColor = BloxDarkBackground,
                bottomBar = {
                  BloxBottomNavigation(
                    currentScreen = currentScreen,
                    onNavigate = { viewModel.navigateTo(it) }
                  )
                }
              ) { innerPadding ->
                Box(
                  modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .statusBarsPadding()
                ) {
                  when (currentScreen) {
                    ScreenNav.HOME -> {
                      HomeScreen(
                        currentUser = user,
                        games = allGames,
                        playtimeSeconds = playtimeSeconds,
                        onLaunchGame = { viewModel.launchGame(it) },
                        onLikeGame = { viewModel.likeGame(it) },
                        onOpenPlaytimeSheet = { viewModel.openPlaytimeSheet() },
                        onLogout = { viewModel.logout() }
                      )
                    }

                    ScreenNav.STUDIO -> {
                      LuauStudioScreen(
                        userPublishedGames = userGames,
                        onTestPlayGame = { title, code -> viewModel.launchLuauTestWorld(title, code) },
                        onPublishGame = { title, desc, genre, code -> viewModel.publishGame(title, desc, genre, code) },
                        onClaimPayout = { viewModel.claimGamePayout(it) }
                      )
                    }

                    ScreenNav.AVATAR -> {
                      AvatarScreen(
                        currentUser = user,
                        inventory = inventory,
                        onSaveAvatarConfig = { skin, torso, legs, hat, wings, gear, shirt, face, pet ->
                          viewModel.saveAvatarConfig(skin, torso, legs, hat, wings, gear, shirt, face, pet)
                        },
                        onCreateCustomAccessory = { name, cat, price, rarity, pColor, aColor, style, publish ->
                          viewModel.createCustomAccessory(name, cat, price, rarity, pColor, aColor, style, publish)
                        }
                      )
                    }

                    ScreenNav.SHOP -> {
                      val ownedIds = inventory.map { it.itemId }.toSet()
                      ShopScreen(
                        userCoins = user.coins,
                        accessories = accessories,
                        ownedItemIds = ownedIds,
                        onBuyItem = { viewModel.buyCatalogAccessory(it) },
                        onOpenPlaytimeRewards = { viewModel.openPlaytimeSheet() }
                      )
                    }

                    ScreenNav.INVENTORY -> {
                      InventoryScreen(
                        items = inventory,
                        onEquipItem = { viewModel.equipInventoryItem(it) }
                      )
                    }

                    ScreenNav.SOCIAL -> {
                      SocialScreen(
                        currentUserId = user.id,
                        currentUserName = user.displayName,
                        friends = friends,
                        globalChat = globalChat,
                        privateRooms = rooms,
                        onSendMessage = { channel, text -> viewModel.sendChatMessage(channel, text) },
                        onAddFriend = { viewModel.addFriend(it) },
                        onCreateRoom = { name, gameTitle -> viewModel.createPrivateRoom(name, gameTitle) },
                        onJoinRoomGame = { gameTitle -> viewModel.launchWorldByTitle(gameTitle) },
                        onJoinFriendGame = { gameTitle -> viewModel.launchWorldByTitle(gameTitle) }
                      )
                    }
                  }
                }
              }

              // Playtime Rewards Modal Bottomsheet/Dialog
              AnimatedVisibility(
                visible = showPlaytimeSheet,
                enter = fadeIn(),
                exit = fadeOut(),
                modifier = Modifier.align(Alignment.Center)
              ) {
                Box(
                  modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.75f))
                    .clickable { viewModel.dismissPlaytimeSheet() },
                  contentAlignment = Alignment.Center
                ) {
                  Box(modifier = Modifier.clickable(enabled = false) {}) {
                    PlaytimeRewardSheet(
                      totalPlaytimeSeconds = playtimeSeconds,
                      currentIntervalSeconds = currentIntervalSeconds,
                      onClaimMilestone = { milestone ->
                        viewModel.earnCoins(milestone.coinReward)
                      },
                      onDismiss = { viewModel.dismissPlaytimeSheet() }
                    )
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

@Composable
fun BloxBottomNavigation(
  currentScreen: ScreenNav,
  onNavigate: (ScreenNav) -> Unit
) {
  Surface(
    color = BloxDarkSurface,
    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF1E2638)),
    modifier = Modifier
      .fillMaxWidth()
      .navigationBarsPadding()
  ) {
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .padding(vertical = 6.dp, horizontal = 4.dp),
      horizontalArrangement = Arrangement.SpaceAround,
      verticalAlignment = Alignment.CenterVertically
    ) {
      NavItem(
        icon = Icons.Default.Gamepad,
        label = "Juegos 3D",
        isSelected = currentScreen == ScreenNav.HOME,
        onClick = { onNavigate(ScreenNav.HOME) }
      )
      NavItem(
        icon = Icons.Default.Code,
        label = "Estudio Luau",
        isSelected = currentScreen == ScreenNav.STUDIO,
        onClick = { onNavigate(ScreenNav.STUDIO) }
      )
      NavItem(
        icon = Icons.Default.Person,
        label = "Avatar 3D",
        isSelected = currentScreen == ScreenNav.AVATAR,
        onClick = { onNavigate(ScreenNav.AVATAR) }
      )
      NavItem(
        icon = Icons.Default.ShoppingBag,
        label = "Tienda",
        isSelected = currentScreen == ScreenNav.SHOP,
        onClick = { onNavigate(ScreenNav.SHOP) }
      )
      NavItem(
        icon = Icons.Default.Inventory2,
        label = "Inventario",
        isSelected = currentScreen == ScreenNav.INVENTORY,
        onClick = { onNavigate(ScreenNav.INVENTORY) }
      )
      NavItem(
        icon = Icons.Default.Chat,
        label = "Social",
        isSelected = currentScreen == ScreenNav.SOCIAL,
        onClick = { onNavigate(ScreenNav.SOCIAL) }
      )
    }
  }
}

@Composable
fun NavItem(
  icon: ImageVector,
  label: String,
  isSelected: Boolean,
  onClick: () -> Unit
) {
  Column(
    horizontalAlignment = Alignment.CenterHorizontally,
    modifier = Modifier
      .clip(RoundedCornerShape(12.dp))
      .clickable { onClick() }
      .padding(horizontal = 4.dp, vertical = 4.dp)
      .testTag("nav_item_${label.lowercase().replace(" ", "_")}")
  ) {
    Box(
      modifier = Modifier
        .size(36.dp)
        .clip(RoundedCornerShape(10.dp))
        .background(if (isSelected) BloxCyanPrimary.copy(alpha = 0.2f) else Color.Transparent),
      contentAlignment = Alignment.Center
    ) {
      Icon(
        icon,
        contentDescription = label,
        tint = if (isSelected) BloxCyanPrimary else TextSecondary,
        modifier = Modifier.size(20.dp)
      )
    }
    Text(
      label,
      color = if (isSelected) BloxCyanPrimary else TextSecondary,
      fontSize = 9.sp,
      fontWeight = if (isSelected) FontWeight.Black else FontWeight.Medium
    )
  }
}
