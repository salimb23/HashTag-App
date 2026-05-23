package com.example.data.repository

import android.util.Log
import com.example.BuildConfig
import com.example.data.api.Content
import com.example.data.api.GenerateContentRequest
import com.example.data.api.GenerationConfig
import com.example.data.api.Part
import com.example.data.api.ParsedHashtagsResponse
import com.example.data.api.RetrofitClient
import com.example.data.db.SavedHashtagSet
import com.example.data.db.SavedHashtagsDao
import kotlinx.coroutines.flow.Flow
import org.json.JSONObject

class HashtagRepository(private val savedHashtagsDao: SavedHashtagsDao) {

    val allSavedHashtags: Flow<List<SavedHashtagSet>> = savedHashtagsDao.getAllSavedHashtags()

    suspend fun saveHashtagSet(title: String, platform: String, hashtags: String, category: String, reachScore: String, tips: String) {
        val entry = SavedHashtagSet(
            title = title,
            platform = platform,
            hashtags = hashtags,
            category = category,
            reachScore = reachScore,
            tips = tips
        )
        savedHashtagsDao.insertHashtagSet(entry)
    }

    suspend fun deleteHashtagSet(set: SavedHashtagSet) {
        savedHashtagsDao.deleteHashtagSet(set)
    }

    suspend fun deleteHashtagSetById(id: Int) {
        savedHashtagsDao.deleteHashtagSetById(id)
    }

    /**
     * Generates hashtags using Gemini API with auto JSON parsing.
     * Falls back to high-quality local analyzer if anything goes wrong or if API key is missing.
     */
    suspend fun generateHashtags(description: String, platform: String): ParsedHashtagsResponse {
        val apiKey = BuildConfig.GEMINI_API_KEY
        
        if (apiKey.isEmpty() || apiKey == "MY_GEMINI_API_KEY") {
            Log.w("HashtagRepository", "API Key is missing or placeholder. Using offline fallback analyzer.")
            return generateOfflineHashtags(description, platform, "يرجى تهيئة مفتاح API في لوحة الأسرار لتفعيل الذكاء الاصطناعي بالكامل ✨")
        }

        val prompt = """
        قم بتوليد هاشتاجات مخصصة ومحسنة تسويقياً للوصف التالي باللغة العربية والإنجليزية حسب الملاءمة.
        الوصف: "$description"
        المنصة المستهدفة: "$platform"

        يجب أن يكون ردك بصيغة JSON حية وصحيحة تماماً ومباشرة، ومغلقة بالطبع بكائن رئيسي بالبنية التالية حصراً:
        {
          "hashtags": ["#هاشتاج1", "#هاشتاج2", "#هاشتاج3", ...],
          "platform": "$platform",
          "category": "تصنيف الموضوع الأساسي (مثال: سياحة، تكنولوجيا، طبخ)",
          "reachScore": "الحجم المتوقع للوصول (عالي جداً 🔥 / عالي ⚡ / متوسط 📈)",
          "tips": "اكتب هنا 1-2 من النصائح التسويقية المخصصة جداً لنشر هذا المحتوى على هذه المنصة بالتحديد لضمان الرواج والوصول."
        }

        مهم جداً: لا تضف أي نصوص توضيحية أو مقدمات أو مؤشرات شيفرة برمجية مثل ```json قبل أو بعد الـ JSON. أرسل كائن الـ JSON مباشرة فقط.
        """.trimIndent()

        val request = GenerateContentRequest(
            contents = listOf(Content(parts = listOf(Part(text = prompt)))),
            generationConfig = GenerationConfig(
                responseMimeType = "application/json",
                temperature = 0.7f
            ),
            systemInstruction = Content(parts = listOf(Part(text = "أنت خبير تسويق رقمي ومحترف إدارة شبكات التواصل الاجتماعي ومختص بزيادة تفاعل المنشورات عن طريق الهاشتاجات الذكية المستهدفة والتريند لعام 2026.")))
        )

        return try {
            val response = RetrofitClient.service.generateContent(apiKey, request)
            val jsonText = response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text
            
            if (!jsonText.isNullOrEmpty()) {
                parseJsonTags(jsonText, platform)
            } else {
                generateOfflineHashtags(description, platform, "فشل خادم الذكاء الاصطناعي في الاستجابة بالنص المطلوب.")
            }
        } catch (e: Exception) {
            Log.e("HashtagRepository", "Gemini API failed: ${e.message}", e)
            generateOfflineHashtags(description, platform, "حدث خطأ بالاتصال بالذكاء الاصطناعي: ${e.localizedMessage}")
        }
    }

