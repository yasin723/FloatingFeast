package com.floatingfeast.game

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.floatingfeast.game.ui.theme.FloatingFeastTheme

private val gold = Color(0xFFE9BD72)
private val lightGold = Color(0xFFFFE5B2)
private val navy = Color(0xFF071E32)
private val cream = Color(0xFFFFF0D5)
private val muted = Color(0xFFB8D1D8)

private val foodList = listOf(
    "Grilled Salmon",
    "Seafood Pasta",
    "Fish Tacos",
    "Lobster Roll",
    "Fresh Lemonade",
    "Iced Coffee"
)

private val foodEmoji = mapOf(
    "Grilled Salmon" to "🐟",
    "Seafood Pasta" to "🍝",
    "Fish Tacos" to "🌮",
    "Lobster Roll" to "🦞",
    "Fresh Lemonade" to "🍋",
    "Iced Coffee" to "🧋"
)

private val foodImage = mapOf(
    "Grilled Salmon" to R.drawable.food_salmon,
    "Seafood Pasta" to R.drawable.food_pasta,
    "Fish Tacos" to R.drawable.food_tacos,
    "Lobster Roll" to R.drawable.food_lobster,
    "Fresh Lemonade" to R.drawable.drink_lemonade,
    "Iced Coffee" to R.drawable.drink_coffee
)

private val guestImage = mapOf(
    1 to R.drawable.guest_1,
    2 to R.drawable.guest_2,
    3 to R.drawable.guest_3
)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            FloatingFeastTheme {
                FloatingFeastGame()
            }
        }
    }
}

