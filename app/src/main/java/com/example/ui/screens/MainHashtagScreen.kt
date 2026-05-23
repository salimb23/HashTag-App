package com.example.ui.screens

import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.api.ParsedHashtagsResponse
import com.example.data.db.SavedHashtagSet
import com.example.ui.viewmodel.GenerationUiState
import com.example.ui.viewmodel.HashtagViewModel
import kotlinx.coroutines.flow.collectLatest
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

// Custom Programmatic Copy Icon to guarantee zero-dependency compilation
val CustomCopyIcon: ImageVector
    @Composable
    get() = remember {
        ImageVector.Builder(
            name = "CustomCopyIcon",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f
        ).apply {
            path(fill = SolidColor(Color.White)) {
                moveTo(16f, 1f)
                lineTo(4f, 1f)
                curveTo(2.9f, 1f, 2f, 1.9f, 2f, 3f)
                verticalLineTo(17f)
                horizontalLineTo(4f)
                verticalLineTo(3f)
                horizontalLineTo(16f)
                verticalLineTo(1f)
                close()
                moveTo(19f, 5f)
                horizontalLineTo(8f)
                curveTo(6.9f, 5f, 6f, 5.9f, 6f, 7f)
                verticalLineTo(21f)
                curveTo(6f, 22.1f, 6.9f, 23f, 8f, 23f)
                horizontalLineTo(19f)
                curveTo(20.1f, 23f, 21f, 22.1f, 21f, 21f)
                verticalLineTo(7f)
                curveTo(21f, 5.9f, 20.1f, 5f, 19f, 5f)
                close()
                moveTo(19f, 21f)
                horizontalLineTo(8f)
                verticalLineTo(7f)
                horizontalLineTo(19f)
                verticalLineTo(21f)
                close()
            }
        }.build()
    }

