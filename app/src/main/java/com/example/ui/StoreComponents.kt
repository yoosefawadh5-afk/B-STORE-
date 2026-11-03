package com.example.ui

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.OpenInBrowser
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.SupportAgent
import androidx.compose.material.icons.filled.Tv
import androidx.compose.material.icons.filled.WifiOff
import androidx.compose.material.icons.outlined.ContentCopy
import androidx.compose.material.icons.outlined.DeleteOutline
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.ui.theme.BrandAccentAmber
import com.example.ui.theme.BrandBluePrimary
import com.example.ui.theme.SuccessGreen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StoreTopBar(
  currentUrl: String,
  pageTitle: String,
  isLoading: Boolean,
  canGoBack: Boolean,
  canGoForward: Boolean,
  onBackClick: () -> Unit,
  onForwardClick: () -> Unit,
  onRefreshClick: () -> Unit,
  onHomeClick: () -> Unit,
  onClearCacheClick: () -> Unit,
  modifier: Modifier = Modifier
) {
  val context = LocalContext.current
  var showMenu by remember { mutableStateOf(false) }
  var showSslDialog by remember { mutableStateOf(false) }
  var showAboutDialog by remember { mutableStateOf(false) }

  TopAppBar(
    modifier = modifier.testTag("store_top_bar"),
    colors = TopAppBarDefaults.topAppBarColors(
      containerColor = MaterialTheme.colorScheme.surface,
      titleContentColor = MaterialTheme.colorScheme.onSurface,
      actionIconContentColor = MaterialTheme.colorScheme.onSurface
    ),
    title = {
      Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
      ) {
        // Official Store Icon badge
        Surface(
          shape = CircleShape,
          color = BrandBluePrimary,
          modifier = Modifier.size(34.dp)
        ) {
          Image(
            painter = painterResource(id = R.drawable.b_store_official_logo),
            contentDescription = "B Store Logo",
            modifier = Modifier
              .fillMaxSize()
              .padding(4.dp)
          )
        }

        Column(modifier = Modifier.weight(1f, fill = false)) {
          Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
          ) {
            Text(
              text = "B Store KSA",
              style = MaterialTheme.typography.titleMedium.copy(
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp
              ),
              maxLines = 1,
              overflow = TextOverflow.Ellipsis
            )

            // SSL Lock icon
            IconButton(
              onClick = { showSslDialog = true },
              modifier = Modifier.size(20.dp)
            ) {
              Icon(
                imageVector = Icons.Default.Lock,
                contentDescription = "الاتصال آمن ومشفر",
                tint = SuccessGreen,
                modifier = Modifier.size(13.dp)
              )
            }
          }

          Text(
            text = if (pageTitle.isNotBlank()) pageTitle else "b-storeksa.com",
            style = MaterialTheme.typography.bodySmall.copy(
              fontSize = 11.sp,
              color = MaterialTheme.colorScheme.onSurfaceVariant
            ),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
          )
        }
      }
    },
    navigationIcon = {
      Row {
        if (canGoBack) {
          IconButton(
            onClick = onBackClick,
            modifier = Modifier.testTag("nav_back_button")
          ) {
            Icon(
              imageVector = Icons.Default.ArrowBack,
              contentDescription = "رجوع"
            )
          }
        }
      }
    },
    actions = {
      if (canGoForward) {
        IconButton(
          onClick = onForwardClick,
          modifier = Modifier.testTag("nav_forward_button")
        ) {
          Icon(
            imageVector = Icons.Default.ArrowForward,
            contentDescription = "إلى الأمام"
          )
        }
      }

      // Reload / Refresh
      IconButton(
        onClick = onRefreshClick,
        modifier = Modifier.testTag("refresh_button")
      ) {
        Icon(
          imageVector = if (isLoading) Icons.Default.Close else Icons.Default.Refresh,
          contentDescription = if (isLoading) "إيقاف التحميل" else "إعادة تحميل الصفحة"
        )
      }

      // Home
      IconButton(
        onClick = onHomeClick,
        modifier = Modifier.testTag("home_button")
      ) {
        Icon(
          imageVector = Icons.Default.Home,
          contentDescription = "الصفحة الرئيسية"
        )
      }

      // More menu
      Box {
        IconButton(
          onClick = { showMenu = true },
          modifier = Modifier.testTag("menu_button")
        ) {
          Icon(
            imageVector = Icons.Default.MoreVert,
            contentDescription = "المزيد من الخيارات"
          )
        }

        DropdownMenu(
          expanded = showMenu,
          onDismissRequest = { showMenu = false }
        ) {
          DropdownMenuItem(
            text = { Text("مشاركة الرابط") },
            leadingIcon = {
              Icon(Icons.Default.Share, contentDescription = null)
            },
            onClick = {
              showMenu = false
              val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(Intent.EXTRA_SUBJECT, pageTitle)
                putExtra(Intent.EXTRA_TEXT, currentUrl)
              }
              context.startActivity(Intent.createChooser(shareIntent, "مشاركة رابط المتجر"))
            }
          )

          DropdownMenuItem(
            text = { Text("نسخ الرابط") },
            leadingIcon = {
              Icon(Icons.Outlined.ContentCopy, contentDescription = null)
            },
            onClick = {
              showMenu = false
              val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
              val clip = ClipData.newPlainText("B Store URL", currentUrl)
              clipboard.setPrimaryClip(clip)
              Toast.makeText(context, "تم نسخ الرابط بنجاح", Toast.LENGTH_SHORT).show()
            }
          )

          DropdownMenuItem(
            text = { Text("فتح في المتصفح") },
            leadingIcon = {
              Icon(Icons.Default.OpenInBrowser, contentDescription = null)
            },
            onClick = {
              showMenu = false
              try {
                val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse(currentUrl))
                context.startActivity(browserIntent)
              } catch (e: Exception) {
                Toast.makeText(context, "تعذر فتح المتصفح", Toast.LENGTH_SHORT).show()
              }
            }
          )

          DropdownMenuItem(
            text = { Text("مسح الذاكرة المؤقتة") },
            leadingIcon = {
              Icon(Icons.Outlined.DeleteOutline, contentDescription = null)
            },
            onClick = {
              showMenu = false
              onClearCacheClick()
            }
          )

          DropdownMenuItem(
            text = { Text("عن متجر بي (B Store)") },
            leadingIcon = {
              Icon(Icons.Outlined.Info, contentDescription = null)
            },
            onClick = {
              showMenu = false
              showAboutDialog = true
            }
          )
        }
      }
    }
  )

  // SSL Info Dialog
  if (showSslDialog) {
    AlertDialog(
      onDismissRequest = { showSslDialog = false },
      icon = {
        Icon(
          Icons.Default.Lock,
          contentDescription = null,
          tint = SuccessGreen,
          modifier = Modifier.size(32.dp)
        )
      },
      title = {
        Text("اتصال آمن ومشفر (HTTPS)", fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
      },
      text = {
        Text(
          "يتم تشفير كافة البيانات والمعاملات المالية في متجر بي ستور (b-storeksa.com) بأعلى معايير الأمان لحماية خصوصيتك وبياناتك.",
          textAlign = TextAlign.Center
        )
      },
      confirmButton = {
        TextButton(onClick = { showSslDialog = false }) {
          Text("حسناً")
        }
      }
    )
  }

  // About Store Dialog
  if (showAboutDialog) {
    AlertDialog(
      onDismissRequest = { showAboutDialog = false },
      title = {
        Row(
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
          Surface(
            shape = CircleShape,
            color = BrandBluePrimary,
            modifier = Modifier.size(36.dp)
          ) {
            Image(
              painter = painterResource(id = R.drawable.b_store_official_logo),
              contentDescription = null,
              modifier = Modifier.fillMaxSize().padding(4.dp)
            )
          }
          Text("متجر بي ستور (B Store)", fontWeight = FontWeight.Bold)
        }
      },
      text = {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
          Text("المتجر الرائد في المملكة العربية السعودية لخدمات:")
          Text("• تجديد اشتراكات beIN Sport أونلاين فورياً وبأفضل الأسعار")
          Text("• البطاقات الرقمية وبطاقات الهدايا والشحن")
          Text("• الاشتراكات الرقمية والترفيهية")
          Text("• دعم فني سريع عبر الواتساب وخدمة عملاء على مدار الساعة")
          Spacer(modifier = Modifier.height(4.dp))
          Text(
            "الموقع الرسمي: b-storeksa.com",
            style = MaterialTheme.typography.bodySmall,
            color = BrandBluePrimary
          )
        }
      },
      confirmButton = {
        Button(
          onClick = { showAboutDialog = false },
          colors = ButtonDefaults.buttonColors(containerColor = BrandBluePrimary)
        ) {
          Text("إغلاق")
        }
      }
    )
  }
}

