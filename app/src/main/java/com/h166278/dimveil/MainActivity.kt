package com.h166278.dimveil

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.h166278.dimveil.domain.DimMode
import com.h166278.dimveil.service.OverlayService

class MainActivity: ComponentActivity() {
 override fun onCreate(savedInstanceState: Bundle?) { super.onCreate(savedInstanceState); setContent { DimVeilHome() } }
 @Composable private fun DimVeilHome() {
   var active by remember { mutableStateOf(false) }; var mode by remember { mutableStateOf(DimMode.NIGHT) }; var depth by remember { mutableIntStateOf(62) }
   val canDraw=OverlayService.canDraw(this)
   MaterialTheme(colorScheme=darkColorScheme(primary=Color(0xffcf75b1), background=Color(0xff080a12), surface=Color(0xff151625))) {
    Column(Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background).padding(24.dp), horizontalAlignment=Alignment.CenterHorizontally) {
      Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.SpaceBetween,verticalAlignment=Alignment.CenterVertically){Column{Text("暗幕",style=MaterialTheme.typography.headlineSmall);Text("让屏幕比系统最低亮度更暗",style=MaterialTheme.typography.bodySmall)}; Text(if(canDraw) "●" else "!",color=if(canDraw) Color(0xff6ee8a8) else Color(0xffffc54a),modifier=Modifier.clickable{ if(!canDraw) startActivity(Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:$packageName"))) })}
      Spacer(Modifier.height(54.dp)); Text(mode.label+"模式",style=MaterialTheme.typography.headlineMedium); Spacer(Modifier.height(20.dp))
      Button(onClick={ if(active){OverlayService.stop(this@MainActivity);active=false}else if(canDraw){OverlayService.start(this@MainActivity,depth,mode);active=true}else startActivity(Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,Uri.parse("package:$packageName"))) },modifier=Modifier.size(210.dp),shape=MaterialTheme.shapes.extraLarge){Text(if(active)"已开启\n点击关闭" else "开启遮罩")}
      Spacer(Modifier.height(28.dp)); Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.SpaceEvenly){DimMode.entries.forEach { item-> FilterChip(selected=mode==item,onClick={mode=item;depth=item.defaultDepth},label={Text(item.label)}) }}
      Spacer(Modifier.height(26.dp)); Text("遮罩深度  $depth%",style=MaterialTheme.typography.titleMedium); Slider(value=depth.toFloat(),onValueChange={depth=it.toInt();if(active)OverlayService.start(this@MainActivity,depth,mode)},valueRange=0f..90f); if(depth>=80) Text("深度较高，请确认仍能看清屏幕",color=Color(0xffffc54a)); Spacer(Modifier.weight(1f)); Text(if(OverlayService.notificationAllowed(this@MainActivity))"通知栏可快捷关闭遮罩" else "通知权限未开启，无法使用通知栏关闭按钮",style=MaterialTheme.typography.bodySmall)
    }
   }
 }
}