@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
@Composable
fun MainHashtagScreen(
    viewModel: HashtagViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current

    val description by viewModel.description.collectAsStateWithLifecycle()
    val selectedPlatform by viewModel.selectedPlatform.collectAsStateWithLifecycle()
    val uiState by viewModel.generationUiState.collectAsStateWithLifecycle()
    val savedHashtags by viewModel.savedHashtags.collectAsStateWithLifecycle()

    // Handle Toast Events
    LaunchedEffect(key1 = true) {
        viewModel.toastEvent.collectLatest { message ->
            Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
        }
    }

    // Modern color design system (Deep Space Theme in Arabic layout)
    val backgroundGradient = Brush.verticalGradient(
        colors = listOf(
            Color(0xFF0F172A), // Slate 900
            Color(0xFF1E1B4B), // Indigo 950
            Color(0xFF0F0F1A)  // Smooth dark canvas bottom
        )
    )

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = Color.Transparent
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(backgroundGradient)
                .padding(innerPadding)
        ) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                contentPadding = PaddingValues(top = 16.dp, bottom = 48.dp)
            ) {
                // Header (RTL Header style)
                item {
                    HeaderSection()
                }

                // Description Tagline Input
                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, Color(0xFF334155), RoundedCornerShape(24.dp)),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B).copy(alpha = 0.8f)),
                        shape = RoundedCornerShape(24.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Text(
                                text = "💡 ماذا تريد أن تنشر اليوم؟",
                                color = Color.White,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                textAlign = TextAlign.Right,
                                modifier = Modifier.fillMaxWidth()
                            )

                            OutlinedTextField(
                                value = description,
                                onValueChange = { viewModel.updateDescription(it) },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("tag_description_input"),
                                placeholder = {
                                    Text(
                                        text = "مثال: طبخ كبة شامية لذيذة، أو نصائح للسفر الاقتصادي لماليزيا، أو مشاريع ذكاء اصطناعي...",
                                        color = Color(0xFF94A3B8),
                                        fontSize = 14.sp,
                                        textAlign = TextAlign.Right,
                                        modifier = Modifier.fillMaxWidth()
                                    )
                                },
                                maxLines = 4,
                                textStyle = LocalTextStyle.current.copy(
                                    color = Color.White,
                                    fontSize = 14.sp,
                                    textAlign = TextAlign.Right
                                ),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = Color(0xFFD946EF), // Fuchsia neon
                                    unfocusedBorderColor = Color(0xFF475569),
                                    focusedContainerColor = Color(0xFF0F172A),
                                    unfocusedContainerColor = Color(0xFF0F172A)
                                ),
                                shape = RoundedCornerShape(12.dp)
                            )

                            // Platform Selector Title
                            Text(
                                text = "📱 اختر شبكة التواصل المستهدفة:",
                                color = Color.White,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold,
                                textAlign = TextAlign.Right,
                                modifier = Modifier.fillMaxWidth()
                            )

                            // Platforms row
                            val platforms = listOf("Instagram", "TikTok", "X", "LinkedIn")
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                platforms.forEach { platform ->
                                    val isSelected = selectedPlatform == platform
                                    PlatformSelectableChip(
                                        name = platform,
                                        selected = isSelected,
                                        onClick = { viewModel.updatePlatform(platform) },
                                        modifier = Modifier.weight(1f)
                                    )
                                }
                            }

                            // CTA Trigger Action
                            Button(
                                onClick = { viewModel.generateTags() },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(52.dp)
                                    .testTag("generate_hashtags_button"),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color.Transparent
                                ),
                                shape = RoundedCornerShape(14.dp),
                                contentPadding = PaddingValues()
                            ) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .background(
                                            Brush.horizontalGradient(
                                                colors = listOf(Color(0xFFD946EF), Color(0xFF8B5CF6))
                                            )
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Search,
                                            contentDescription = null,
                                            tint = Color.White,
                                            modifier = Modifier.size(20.dp)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = "توليد الهاشتاجات الذكية ✨",
                                            fontWeight = FontWeight.Bold,
                                            color = Color.White,
                                            fontSize = 16.sp
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // Quick Micro-tag Prompts
                item {
                    QuickPromptsSection(onSelect = { viewModel.selectQuickCategory(it) })
                }

                // Response Canvas (Dynamic State view integration)
                item {
                    AnimatedContent(
                        targetState = uiState,
                        transitionSpec = {
                            fadeIn() togetherWith fadeOut()
                        },
                        label = "UiStateTransit"
                    ) { state ->
                        when (state) {
                            is GenerationUiState.Initial -> {
                                EmptyStateCard()
                            }
                            is GenerationUiState.Loading -> {
                                LoadingStateCard()
                            }
                            is GenerationUiState.Success -> {
                                SuccessStateCard(
                                    response = state.data,
                                    onCopyAll = {
                                        val joined = state.data.hashtags.joinToString(" ")
                                        clipboardManager.setText(AnnotatedString(joined))
                                        Toast.makeText(context, "تم نسخ كل الهاشتاجات! 📋", Toast.LENGTH_SHORT).show()
                                    },
                                    onSave = {
                                        val defaultTitle = description.take(24) + "..."
                                        viewModel.saveCurrentSet(defaultTitle, state.data)
                                    },
                                    onClear = {
                                        viewModel.resetState()
                                    }
                                )
                            }
                            is GenerationUiState.Error -> {
                                ErrorStateCard(
                                    message = state.errorMessage,
                                    onRetry = { viewModel.generateTags() }
                                )
                            }
                        }
                    }
                }

                // Saved Archive Drawer Listing
                item {
                    Text(
                        text = "⭐ الحافظة والمفضلة المحفوظة",
                        color = Color.White,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Right,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp)
                    )
                }

                if (savedHashtags.isEmpty()) {
                    item {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .border(1.dp, Color(0xFF334155).copy(alpha = 0.5f), RoundedCornerShape(16.dp)),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A).copy(alpha = 0.5f)),
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(24.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Star,
                                    contentDescription = null,
                                    tint = Color(0xFF475569),
                                    modifier = Modifier.size(32.dp)
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "لم تقم بحفظ أي هاشتاجات حتى الآن.",
                                    color = Color(0xFF94A3B8),
                                    fontSize = 12.sp,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    }
                } else {
                    items(savedHashtags, key = { it.id }) { savedItem ->
                        SavedHashtagItemRow(
                            savedSet = savedItem,
                            onCopy = {
                                clipboardManager.setText(AnnotatedString(savedItem.hashtags))
                                Toast.makeText(context, "تم النسخ بنجاح! 📋", Toast.LENGTH_SHORT).show()
                            },
                            onDelete = {
                                viewModel.deleteSavedSet(savedItem)
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun HeaderSection() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(
            horizontalAlignment = Alignment.End,
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = "هاشتاج الذكي 📱",
                color = Color.White,
                fontSize = 24.sp,
                fontWeight = FontWeight.Black,
                textAlign = TextAlign.Right
            )
            Text(
                text = "جلب الهاشتاجات الأكثر فاعلية وتريند بالذكاء الاصطناعي",
                color = Color(0xFF34D399), // Emerald accent
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                textAlign = TextAlign.Right
            )
        }

        Spacer(modifier = Modifier.width(16.dp))

        // Arabesque circular neon badge icon representing social trend charts
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(RoundedCornerShape(14.dp))
                .background(Brush.radialGradient(listOf(Color(0xFF8B5CF6), Color(0xFFEC4899)))),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "#",
                color = Color.White,
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
fun PlatformSelectableChip(
    name: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    // Elegant platform custom coloring helper
    val selectedColor = when (name) {
        "Instagram" -> Color(0xFFEC4899) // Pinkish
        "TikTok" -> Color(0xFF06B6D4) // Cyan
        "X" -> Color(0xFFFFFFFF) // White
        "LinkedIn" -> Color(0xFF2563EB) // Blue
        else -> Color(0xFFD946EF)
    }

    Box(
        modifier = modifier
            .height(38.dp)
            .clip(RoundedCornerShape(10.dp))
            .background(if (selected) selectedColor.copy(alpha = 0.25f) else Color(0xFF0F172A))
            .border(
                width = 1.dp,
                color = if (selected) selectedColor else Color(0xFF334155),
                shape = RoundedCornerShape(10.dp)
            )
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = when (name) {
                "Instagram" -> "إنستغرام"
                "TikTok" -> "تيك توك"
                "X" -> "إكس (X)"
                "LinkedIn" -> "لينكد إن"
                else -> name
            },
            color = if (selected) selectedColor else Color(0xFF94A3B8),
            fontSize = 11.sp,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
fun QuickPromptsSection(onSelect: (String) -> Unit) {
    val items = listOf(
        "✈️ سياحة وسفر في دبي" to "سفر سياحة رحلات دبي فنادق شواطئ عطلة معالم دبي السياحية",
        "🍲 وصفة برياني دجاج" to "طبخ برياني دجاج وصفة سهلة مطبخ شيف عشاء لذيذ طعام شهي",
        "💻 تعلم لغة كوتلن" to "تطوير تطبيقات برمجة كوتلن اندرويد تكنولوجيا تعلم كودينج مبرمج",
        "🏋️‍♂️ تمرين لياقة سريع" to "تمارين كارديو جيم فتنس لياقة صحة رشاقة تمرين منزل سريع",
        "📈 استراتيجيات التسويق" to "تسويق رقمي بيزنس مشاريع أرباح مبيعات ريادة أعمال شركات تجارة"
    )

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Text(
            text = "✨ بنقرة واحدة (أفكار سريعة):",
            color = Color(0xFF94A3B8),
            fontSize = 13.sp,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Right
        )

        LazyRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            reverseLayout = true // For correct Arabic browsing flow from right to left
        ) {
            items(items) { (displayName, query) ->
                Box(
                    modifier = Modifier
                        .height(34.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0xFF334155).copy(alpha = 0.3f))
                        .border(1.dp, Color(0xFF475569).copy(alpha = 0.4f), RoundedCornerShape(8.dp))
                        .clickable { onSelect(query) }
                        .padding(horizontal = 12.dp, vertical = 6.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = displayName,
                        color = Color(0xFFE2E8F0),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}

@Composable
fun EmptyStateCard() {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, Color(0xFF334155).copy(alpha = 0.8f), RoundedCornerShape(20.dp)),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B).copy(alpha = 0.5f)),
        shape = RoundedCornerShape(20.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = Icons.Default.Info,
                contentDescription = null,
                tint = Color(0xFF8B5CF6).copy(alpha = 0.8f),
                modifier = Modifier.size(42.dp)
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = "جلب الهاشتاجات فارغ الآن",
                color = Color.White,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "اكتب فكرتك في الحقل أعلاه واضغط على زر التوليد للحصول على هاشتاجات احترافية فوراً.",
                color = Color(0xFF94A3B8),
                fontSize = 12.sp,
                lineHeight = 18.sp,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
fun LoadingStateCard() {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, Color(0xFF334155).copy(alpha = 0.8f), RoundedCornerShape(20.dp)),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B).copy(alpha = 0.8f)),
        shape = RoundedCornerShape(20.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            CircularProgressIndicator(
                color = Color(0xFFD946EF),
                strokeWidth = 3.dp,
                modifier = Modifier.size(36.dp)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "جاري تحليل المنشور وتوليد الهاشتاجات الذكية...",
                color = Color.White,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "نقوم باستهداف الهاشتاجات النشطة والأكثر ملاءمة لانتشارك التجاري.",
                color = Color(0xFF94A3B8),
                fontSize = 11.sp,
                textAlign = TextAlign.Center
            )
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun SuccessStateCard(
    response: ParsedHashtagsResponse,
    onCopyAll: () -> Unit,
    onSave: () -> Unit,
    onClear: () -> Unit
) {
    val clipboardManager = LocalClipboardManager.current
    val context = LocalContext.current

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, Color(0xFF34D399).copy(alpha = 0.5f), RoundedCornerShape(20.dp)),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF111827).copy(alpha = 0.95f)),
        shape = RoundedCornerShape(20.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Success Header Section
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Clear button on left
                IconButton(
                    onClick = onClear,
                    modifier = Modifier
                        .size(32.dp)
                        .background(Color(0xFF374151), RoundedCornerShape(8.dp))
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Clear result",
                        tint = Color.White,
                        modifier = Modifier.size(16.dp)
                    )
                }

                // Category and score indicators at center/right
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(Color(0xFF059669).copy(alpha = 0.2f))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = "الوصول: ${response.reachScore}",
                            color = Color(0xFF34D399),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(Color(0xFF8B5CF6).copy(alpha = 0.2f))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = "التصنيف: ${response.category}",
                            color = Color(0xFFA78BFA),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            // Divider
            HorizontalDivider(color = Color(0xFF374151))

            Text(
                text = "👇 انقر على أي هاشتاج لتنسخه منفرداً:",
                color = Color(0xFFE2E8F0),
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                textAlign = TextAlign.Right,
                modifier = Modifier.fillMaxWidth()
            )

            // Flow grid of interactive hashtags
            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp, Alignment.End),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                response.hashtags.forEach { tag ->
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color(0xFF1F2937))
                            .border(1.dp, Color(0xFF374151), RoundedCornerShape(8.dp))
                            .clickable {
                                clipboardManager.setText(AnnotatedString(tag))
                                Toast.makeText(context, "نسخ: $tag", Toast.LENGTH_SHORT).show()
                            }
                            .padding(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = tag,
                            color = Color(0xFF38BDF8), // Cyan neon tag aesthetic
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            // Divider
            HorizontalDivider(color = Color(0xFF374151))

            // Expert Marketing Tips Area
            if (response.tips.isNotEmpty()) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFF1E1B4B).copy(alpha = 0.6f))
                        .padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "نصيحة الخبير التسويقية 💡",
                            color = Color(0xFFFBBF24), // Amber text
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Right
                        )
                    }
                    Text(
                        text = response.tips,
                        color = Color(0xFFCBD5E1),
                        fontSize = 12.sp,
                        lineHeight = 18.sp,
                        textAlign = TextAlign.Right,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            // Copy All & Saved to Favorites Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Save Button
                Button(
                    onClick = onSave,
                    modifier = Modifier
                        .weight(1f)
                        .height(44.dp)
                        .testTag("save_hashtags_button"),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF374151)),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Star,
                        contentDescription = null,
                        tint = Color(0xFFFBBF24),
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "حفظ الغلاف ⭐",
                        color = Color.White,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                // Copy All Button with neon gradient borders
                Button(
                    onClick = onCopyAll,
                    modifier = Modifier
                        .weight(1.2f)
                        .height(44.dp)
                        .testTag("copy_all_hashtags_button"),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD946EF)),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Icon(
                        imageVector = CustomCopyIcon,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "نسخ كل الهاشتاجات 📋",
                        color = Color.White,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
fun ErrorStateCard(message: String, onRetry: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, Color(0xFFEF4444).copy(alpha = 0.5f), RoundedCornerShape(20.dp)),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B).copy(alpha = 0.8f)),
        shape = RoundedCornerShape(20.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = Icons.Default.Warning,
                contentDescription = null,
                tint = Color(0xFFEF4444),
                modifier = Modifier.size(36.dp)
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = "تعذر توليد الهاشتاجات بالكامل",
                color = Color.White,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = message,
                color = Color(0xFFF87171),
                fontSize = 12.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = onRetry,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF475569)),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(text = "محاولة أخرى 🔄", color = Color.White, fontSize = 12.sp)
            }
        }
    }
}