@Composable
fun StoreProgressBar(
  isLoading: Boolean,
  progress: Int,
  modifier: Modifier = Modifier
) {
  AnimatedVisibility(
    visible = isLoading && progress in 1..99,
    enter = fadeIn(),
    exit = fadeOut(),
    modifier = modifier
  ) {
    val animatedProgress by animateFloatAsState(
      targetValue = progress / 100f,
      label = "progress"
    )
    LinearProgressIndicator(
      progress = { animatedProgress },
      modifier = Modifier
        .fillMaxWidth()
        .height(3.dp),
      color = BrandAccentAmber,
      trackColor = Color.Transparent
    )
  }
}

@Composable
fun StoreBottomBar(
  currentUrl: String,
  onNavigateUrl: (String) -> Unit,
  onContactSupport: () -> Unit,
  modifier: Modifier = Modifier
) {
  val isHome = currentUrl == "https://b-storeksa.com" || currentUrl == "https://b-storeksa.com/"
  val isCart = currentUrl.contains("/cart")

  NavigationBar(
    modifier = modifier.testTag("store_bottom_bar"),
    containerColor = MaterialTheme.colorScheme.surface,
    tonalElevation = 6.dp
  ) {
    NavigationBarItem(
      selected = isHome,
      onClick = { onNavigateUrl("https://b-storeksa.com") },
      icon = { Icon(Icons.Default.Home, contentDescription = "الرئيسية") },
      label = { Text("الرئيسية") },
      colors = NavigationBarItemDefaults.colors(
        selectedIconColor = BrandBluePrimary,
        selectedTextColor = BrandBluePrimary,
        indicatorColor = BrandBluePrimary.copy(alpha = 0.12f)
      )
    )

    NavigationBarItem(
      selected = isCart,
      onClick = { onNavigateUrl("https://b-storeksa.com/cart") },
      icon = { Icon(Icons.Default.ShoppingCart, contentDescription = "السلة") },
      label = { Text("السلة") },
      colors = NavigationBarItemDefaults.colors(
        selectedIconColor = BrandBluePrimary,
        selectedTextColor = BrandBluePrimary,
        indicatorColor = BrandBluePrimary.copy(alpha = 0.12f)
      )
    )

    NavigationBarItem(
      selected = false,
      onClick = { onNavigateUrl("https://b-storeksa.com") },
      icon = { Icon(Icons.Default.Tv, contentDescription = "اشتراك beIN") },
      label = { Text("تجديد beIN") },
      colors = NavigationBarItemDefaults.colors(
        selectedIconColor = BrandBluePrimary,
        selectedTextColor = BrandBluePrimary
      )
    )

    NavigationBarItem(
      selected = false,
      onClick = onContactSupport,
      icon = { Icon(Icons.Default.SupportAgent, contentDescription = "الدعم الفني") },
      label = { Text("خدمة العملاء") },
      colors = NavigationBarItemDefaults.colors(
        selectedIconColor = BrandBluePrimary,
        selectedTextColor = BrandBluePrimary
      )
    )
  }
}

