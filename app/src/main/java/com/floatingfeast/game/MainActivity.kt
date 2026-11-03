package com.floatingfeast.game

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.floatingfeast.game.ui.theme.FloatingFeastTheme

private val gold = Color(0xFFFFD27A)
private val navy = Color(0xFF092F49)
private val sea = Color(0xFF12647A)
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
    var gameStarted by remember { mutableStateOf(false) }
    var score by remember { mutableIntStateOf(0) }
    var lives by remember { mutableIntStateOf(5) }
    var combo by remember { mutableIntStateOf(0) }
    var selectedMeal by remember { mutableStateOf<String?>(null) }
    var message by remember { mutableStateOf("Welcome aboard! Select a meal.") }
    var gameOver by remember { mutableStateOf(false) }

    val orders = remember {
        mutableStateListOf(
            foodList[0],
            foodList[2],
            foodList[4]
        )
    }

    fun newOrder(): String {
        return foodList.random()
    }

    fun serveCustomer(index: Int) {
        val meal = selectedMeal
        if (meal == null) {
            message = "Select a meal first!"
            return
        }

        if (meal == orders[index]) {
            combo = (combo + 1).coerceAtMost(5)
            score += 10 + combo * 2
            orders[index] = newOrder()
            message = "Correct order! Great service! +${10 + combo * 2} points"
        } else {
            score = (score - 5).coerceAtLeast(0)
            lives--
            combo = 0
            message = "Wrong order! Try again. -5 points"
            if (lives <= 0) {
                gameOver = true
            }
        }
        selectedMeal = null
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(navy, sea, Color(0xFF16475A))
                )
            )
    ) {
        if (!gameStarted) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.SpaceEvenly
            ) {
                Text("⚓ FLOATING FEAST", color = gold,
                    fontSize = 29.sp, fontWeight = FontWeight.ExtraBold)

                Text("⛵", fontSize = 105.sp)

                Text("DINING ON THE SEA", color = Color.White,
                    fontSize = 21.sp, fontWeight = FontWeight.Bold)

                Text("Serve guests on your floating restaurant!",
                    color = Color.White, textAlign = TextAlign.Center)

                Button(
                    onClick = { gameStarted = true },
                    modifier = Modifier.fillMaxWidth().height(58.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = gold,
                        contentColor = navy
                    )
                ) {
                    Text("START GAME", fontSize = 19.sp,
                        fontWeight = FontWeight.Bold)
                }

                Text("A LUXURY DINING ADVENTURE",
                    color = gold, fontSize = 12.sp)
            }
        } else if (gameOver) {
            Column(
                modifier = Modifier.fillMaxSize().padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text("GAME OVER", color = gold,
                    fontSize = 34.sp, fontWeight = FontWeight.ExtraBold)
                Text("Final Score: $score", color = Color.White,
                    fontSize = 24.sp)
                Spacer(Modifier.height(24.dp))
                Button(onClick = {
                    score = 0
                    lives = 5
                    combo = 0
                    selectedMeal = null
                    orders[0] = newOrder()
                    orders[1] = newOrder()
                    orders[2] = newOrder()
                    message = "Welcome aboard! Select a meal."
                    gameOver = false
                }) {
                    Text("PLAY AGAIN")
                }
                TextButton(onClick = { gameStarted = false }) {
                    Text("MAIN MENU", color = gold)
                }
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 12.dp, vertical = 10.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("⚓ FLOATING FEAST", color = gold,
                    fontSize = 23.sp, fontWeight = FontWeight.ExtraBold)

                Spacer(Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    StatBox("SCORE", "$score")
                    StatBox("LIVES", "❤️ $lives")
                    StatBox("COMBO", "🔥 x$combo")
                }

                Spacer(Modifier.height(12.dp))

                Text("🌊  WELCOME TO THE DECK  🌊",
                    color = Color.White, fontSize = 14.sp,
                    fontWeight = FontWeight.Bold)

                Spacer(Modifier.height(12.dp))

                Text("CUSTOMER ORDERS",
                    color = gold, fontSize = 17.sp,
                    fontWeight = FontWeight.Bold)

                Spacer(Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(7.dp)
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

                Text(
                    text = message,
                    color = Color.White,
                    fontSize = 13.sp,
                    textAlign = TextAlign.Center,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFF16475A), RoundedCornerShape(12.dp))
                        .padding(12.dp)
                )

                Spacer(Modifier.height(12.dp))

                Text("SELECT A MEAL",
                    color = gold, fontSize = 18.sp,
                    fontWeight = FontWeight.Bold)

                Spacer(Modifier.height(8.dp))

                for (row in foodList.chunked(2)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        row.forEach { food ->
                            FoodButton(
                                food = food,
                                selected = selectedMeal == food,
                                onClick = {
                                    selectedMeal = food
                                    message = "${foodEmoji[food]} $food selected. Tap a customer!"
                                },
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                    Spacer(Modifier.height(8.dp))
                }

                Spacer(Modifier.weight(1f))

                TextButton(onClick = {
                    gameStarted = false
                    gameOver = false
                }) {
                    Text("EXIT TO MAIN MENU", color = gold)
                }
            }
        }
    }
}

@Composable
fun StatBox(title: String, value: String) {
    Column(
        modifier = Modifier
            .background(Color(0xFF16475A), RoundedCornerShape(12.dp))
            .padding(horizontal = 12.dp, vertical = 9.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(title, color = gold, fontSize = 10.sp,
            fontWeight = FontWeight.Bold)
        Text(value, color = Color.White, fontSize = 16.sp,
            fontWeight = FontWeight.Bold)
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
            .background(Color(0xFFF4E5C5), RoundedCornerShape(14.dp))
            .clickable { onClick() }
            .padding(vertical = 12.dp, horizontal = 4.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("👤", fontSize = 35.sp)
        Text("GUEST $number", color = navy,
            fontSize = 10.sp, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(5.dp))
        Text(foodEmoji[order] ?: "🍽️", fontSize = 25.sp)
        Text(order, color = navy, fontSize = 10.sp,
            fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
        Spacer(Modifier.height(5.dp))
        Text("TAP TO SERVE", color = Color(0xFF9B5527),
            fontSize = 9.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
fun FoodButton(
    food: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Button(
        onClick = onClick,
        modifier = modifier.height(76.dp),
        shape = RoundedCornerShape(14.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = if (selected) gold else Color(0xFFF4E5C5),
            contentColor = navy
        ),
        contentPadding = PaddingValues(4.dp)
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(foodEmoji[food] ?: "🍽️", fontSize = 23.sp)
            Text(food, fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                lineHeight = 12.sp)
        }
    }
}