@Composable
fun FloatingFeastGame() {

    val context = LocalContext.current
    val profileStore = remember {
        context.getSharedPreferences("floating_feast_profile", android.content.Context.MODE_PRIVATE)
    }

    var nickname by remember {
        mutableStateOf(profileStore.getString("nickname", "") ?: "")
    }
    var selectedAvatar by remember {
        mutableIntStateOf(profileStore.getInt("avatar", 1))
    }
    val foodPreferences = remember {
        mutableStateListOf<String>().apply {
            val saved = profileStore.getStringSet("food_preferences", emptySet()) ?: emptySet()
            addAll(saved)
        }
    }
    var profileCreated by remember {
        mutableStateOf(profileStore.getBoolean("profile_created", false))
    }

    var gameStarted by remember { mutableStateOf(false) }
    var score by remember { mutableIntStateOf(0) }
    var lives by remember { mutableIntStateOf(5) }
    var combo by remember { mutableIntStateOf(0) }
    var wasteMeter by remember { mutableIntStateOf(0) }
    var selectedMeal by remember { mutableStateOf<String?>(null) }
    var gameOver by remember { mutableStateOf(false) }

    var message by remember {
        mutableStateOf("Welcome aboard! Select a meal.")
    }

    val orders = remember {
        mutableStateListOf(
            foodList[0],
            foodList[2],
            foodList[4]
        )
    }

    fun newOrder(): String = foodList.random()

    fun resetGame() {
        score = 0
        lives = 5
        combo = 0
        wasteMeter = 0
        selectedMeal = null
        orders[0] = newOrder()
        orders[1] = newOrder()
        orders[2] = newOrder()
        message = "Welcome aboard! Select a meal."
        gameOver = false
    }

    fun serveCustomer(index: Int) {
        val meal = selectedMeal

        if (meal == null) {
            message = "Please select a meal first!"
            return
        }

        if (meal == orders[index]) {
            combo = (combo + 1).coerceAtMost(5)
            val earned = 10 + combo * 2
            score += earned
            wasteMeter = (wasteMeter - 5).coerceAtLeast(0)
            orders[index] = newOrder()
            message = "Perfect service! +$earned points"
        } else {
            score = (score - 5).coerceAtLeast(0)
            lives--
            combo = 0
            wasteMeter = (wasteMeter + 15).coerceAtMost(100)
            message = "Wrong order! -5 points. Food waste increased!"

            if (lives <= 0) {
                gameOver = true
            }
        }

        selectedMeal = null
    }

    Box(modifier = Modifier.fillMaxSize()) {

        // YACHT RESTAURANT BACKGROUND
        Image(
            painter = painterResource(
                id = R.drawable.yacht_restaurant_bg
            ),
            contentDescription = "Luxury yacht restaurant",
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )

        // DARK OVERLAY FOR TEXT READABILITY
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color(0x99051B2B),
                            Color(0x77051B2B),
                            Color(0xAA051B2B)
                        )
                    )
                )
        )

        // PLAYER REGISTRATION
        if (!profileCreated) {
            PlayerRegistrationScreen(
                nickname = nickname,
                onNicknameChange = { nickname = it },
                selectedAvatar = selectedAvatar,
                onAvatarSelected = { selectedAvatar = it },
                foodPreferences = foodPreferences,
                onPreferenceChanged = { preference, checked ->
                    if (checked && preference !in foodPreferences) {
                        foodPreferences.add(preference)
                    } else if (!checked) {
                        foodPreferences.remove(preference)
                    }
                },
                onContinue = {
                    profileStore.edit()
                        .putString("nickname", nickname.trim())
                        .putInt("avatar", selectedAvatar)
                        .putStringSet("food_preferences", foodPreferences.toSet())
                        .putBoolean("profile_created", true)
                        .apply()
                    profileCreated = true
                }
            )
        }

        // MAIN MENU
        else if (!gameStarted) {

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(25.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.SpaceEvenly
            ) {

                Text(
                    "⚓ FLOATING FEAST",
                    color = gold,
                    fontSize = 28.sp,
                    fontWeight = FontWeight.ExtraBold
                )

                Image(
                    painter = painterResource(
                        id = guestImage[selectedAvatar] ?: R.drawable.guest_1
                    ),
                    contentDescription = "Player avatar",
                    modifier = Modifier.size(76.dp),
                    contentScale = ContentScale.Fit
                )

                Text(
                    "WELCOME ABOARD, ${nickname.uppercase()}",
                    color = lightGold,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("🌊", fontSize = 34.sp)
                    Text("⛵", fontSize = 100.sp)

                    Text(
                        "DINING ON THE SEA",
                        color = Color.White,
                        fontSize = 21.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(Modifier.height(10.dp))

                    Text(
                        "An exclusive dining experience awaits.",
                        color = muted,
                        fontSize = 14.sp,
                        textAlign = TextAlign.Center
                    )
                }

                Button(
                    onClick = { gameStarted = true },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(58.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = gold,
                        contentColor = navy
                    )
                ) {
                    Text(
                        "START GAME  →",
                        fontSize = 17.sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                }

                Text(
                    "A LUXURY DINING ADVENTURE",
                    color = lightGold,
                    fontSize = 11.sp,
                    letterSpacing = 2.sp
                )
            }
        }

        // GAME OVER
        else if (gameOver) {

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(25.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {

                Text("⚓", fontSize = 65.sp)

                Text(
                    "SERVICE COMPLETED",
                    color = gold,
                    fontSize = 27.sp,
                    fontWeight = FontWeight.ExtraBold,
                    textAlign = TextAlign.Center
                )

                Spacer(Modifier.height(18.dp))

                Text(
                    "FINAL SCORE",
                    color = muted,
                    fontSize = 13.sp,
                    letterSpacing = 2.sp
                )

                Text(
                    "$score",
                    color = Color.White,
                    fontSize = 42.sp,
                    fontWeight = FontWeight.ExtraBold
                )

                Spacer(Modifier.height(30.dp))

                Button(
                    onClick = { resetGame() },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = gold,
                        contentColor = navy
                    )
                ) {
                    Text(
                        "PLAY AGAIN",
                        fontWeight = FontWeight.Bold
                    )
                }

                TextButton(
                    onClick = {
                        gameStarted = false
                        gameOver = false
                    }
                ) {
                    Text("MAIN MENU", color = lightGold)
                }
            }
        }

        // GAME SCREEN
        else {

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(
                        horizontal = 14.dp,
                        vertical = 12.dp
                    ),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {

                Text(
                    "⚓ FLOATING FEAST",
                    color = gold,
                    fontSize = 23.sp,
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = 1.sp
                )

                Text(
                    "LUXURY YACHT RESTAURANT",
                    color = Color.White,
                    fontSize = 9.sp,
                    letterSpacing = 2.sp
                )

                Spacer(Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    StatBox("SCORE", "$score", "🏆", Modifier.weight(1f))
                    StatBox("LIVES", "$lives / 5", "❤️", Modifier.weight(1f))
                    StatBox("COMBO", "x$combo", "🔥", Modifier.weight(1f))
                }

                Spacer(Modifier.height(20.dp))

                Text(
                    "🌊  WELCOME TO THE DECK  🌊",
                    color = Color.White,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )

                Spacer(Modifier.height(5.dp))

                Text(
                    "Your guests are waiting for their orders",
                    color = Color.White,
                    fontSize = 11.sp
                )

                Spacer(Modifier.height(16.dp))

                // FOOD WASTE METER
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(Color(0xDD10394B))
                        .border(1.dp, Color(0xFF7897A0), RoundedCornerShape(14.dp))
                        .padding(14.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("♻ FOOD WASTE METER", color = lightGold, fontSize = 13.sp, fontWeight = FontWeight.ExtraBold)
                        Text("$wasteMeter / 100", color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                    }
                    Spacer(Modifier.height(9.dp))
                    LinearProgressIndicator(
                        progress = { wasteMeter / 100f },
                        modifier = Modifier.fillMaxWidth().height(10.dp).clip(RoundedCornerShape(10.dp)),
                        color = when {
                            wasteMeter >= 70 -> Color(0xFFE45B5B)
                            wasteMeter >= 40 -> Color(0xFFFFB74D)
                            else -> Color(0xFF71C99A)
                        },
                        trackColor = Color(0xFF365968)
                    )
                    Spacer(Modifier.height(7.dp))
                    Text(
                        text = when {
                            wasteMeter >= 70 -> "High waste! Be more careful with orders."
                            wasteMeter >= 40 -> "Moderate waste. Improve your service."
                            wasteMeter > 0 -> "Good progress! Keep reducing food waste."
                            else -> "Excellent! No unnecessary food waste."
                        },
                        color = Color.White,
                        fontSize = 10.sp
                    )
                }

                Spacer(Modifier.height(16.dp))

                SectionTitle("CUSTOMER ORDERS", "01")

                Spacer(Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    orders.forEachIndexed { index, order ->
                        CustomerCard(
                            number = index + 1,
                            order = order,
                            onClick = { serveCustomer(index) },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                Spacer(Modifier.height(14.dp))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .shadow(5.dp, RoundedCornerShape(12.dp))
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xDD10394B))
                        .border(
                            1.dp,
                            Color(0xFF7897A0),
                            RoundedCornerShape(12.dp)
                        )
                        .padding(13.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        message,
                        color = if (message.startsWith("Wrong")) {
                            lightGold
                        } else {
                            Color.White
                        },
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        textAlign = TextAlign.Center
                    )
                }

                Spacer(Modifier.height(18.dp))

                SectionTitle("SELECT A MEAL", "02")

                Spacer(Modifier.height(10.dp))

                foodList.chunked(2).forEach { row ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(9.dp)
                    ) {
                        row.forEach { food ->
                            FoodButton(
                                food = food,
                                selected = selectedMeal == food,
                                onClick = {
                                    selectedMeal = food
                                    message =
                                        "${foodEmoji[food]} $food selected. Choose a guest!"
                                },
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }

                    Spacer(Modifier.height(9.dp))
                }

                Spacer(Modifier.height(10.dp))

                Text(
                    "SELECT A DISH  •  SERVE THE RIGHT GUEST",
                    color = Color.White,
                    fontSize = 9.sp,
                    letterSpacing = 1.sp,
                    textAlign = TextAlign.Center
                )

                Spacer(Modifier.height(12.dp))

                TextButton(
                    onClick = {
                        gameStarted = false
                        gameOver = false
                    }
                ) {
                    Text(
                        "← EXIT TO MAIN MENU",
                        color = lightGold,
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp,
                        letterSpacing = 1.sp
                    )
                }
            }
        }
    }
}

@Composable
fun PlayerRegistrationScreen(
    nickname: String,
    onNicknameChange: (String) -> Unit,
    selectedAvatar: Int,
    onAvatarSelected: (Int) -> Unit,
    foodPreferences: List<String>,
    onPreferenceChanged: (String, Boolean) -> Unit,
    onContinue: () -> Unit
) {
    val canContinue = nickname.trim().length >= 2

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 22.dp, vertical = 18.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            "⚓ FLOATING FEAST",
            color = gold,
            fontSize = 25.sp,
            fontWeight = FontWeight.ExtraBold,
            letterSpacing = 1.sp
        )

        Spacer(Modifier.height(6.dp))

        Text(
            "CREATE YOUR PLAYER PROFILE",
            color = Color.White,
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.sp
        )

        Text(
            "Join the crew and help us waste less food.",
            color = muted,
            fontSize = 11.sp,
            textAlign = TextAlign.Center
        )

        Spacer(Modifier.height(20.dp))

        Text(
            "CHOOSE YOUR AVATAR",
            color = gold,
            fontSize = 13.sp,
            fontWeight = FontWeight.ExtraBold
        )

        Spacer(Modifier.height(10.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            (1..3).forEach { avatar ->
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(14.dp))
                        .background(
                            if (selectedAvatar == avatar) Color(0xFFE9BD72)
                            else Color(0xCC10394B)
                        )
                        .border(
                            width = if (selectedAvatar == avatar) 2.dp else 1.dp,
                            color = if (selectedAvatar == avatar) Color.White else Color(0xFF7897A0),
                            shape = RoundedCornerShape(14.dp)
                        )
                        .clickable { onAvatarSelected(avatar) }
                        .padding(vertical = 9.dp, horizontal = 4.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Image(
                        painter = painterResource(
                            id = guestImage[avatar] ?: R.drawable.guest_1
                        ),
                        contentDescription = "Avatar $avatar",
                        modifier = Modifier.size(66.dp),
                        contentScale = ContentScale.Fit
                    )

                    Text(
                        when (avatar) {
                            1 -> "ALEX"
                            2 -> "SOPHIA"
                            else -> "JAMES"
                        },
                        color = if (selectedAvatar == avatar) navy else Color.White,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.ExtraBold
                    )

                    if (selectedAvatar == avatar) {
                        Text(
                            "SELECTED ✓",
                            color = navy,
                            fontSize = 8.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        Spacer(Modifier.height(18.dp))

        Text(
            "YOUR NICKNAME",
            modifier = Modifier.fillMaxWidth(),
            color = gold,
            fontSize = 12.sp,
            fontWeight = FontWeight.ExtraBold
        )

        Spacer(Modifier.height(6.dp))

        OutlinedTextField(
            value = nickname,
            onValueChange = { if (it.length <= 16) onNicknameChange(it) },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            placeholder = { Text("Enter your nickname") },
            keyboardOptions = KeyboardOptions(
                capitalization = KeyboardCapitalization.Words
            ),
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White,
                focusedBorderColor = gold,
                unfocusedBorderColor = Color(0xFF7897A0),
                focusedLabelColor = gold,
                cursorColor = gold,
                focusedContainerColor = Color(0xAA10394B),
                unfocusedContainerColor = Color(0xAA10394B)
            )
        )

        Spacer(Modifier.height(16.dp))

        Text(
            "FOOD PREFERENCES",
            modifier = Modifier.fillMaxWidth(),
            color = gold,
            fontSize = 12.sp,
            fontWeight = FontWeight.ExtraBold
        )

        Text(
            "Choose any preferences that describe you.",
            modifier = Modifier.fillMaxWidth(),
            color = muted,
            fontSize = 10.sp
        )

        val preferences = listOf(
            "Seafood lover",
            "Healthy meals",
            "Vegetarian"
        )

        preferences.forEach { preference ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(Color(0xAA10394B))
                    .padding(horizontal = 8.dp, vertical = 2.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Checkbox(
                    checked = preference in foodPreferences,
                    onCheckedChange = { onPreferenceChanged(preference, it) },
                    colors = CheckboxDefaults.colors(
                        checkedColor = gold,
                        uncheckedColor = lightGold,
                        checkmarkColor = navy
                    )
                )
                Text(
                    preference,
                    color = Color.White,
                    fontSize = 12.sp
                )
            }
        }

        Spacer(Modifier.height(18.dp))

        Button(
            onClick = onContinue,
            enabled = canContinue,
            modifier = Modifier
                .fillMaxWidth()
                .height(54.dp),
            shape = RoundedCornerShape(14.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = gold,
                contentColor = navy,
                disabledContainerColor = Color(0xFF777777),
                disabledContentColor = Color(0xFFDDDDDD)
            )
        ) {
            Text(
                "BOARD THE YACHT  →",
                fontSize = 14.sp,
                fontWeight = FontWeight.ExtraBold
            )
        }

        if (!canContinue) {
            Spacer(Modifier.height(5.dp))
            Text(
                "Enter at least 2 characters to continue.",
                color = lightGold,
                fontSize = 10.sp
            )
        }
    }
}

@Composable
fun SectionTitle(
    title: String,
    number: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            number,
            color = gold,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold
        )

        Spacer(Modifier.width(8.dp))

        Text(
            title,
            color = gold,
            fontSize = 16.sp,
            fontWeight = FontWeight.ExtraBold,
            letterSpacing = 1.sp
        )

        Spacer(Modifier.width(8.dp))

        Box(
            modifier = Modifier
                .weight(1f)
                .height(1.dp)
                .background(Color(0xFFB8A17D))
        )
    }
}

@Composable
fun StatBox(
    title: String,
    value: String,
    emoji: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .shadow(5.dp, RoundedCornerShape(14.dp))
            .clip(RoundedCornerShape(14.dp))
            .background(Color(0xDD10394B))
            .border(
                1.dp,
                Color(0xFF7897A0),
                RoundedCornerShape(14.dp)
            )
            .padding(vertical = 10.dp, horizontal = 4.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            title,
            color = muted,
            fontSize = 9.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.sp
        )

        Spacer(Modifier.height(4.dp))

        Text(emoji, fontSize = 17.sp)

        Text(
            value,
            color = lightGold,
            fontSize = 15.sp,
            fontWeight = FontWeight.ExtraBold
        )
    }
}

