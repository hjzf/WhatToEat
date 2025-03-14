package com.lkjhgfdsa.ui.pages

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
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
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.io.File

data class ItemListPageUiState(
    val messageText: String = "",
    val manageMode: Boolean = false,
    val options: List<Option> = emptyList()
)

class ItemListPageViewModel : ViewModel() {

    private val _manageMode = MutableStateFlow(false)

    fun reverseManageMode() {
        _manageMode.update { !it }
    }

    private val _messageText = MutableStateFlow("")

    fun showMessage(text: String) {
        _messageText.update { text }
    }

    fun afterMessageShown() {
        _messageText.update { "" }
    }

    fun deleteOption(option: Option) {
        viewModelScope.launch {
            Repository.deleteOptionsAsync(option)
        }
    }

    private val _options = Repository.getOptionsFlow()

    val uiState = combine(_manageMode, _options, _messageText) { manageMode, options, messageText ->
        ItemListPageUiState(manageMode = manageMode, options = options, messageText = messageText)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = ItemListPageUiState()
    )

}

@Composable
fun OptionListPage(
    goBack: () -> Unit,
    goTo: (String) -> Unit,
    viewModel: ItemListPageViewModel = viewModel(),
    state: SnackbarHostState = remember { SnackbarHostState() }
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val theme = LocalTheme.current
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
        }) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight()
                .background(color = theme.background)
                .padding(paddingValues = paddingValues)
        ) {
            val backIcon = theme.backIcon
            TitleBar(imageResource = backIcon,
                textLeft = context.getString(R.string.app_options),
                onImageClick = { goBack() },
                textRight = if (uiState.manageMode) context.getString(R.string.app_cancel) else context.getString(
                    R.string.app_manage
                ),
                onTextRightClick = { viewModel.reverseManageMode() })
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(0.dp)
                    .weight(1f)
            ) {
                items(uiState.options, key = { it.id }) { option ->
                    OptionCard(name = option.name,
                        showDeleteButton = uiState.manageMode,
                        onItemClick = {
                            goTo("/home/option-list/option-edit?id=${option.id}")
                        },
                        image = option.image,
                        foodType = option.foodType(),
                        onDeleteButtonClick = {
                            viewModel.deleteOption(option)
                            viewModel.reverseManageMode()
                            viewModel.showMessage("${option.name}${context.getString(R.string.app_has_been_deleted)}")
                        }
                    )
                }
                if (!uiState.manageMode) {
                    item {
                        AddOptionCard { goTo("/home/option-list/option-edit") }
                    }
                }
                item {
                    Spacer(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(15.dp)
                    )
                }
            }
            if (uiState.messageText.isNotBlank()) {
                LaunchedEffect(uiState.messageText) {
                    state.showSnackbar(
                        message = uiState.messageText,
                        actionLabel = context.getString(R.string.app_ok)
                    )
                    viewModel.afterMessageShown()
                }
            }
        }
    }
}

