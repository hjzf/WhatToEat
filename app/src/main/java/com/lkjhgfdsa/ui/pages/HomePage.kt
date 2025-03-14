package com.lkjhgfdsa.ui.pages

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.VectorConverter
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.lkjhgfdsa.R
import com.lkjhgfdsa.ThisApplication.Companion.IMAGE_DIR
import com.lkjhgfdsa.logic.Repository
import com.lkjhgfdsa.logic.model.FoodType
import com.lkjhgfdsa.logic.model.Option
import com.lkjhgfdsa.logic.model.foodType
import com.lkjhgfdsa.ui.components.TitleBar
import com.lkjhgfdsa.ui.theme.LocalTheme
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.io.File
import kotlin.math.absoluteValue
import kotlin.math.roundToInt
import kotlin.random.Random

data class HomePageUiState(
    val messageText: String = "",
    val actionText: String = "",
    val option: Option = Option()
)

class HomePageViewModel : ViewModel() {

    private val _messageText = MutableStateFlow("")

    private val _actionText = MutableStateFlow("")

    private val _random = MutableStateFlow(0)

    private val _options = Repository.getOptionsFlow()

    fun showMessage(message: String, action: String) {
        _messageText.update { message }
        _actionText.update { action }
    }

    fun afterMessageShown() {
        _messageText.update { "" }
        _actionText.update { "" }
    }

    fun randomOption() {
        _random.update { it + 1 }
    }

    private fun insertDefaultData() {
        viewModelScope.launch {
            Repository.insertOptionsAsync(
                Option(
                    name = "example",
                    description = "ok",
                    type = FoodType.TYPE_1.value
                )
            )
        }
    }

    private val _currentOption = combine(_random, _options) { _, options ->
        if (options.isEmpty()) {
            insertDefaultData()
            Option()
        } else {
            options[Random(System.nanoTime()).nextInt(0, options.size)]
        }
    }

    val uiState = combine(
        _messageText, _actionText, _currentOption
    ) { messageText, actionText, currentOption ->
        HomePageUiState(
            messageText = messageText,
            actionText = actionText,
            option = currentOption
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000L),
        initialValue = HomePageUiState()
    )

}