@Composable
fun ErrorScreen(
  isOffline: Boolean,
  onRetry: () -> Unit,
  onOpenBrowser: () -> Unit,
  modifier: Modifier = Modifier
) {
  Column(
    modifier = modifier
      .fillMaxSize()
      .padding(24.dp)
      .testTag("error_screen"),
    horizontalAlignment = Alignment.CenterHorizontally,
    verticalArrangement = Arrangement.Center
  ) {
    Card(
      shape = RoundedCornerShape(24.dp),
      colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
      elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
      modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp)
    ) {
      Column(
        modifier = Modifier.padding(28.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
      ) {
        Surface(
          shape = CircleShape,
          color = if (isOffline) MaterialTheme.colorScheme.errorContainer else BrandBluePrimary.copy(alpha = 0.12f),
          modifier = Modifier.size(72.dp)
        ) {
          Box(contentAlignment = Alignment.Center) {
            Icon(
              imageVector = if (isOffline) Icons.Default.WifiOff else Icons.Default.Refresh,
              contentDescription = null,
              tint = if (isOffline) MaterialTheme.colorScheme.error else BrandBluePrimary,
              modifier = Modifier.size(36.dp)
            )
          }
        }

        Text(
          text = if (isOffline) "لا يوجد اتصال بالإنترنت" else "تعذر تحميل المتجر",
          style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
          textAlign = TextAlign.Center
        )

        Text(
          text = if (isOffline) {
            "يرجى التحقق من اتصال هاتفك بشبكة الـ Wi-Fi أو بيانات الجوال لتصفح b-storeksa.com والتمتع بالعروض"
          } else {
            "حدث خطأ غير متوقع أثناء محاولة فتح المتجر. يرجى الضغط على زر إعادة المحاولة"
          },
          style = MaterialTheme.typography.bodyMedium.copy(
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            lineHeight = 22.sp
          ),
          textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(8.dp))

        Button(
          onClick = onRetry,
          colors = ButtonDefaults.buttonColors(containerColor = BrandBluePrimary),
          shape = RoundedCornerShape(12.dp),
          modifier = Modifier
            .fillMaxWidth()
            .height(50.dp)
            .testTag("retry_button")
        ) {
          Icon(Icons.Default.Refresh, contentDescription = null)
          Spacer(modifier = Modifier.width(8.dp))
          Text("إعادة المحاولة", fontWeight = FontWeight.Bold)
        }

        OutlinedButton(
          onClick = onOpenBrowser,
          shape = RoundedCornerShape(12.dp),
          modifier = Modifier
            .fillMaxWidth()
            .height(50.dp)
            .testTag("open_browser_button")
        ) {
          Icon(Icons.Default.OpenInBrowser, contentDescription = null)
          Spacer(modifier = Modifier.width(8.dp))
          Text("فتح في متصفح خارجي")
        }
      }
    }
  }
}