@Composable
fun CustomerCard(
    number: Int,
    order: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .shadow(6.dp, RoundedCornerShape(16.dp))
            .clip(RoundedCornerShape(16.dp))
            .background(
                Brush.verticalGradient(
                    listOf(
                        Color(0xFFFFF2D9),
                        Color(0xFFEAD6AE)
                    )
                )
            )
            .border(
                1.dp,
                Color(0xFFFFE8BC),
                RoundedCornerShape(16.dp)
            )
            .clickable { onClick() }
            .padding(vertical = 9.dp, horizontal = 3.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Image(
            painter = painterResource(
                id = guestImage[number] ?: R.drawable.guest_1
            ),
            contentDescription = "Guest $number",
            modifier = Modifier.size(65.dp),
            contentScale = ContentScale.Fit
        )

        Text(
            "GUEST $number",
            color = navy,
            fontSize = 9.sp,
            fontWeight = FontWeight.ExtraBold
        )

        Spacer(Modifier.height(5.dp))

        Image(
            painter = painterResource(
                id = foodImage[order] ?: R.drawable.food_salmon
            ),
            contentDescription = order,
            modifier = Modifier.size(48.dp),
            contentScale = ContentScale.Fit
        )

        Text(
            order,
            color = navy,
            fontSize = 9.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
            lineHeight = 11.sp,
            minLines = 2
        )

        Spacer(Modifier.height(7.dp))

        Text(
            "TAP TO SERVE",
            color = Color(0xFF96602D),
            fontSize = 8.sp,
            fontWeight = FontWeight.ExtraBold
        )
    }
}

@Composable
fun FoodButton(
    food: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val backgroundColor = if (selected) gold else cream

    Column(
        modifier = modifier
            .height(88.dp)
            .shadow(
                elevation = if (selected) 7.dp else 3.dp,
                shape = RoundedCornerShape(15.dp)
            )
            .clip(RoundedCornerShape(15.dp))
            .background(backgroundColor)
            .border(
                width = if (selected) 2.dp else 1.dp,
                color = if (selected) lightGold else Color(0xFFFFE9C4),
                shape = RoundedCornerShape(15.dp)
            )
            .clickable { onClick() }
            .padding(5.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Image(
            painter = painterResource(
                id = foodImage[food] ?: R.drawable.food_salmon
            ),
            contentDescription = food,
            modifier = Modifier.size(53.dp),
            contentScale = ContentScale.Fit
        )

        Text(
            food,
            color = navy,
            fontSize = 10.sp,
            fontWeight = FontWeight.ExtraBold,
            textAlign = TextAlign.Center,
            lineHeight = 12.sp
        )

        if (selected) {
            Text(
                "SELECTED ✓",
                color = Color(0xFF604018),
                fontSize = 8.sp,
                fontWeight = FontWeight.ExtraBold
            )
        }
    }
}