@Composable
fun SavedHashtagItemRow(
    savedSet: SavedHashtagSet,
    onCopy: () -> Unit,
    onDelete: () -> Unit
) {
    val df = remember { SimpleDateFormat("MM-dd HH:mm", Locale.US) }
    val timeFormatted = remember(savedSet.timestamp) { df.format(Date(savedSet.timestamp)) }
    var expanded by remember { mutableStateOf(false) }

    val platformColor = when (savedSet.platform) {
        "Instagram" -> Color(0xFFEC4899)
        "TikTok" -> Color(0xFF06B6D4)
        "X" -> Color(0xFFFFFFFF)
        "LinkedIn" -> Color(0xFF2563EB)
        else -> Color(0xFF8B5CF6)
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { expanded = !expanded }
            .border(1.dp, Color(0xFF334155).copy(alpha = 0.5f), RoundedCornerShape(16.dp)),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B).copy(alpha = 0.6f)),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Row metadata
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Time on left
                Text(
                    text = timeFormatted,
                    color = Color(0xFF64748B),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium
                )

                // Platform, count labels on right
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(platformColor.copy(alpha = 0.15f))
                            .padding(horizontal = 8.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = when (savedSet.platform) {
                                "Instagram" -> "إنستغرام"
                                "TikTok" -> "تيك توك"
                                "X" -> "إكس"
                                "LinkedIn" -> "لينكد إن"
                                else -> savedSet.platform
                            },
                            color = platformColor,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(Color(0xFF334155))
                            .padding(horizontal = 8.dp, vertical = 2.dp)
                    ) {
                        val count = remember(savedSet.hashtags) {
                            savedSet.hashtags.split(Regex("\\s+")).filter { it.isNotEmpty() }.size
                        }
                        Text(
                            text = "$count هاشتاج",
                            color = Color(0xFFE2E8F0),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }

            // Subject / Title description
            Text(
                text = savedSet.title,
                color = Color.White,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Right,
                modifier = Modifier.fillMaxWidth(),
                maxLines = if (expanded) Int.MAX_VALUE else 1
            )

            // Hashtags body content
            AnimatedVisibility(visible = expanded || savedSet.hashtags.isNotEmpty()) {
                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = savedSet.hashtags,
                        color = Color(0xFF38BDF8),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Normal,
                        textAlign = TextAlign.Right,
                        modifier = Modifier.fillMaxWidth()
                    )

                    if (savedSet.tips.isNotEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color(0xFF0F172A), RoundedCornerShape(8.dp))
                                .padding(8.dp)
                        ) {
                            Text(
                                text = "نصيحة: ${savedSet.tips}",
                                color = Color(0xFF94A3B8),
                                fontSize = 11.sp,
                                textAlign = TextAlign.Right,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        // Delete Button
                        IconButton(
                            onClick = onDelete,
                            modifier = Modifier
                                .size(36.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color(0xFFEF4444).copy(alpha = 0.1f))
                        ) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = "Delete",
                                tint = Color(0xFFF87171),
                                modifier = Modifier.size(16.dp)
                            )
                        }

                        // Copy Button
                        Button(
                            onClick = onCopy,
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF059669)),
                            modifier = Modifier
                                .weight(1f)
                                .height(36.dp),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Icon(
                                imageVector = CustomCopyIcon,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(text = "نسخ مجمل الرقم 📋", color = Color.White, fontSize = 12.sp)
                        }
                    }
                }
            }
        }
    }
}