@Composable
fun HomePage(
    goTo: (String) -> Unit,
    viewModel: HomePageViewModel = viewModel(),
    state: SnackbarHostState = remember { SnackbarHostState() }
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val theme = LocalTheme.current
    val context = LocalContext.current
    val imageFile = remember(uiState.option.image) {
        derivedStateOf {
            if (uiState.option.image.isEmpty()) {
                null
            } else {
                val file = File(context.filesDir, "$IMAGE_DIR/${uiState.option.image}")
                if (file.exists() && file.isFile && file.canRead()) {
                    file
                } else {
                    null
                }
            }
        }
    }
    Scaffold(
        modifier = Modifier
            .fillMaxWidth()
            .fillMaxHeight(),
        snackbarHost = {
            SnackbarHost(hostState = state) {
                Snackbar(
                    snackbarData = it,
                    containerColor = theme.card,
                    contentColor = theme.primaryTextColor,
                    actionColor = theme.tips
                )
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight()
                .background(color = theme.background)
                .padding(paddingValues = paddingValues)
        ) {
            TitleBar(
                imageResource = R.drawable.app_icon,
                textLeft = context.getString(R.string.app_what_to_eat),
                onImageClick = {
                    viewModel.showMessage(
                        context.getString(R.string.app_why_poking_me),
                        context.getString(R.string.app_sorry)
                    )
                },
                onTextLeftClick = {
                    viewModel.showMessage(
                        context.getString(R.string.app_answer_of_what_to_eat),
                        context.getString(R.string.app_ok)
                    )
                },
                textRight = context.getString(R.string.app_options),
                onTextRightClick = { goTo("/home/option-list") }
            )
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(0.dp)
                    .weight(1f)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(0.dp)
                        .weight(1f)
                        .background(color = theme.background)
                        .padding(top = 36.dp, bottom = 36.dp, start = 20.dp, end = 20.dp),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    val coroutineScope = rememberCoroutineScope()
                    val animatedOffset = remember {
                        Animatable(Offset.Zero, Offset.VectorConverter)
                    }
                    val rate = remember { mutableFloatStateOf(0.1f) }
                    val isDragging = remember { mutableStateOf(false) }
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .fillMaxHeight()
                            .offset {
                                IntOffset(
                                    animatedOffset.value.x.roundToInt(),
                                    animatedOffset.value.y.roundToInt()
                                )
                            }
                            .pointerInput(Unit) {
                                detectDragGestures(
                                    onDragStart = {
                                        isDragging.value = true
                                    },
                                    onDragEnd = {
                                        isDragging.value = false
                                        val screenWidth = size.width
                                        val screenHeight = size.height
                                        val x = animatedOffset.value.x
                                        val y = animatedOffset.value.y
                                        if (x.absoluteValue > screenWidth * rate.floatValue || y.absoluteValue > screenHeight * rate.floatValue) {
                                            val target = Offset(
                                                if (x > 0) screenWidth.toFloat() * 2 else -screenWidth.toFloat() * 2,
                                                if (y > 0) screenHeight.toFloat() * 2 else -screenHeight.toFloat() * 2
                                            )
                                            coroutineScope.launch {
                                                animatedOffset.animateTo(
                                                    target,
                                                    tween(
                                                        durationMillis = 500,
                                                        easing = LinearOutSlowInEasing
                                                    )
                                                )
                                                viewModel.randomOption()
                                                delay(200)
                                                animatedOffset.snapTo(Offset.Zero)
                                            }
                                        } else {
                                            coroutineScope.launch {
                                                animatedOffset.animateTo(
                                                    Offset.Zero,
                                                    spring(dampingRatio = 1f, stiffness = 1500f)
                                                )
                                            }
                                        }
                                    },
                                    onDragCancel = {
                                        isDragging.value = false
                                        val screenWidth = size.width
                                        val screenHeight = size.height
                                        val x = animatedOffset.value.x
                                        val y = animatedOffset.value.y
                                        if (x.absoluteValue > screenWidth * rate.floatValue || y.absoluteValue > screenHeight * rate.floatValue) {
                                            val target = Offset(
                                                if (x > 0) screenWidth.toFloat() * 2 else -screenWidth.toFloat() * 2,
                                                if (y > 0) screenHeight.toFloat() * 2 else -screenHeight.toFloat() * 2
                                            )
                                            coroutineScope.launch {
                                                animatedOffset.animateTo(
                                                    target,
                                                    tween(
                                                        durationMillis = 500,
                                                        easing = LinearOutSlowInEasing
                                                    )
                                                )
                                                viewModel.randomOption()
                                                delay(200)
                                                animatedOffset.snapTo(Offset.Zero)
                                            }
                                        } else {
                                            coroutineScope.launch {
                                                animatedOffset.animateTo(
                                                    Offset.Zero,
                                                    spring(dampingRatio = 1f, stiffness = 1500f)
                                                )
                                            }
                                        }
                                    },
                                    onDrag = { change, dragAmount ->
                                        change.consume()
                                        coroutineScope.launch {
                                            animatedOffset.snapTo(
                                                Offset(
                                                    animatedOffset.value.x + 2f * dragAmount.x,
                                                    animatedOffset.value.y + 2f * dragAmount.y
                                                )
                                            )
                                        }
                                    }
                                )
                            }
                            .clickable(
                                onClick = {
                                    viewModel.showMessage(
                                        context.getString(R.string.app_how_did_you_like_it),
                                        context.getString(R.string.app_another_one)
                                    )
                                },
                                indication = null,
                                interactionSource = remember { MutableInteractionSource() }
                            ),
                        colors = CardDefaults.cardColors(containerColor = theme.card),
                        shape = RoundedCornerShape(5.dp),
                        elevation = CardDefaults.cardElevation(
                            defaultElevation = 1.dp,
                            pressedElevation = 1.dp,
                            focusedElevation = 1.dp,
                            hoveredElevation = 1.dp,
                            draggedElevation = 1.dp,
                            disabledElevation = 1.dp
                        )
                    ) {
                        Column(
                            modifier = Modifier
                                .padding(vertical = 30.dp)
                                .fillMaxSize(),
                            verticalArrangement = Arrangement.Center,
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = uiState.option.name,
                                modifier = Modifier
                                    .wrapContentWidth(align = Alignment.CenterHorizontally)
                                    .height(80.dp),
                                color = theme.primaryTextColor,
                                fontSize = 32.sp,
                                fontWeight = FontWeight.Bold
                            )
                            if (imageFile.value != null) {
                                AsyncImage(
                                    model = imageFile.value,
                                    contentDescription = null,
                                    modifier = Modifier
                                        .wrapContentWidth(Alignment.CenterHorizontally)
                                        .height(200.dp)
                                        .padding(vertical = 20.dp),
                                    contentScale = ContentScale.Fit,
                                    placeholder = painterResource(R.drawable.ic_transparent),
                                    error = painterResource(R.drawable.ic_transparent)
                                )
                            } else {
                                Image(
                                    painter = painterResource(uiState.option.foodType().icon),
                                    contentDescription = null,
                                    modifier = Modifier
                                        .wrapContentWidth(Alignment.CenterHorizontally)
                                        .height(200.dp)
                                        .padding(vertical = 20.dp),
                                    contentScale = ContentScale.Fit
                                )
                            }
                            Text(
                                text = uiState.option.description,
                                modifier = Modifier
                                    .padding(
                                        start = 20.dp,
                                        end = 20.dp,
                                        top = 20.dp,
                                        bottom = 20.dp
                                    )
                                    .wrapContentWidth(Alignment.CenterHorizontally)
                                    .height(120.dp),
                                color = theme.primaryTextColor,
                                fontSize = 24.sp
                            )
                        }
                    }
                }
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .wrapContentHeight()
                        .background(color = Color.Transparent)
                        .padding(top = 0.dp, bottom = 72.dp, start = 20.dp, end = 20.dp),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Button(
                        onClick = {
                            viewModel.randomOption()
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFFF7A44C), contentColor = Color(0xFFF6F6F6)
                        ),
                        modifier = Modifier
                            .wrapContentWidth(align = Alignment.CenterHorizontally)
                            .height(50.dp),
                        shape = RoundedCornerShape(size = 50.dp * 0.5f)
                    ) {
                        Text(
                            text = "  ${context.getString(R.string.app_another_one)}  ",
                            color = Color(0xFFF6F6F6),
                            fontSize = 26.sp
                        )
                    }
                }
            }
        }
        if (uiState.messageText.isNotBlank()) {
            LaunchedEffect(uiState.messageText) {
                val result = state.showSnackbar(
                    message = uiState.messageText,
                    actionLabel = uiState.actionText
                )
                if (result == SnackbarResult.ActionPerformed) {
                    when (uiState.actionText) {
                        context.getString(R.string.app_another_one) -> viewModel.randomOption()
                        else -> {}
                    }
                }
                viewModel.afterMessageShown()
            }
        }
    }
}