@Composable
fun OptionCard(
    name: String,
    image: String = "",
    foodType: FoodType = FoodType.NULL,
    showDeleteButton: Boolean = false,
    onItemClick: () -> Unit = {},
    onDeleteButtonClick: () -> Unit = {}
) {
    val context = LocalContext.current
    val theme = LocalTheme.current
    val imageFile = remember(image) {
        derivedStateOf {
            if (image.isEmpty()) {
                null
            } else {
                val file = File(context.filesDir, "$IMAGE_DIR/${image}")
                if (file.exists() && file.isFile && file.canRead()) {
                    file
                } else {
                    null
                }
            }
        }
    }
    Spacer(
        modifier = Modifier
            .fillMaxWidth()
            .height(15.dp)
    )
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .wrapContentHeight()
            .padding(start = 15.dp, end = 15.dp)
            .clickable(onClick = onItemClick,
                indication = null,
                interactionSource = remember { MutableInteractionSource() })
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight(),
            colors = CardDefaults.cardColors(containerColor = theme.card),
            shape = RoundedCornerShape(7.dp),
            elevation = CardDefaults.cardElevation(
                defaultElevation = 0.dp,
                pressedElevation = 0.dp,
                focusedElevation = 0.dp,
                hoveredElevation = 0.dp,
                draggedElevation = 0.dp,
                disabledElevation = 0.dp
            )
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(IntrinsicSize.Max)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .wrapContentHeight()
                        .padding(start = 15.dp, end = 15.dp, top = 20.dp, bottom = 20.dp)
                        .background(color = Color.Transparent),
                    horizontalArrangement = Arrangement.Start,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Card(
                        modifier = Modifier
                            .width(75.dp)
                            .height(50.dp),
                        colors = CardDefaults.cardColors(containerColor = theme.card),
                        shape = RoundedCornerShape(5.dp),
                        elevation = CardDefaults.cardElevation(
                            defaultElevation = 0.dp,
                            pressedElevation = 0.dp,
                            focusedElevation = 0.dp,
                            hoveredElevation = 0.dp,
                            draggedElevation = 0.dp,
                            disabledElevation = 0.dp
                        )
                    ) {
                        if (imageFile.value != null) {
                            AsyncImage(
                                model = imageFile.value,
                                contentDescription = null,
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Fit,
                                placeholder = painterResource(R.drawable.ic_transparent),
                                error = painterResource(R.drawable.ic_transparent)
                            )
                        } else {
                            Image(
                                painter = painterResource(foodType.icon),
                                contentDescription = null,
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Fit
                            )
                        }
                    }
                    Spacer(
                        modifier = Modifier.width(20.dp)
                    )
                    Text(
                        text = name, color = theme.primaryTextColor, fontSize = 16.sp
                    )
                    Spacer(
                        modifier = Modifier
                            .width(0.dp)
                            .weight(1f)
                    )
                    if (showDeleteButton) {
                        Button(
                            onClick = { onDeleteButtonClick() },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = theme.deleteButtonBackground
                            ),
                            shape = RoundedCornerShape(3.dp)
                        ) {
                            Text(
                                text = context.getString(R.string.app_delete),
                                color = Color.White,
                                fontSize = 14.sp
                            )
                        }
                        Spacer(
                            modifier = Modifier.width(5.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun AddOptionCard(
    onItemClick: () -> Unit = {}
) {
    val context = LocalContext.current
    val theme = LocalTheme.current
    Spacer(
        modifier = Modifier
            .fillMaxWidth()
            .height(15.dp)
    )
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .wrapContentHeight()
            .padding(start = 15.dp, end = 15.dp)
            .clickable(onClick = onItemClick,
                indication = null,
                interactionSource = remember { MutableInteractionSource() })
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight(),
            colors = CardDefaults.cardColors(containerColor = theme.card),
            shape = RoundedCornerShape(7.dp),
            elevation = CardDefaults.cardElevation(
                defaultElevation = 0.dp,
                pressedElevation = 0.dp,
                focusedElevation = 0.dp,
                hoveredElevation = 0.dp,
                draggedElevation = 0.dp,
                disabledElevation = 0.dp
            )
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(IntrinsicSize.Max)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .wrapContentHeight()
                        .padding(start = 15.dp, end = 15.dp, top = 20.dp, bottom = 20.dp)
                        .background(color = Color.Transparent),
                    horizontalArrangement = Arrangement.Start,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Card(
                        modifier = Modifier
                            .width(75.dp)
                            .height(50.dp),
                        colors = CardDefaults.cardColors(containerColor = theme.card),
                        shape = RoundedCornerShape(5.dp),
                        elevation = CardDefaults.cardElevation(
                            defaultElevation = 0.dp,
                            pressedElevation = 0.dp,
                            focusedElevation = 0.dp,
                            hoveredElevation = 0.dp,
                            draggedElevation = 0.dp,
                            disabledElevation = 0.dp
                        )
                    ) {
                        Image(
                            painter = painterResource(id = theme.addOptionIcon),
                            contentDescription = null,
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Fit
                        )
                    }
                    Spacer(
                        modifier = Modifier.width(20.dp)
                    )
                    Text(
                        text = context.getString(R.string.app_add_new_option),
                        color = theme.primaryTextColor,
                        fontSize = 16.sp
                    )
                    Spacer(
                        modifier = Modifier
                            .width(0.dp)
                            .weight(1f)
                    )
                }
            }
        }
    }
}