    private fun parseJsonTags(jsonText: String, defaultPlatform: String): ParsedHashtagsResponse {
        return try {
            // Clean markdown blocks if Gemini accidentally included them
            val cleaned = jsonText.trim()
                .removePrefix("```json")
                .removePrefix("```")
                .removeSuffix("```")
                .trim()
            
            val jsonObject = JSONObject(cleaned)
            val jsonArray = jsonObject.getJSONArray("hashtags")
            val tagsList = mutableListOf<String>()
            for (i in 0 until jsonArray.length()) {
                var tag = jsonArray.getString(i).trim()
                if (!tag.startsWith("#")) {
                    tag = "#$tag"
                }
                tagsList.add(tag)
            }

            ParsedHashtagsResponse(
                hashtags = tagsList,
                platform = jsonObject.optString("platform", defaultPlatform),
                category = jsonObject.optString("category", "عام"),
                reachScore = jsonObject.optString("reachScore", "متوسط 📈"),
                tips = jsonObject.optString("tips", "احرص على إضافة محتوى مرئي جذاب لزيادة الرواج.")
            )
        } catch (e: Exception) {
            Log.e("HashtagRepository", "JSON parsing failed for: $jsonText", e)
            // Extract from raw using regex fallback
            val hashtags = extractHashtagsFromRawText(jsonText)
            if (hashtags.isNotEmpty()) {
                ParsedHashtagsResponse(
                    hashtags = hashtags,
                    platform = defaultPlatform,
                    category = "ذكية وعامة",
                    reachScore = "متوسط 📈",
                    tips = "تم استخراج الهاشتاجات تلقائياً من الرد الأولي لتعذر تحليل هيكل البيانات."
                )
            } else {
                generateOfflineHashtags("فشل تنظيم الاستجابة", defaultPlatform, "تعذر معالجة الرد من الذكاء الاصطناعي بصيغة JSON.")
            }
        }
    }

    private fun extractHashtagsFromRawText(text: String): List<String> {
        val regex = Regex("#[\\p{L}\\p{N}_]+")
        return regex.findAll(text).map { it.value }.toList()
    }

    /**
     * Outstanding offline hashtag generator that analyzes semantic words to produce local tags.
     */
    private fun generateOfflineHashtags(description: String, platform: String, errorTip: String): ParsedHashtagsResponse {
        val wordList = description.split(Regex("[\\s,،.?!_()\\-\"]+"))
            .filter { it.length > 2 }
            .distinct()

        val generatedTags = mutableListOf<String>()
        var detectedCategory = "عام"

        // Local semantic analysis for Arabic and English themes
        val keywordMaps = mapOf(
            "سياحة" to listOf("سفر", "سياحة", "فندق", "رحلة", "دبي", "طيران", "بحر", "جواز", "travel", "tourism", "hotel", "beach", "dubai"),
            "طبخ وأطعمة" to listOf("طبخ", "حلويات", "وصفة", "أكل", "مطعم", "شيف", "لذيذ", "عشاء", "food", "chef", "recipe", "cooking", "delicious"),
            "برمجة وتقنية" to listOf("تطبيق", "برمجة", "تقنية", "كود", "هاتف", "ذكاء", "موقع", "كمبيوتر", "tech", "coding", "developer", "ai", "app"),
            "رياضة وصحة" to listOf("رياضة", "جيم", "لياقة", "صحة", "جري", "تمرين", "نادي", "sport", "gym", "fitness", "health", "workout"),
            "عقارات واستثمار" to listOf("عقار", "شقة", "استثمار", "شراء", "بيع", "فيلا", "منزل", "realestate", "apartment", "invest", "villa"),
            "تسويق وأعمال" to listOf("تسويق", "عمل", "أرباح", "شركة", "مشروع", "تجارة", "بيزنس", "marketing", "business", "money", "project")
        )

        for (word in wordList) {
            val lowercaseWord = word.lowercase()
            for ((category, keywords) in keywordMaps) {
                if (keywords.any { lowercaseWord.contains(it) }) {
                    detectedCategory = category
                    if (generatedTags.size < 12) {
                        generatedTags.add("#$word")
                    }
                }
            }
        }

        // Add standard generic social tags based on platform
        val platformTags = when (platform.lowercase()) {
            "instagram" -> listOf("#انستقرام", "#اكسبلور", "#لايك", "#تصويري", "#instagram", "#explore", "#photooftheday")
            "tiktok" -> listOf("#تيك_توك", "#تريند", "#فولو", "#عرب", "#tiktok", "#trending", "#fyp", "#foryou")
            "x", "twitter" -> listOf("#اليوم", "#عاجل", "#تويتر", "#خبر", "#trending", "#news", "#twitter")
            "linkedin" -> listOf("#وظائف", "#تطوير_الذات", "#عمل", "#شركات", "#career", "#networking", "#motivation")
            else -> listOf("#اكسبلور", "#عرب", "#جديد", "#تريند", "#explore", "#trending", "#viral")
        }
        
        generatedTags.addAll(platformTags)
        
        // Ensure unique, clean hashtags, max 15
        val finalTags = generatedTags.distinct()
            .map { it.replace("#+", "#") }
            .take(15)

        return ParsedHashtagsResponse(
            hashtags = finalTags,
            platform = platform,
            category = "$detectedCategory (محلي 🛠️)",
            reachScore = "متوسط 📈",
            tips = "$errorTip\nنصيحة تلقائية: الهاشتاجات المحلية مستخرجة من النص الخاص بك. لنتائج ذكية وتحليل عميق، تفضل بتهيئة مفتاح API."
        )
    }
}
