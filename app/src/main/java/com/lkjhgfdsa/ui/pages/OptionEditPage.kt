package com.lkjhgfdsa.ui.pages

import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.SavedStateHandle
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
import com.lkjhgfdsa.ui.components.SingleCheckBox
import com.lkjhgfdsa.ui.components.TitleBar
import com.lkjhgfdsa.ui.theme.LocalTheme
import github.leavesczy.matisse.CoilImageEngine
import github.leavesczy.matisse.Matisse
import github.leavesczy.matisse.MatisseCapture
import github.leavesczy.matisse.MatisseCaptureContract
import github.leavesczy.matisse.MatisseContract
import github.leavesczy.matisse.MediaResource
import github.leavesczy.matisse.MediaStoreCaptureStrategy
import github.leavesczy.matisse.MediaType
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

private val types = listOf(
    FoodType.TYPE_1,
    FoodType.TYPE_2,
    FoodType.TYPE_3,
    FoodType.TYPE_4,
    FoodType.TYPE_5,
    FoodType.NULL
)

data class ItemEditPageUiState(
    val addNewOption: Boolean = true,
    val contentChanged: Boolean = false,
    val messageText: String = "",
    val id: String = "",
    val name: String = "",
    val image: String = "",
    val description: String = "",
    val foodType: FoodType = FoodType.NULL,
)

class ItemEditPageViewModel(savedStateHandle: SavedStateHandle) : ViewModel() {

    private val _id: String? = savedStateHandle["id"]

    private val _uiState = MutableStateFlow(ItemEditPageUiState())

    fun showMessage(text: String) {
        _uiState.update { it.copy(messageText = text) }
    }

    fun afterMessageShown() {
        _uiState.update { it.copy(messageText = "") }
    }

    fun setName(value: String) {
        _uiState.update { it.copy(name = value, contentChanged = true) }
    }

    fun setDescription(value: String) {
        _uiState.update { it.copy(description = value, contentChanged = true) }
    }

    fun setImage(value: String) {
        _uiState.update { it.copy(image = value, contentChanged = true) }
    }

    fun setFoodType(value: FoodType) {
        _uiState.update { it.copy(foodType = value, contentChanged = true) }
    }

    fun saveOption(afterSave: () -> Unit = {}) {
        viewModelScope.launch {
            if (_uiState.value.addNewOption) {
                Repository.insertOptionsAsync(
                    Option(
                        name = _uiState.value.name,
                        description = _uiState.value.description,
                        image = _uiState.value.image,
                        type = _uiState.value.foodType.value,
                    )
                )
            } else {
                Repository.updateOptionsAsync(
                    Option(
                        id = _uiState.value.id,
                        name = _uiState.value.name,
                        description = _uiState.value.description,
                        image = _uiState.value.image,
                        type = _uiState.value.foodType.value
                    )
                )
            }
            _uiState.update { it.copy(contentChanged = false) }
            afterSave()
        }
    }

    val uiState = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            if (!_id.isNullOrEmpty()) {
                val option = Repository.getOptionAsync(_id)
                if (option != null) {
                    _uiState.update {
                        it.copy(
                            addNewOption = false,
                            id = option.id,
                            name = option.name,
                            description = option.description,
                            image = option.image,
                            foodType = option.foodType(),
                        )
                    }
                }
            }
        }
    }

}

@Composable
fun OptionEditPage(
    goBack: () -> Unit,
    viewModel: ItemEditPageViewModel = viewModel(),
    state: SnackbarHostState = remember { SnackbarHostState() }
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val theme = LocalTheme.current
    val textStyle = LocalTextStyle.current
    val context = LocalContext.current
    val backIcon = theme.backIcon
    val saveButtonColor = remember(uiState.contentChanged) {
        if (uiState.contentChanged) {
            theme.primaryTextColor
        } else {
            theme.secondTextColor
        }
    }
    val imageFile = remember(uiState.image) {
        derivedStateOf {
            if (uiState.image.isEmpty()) {
                null
            } else {
                val file = File(context.filesDir, "${IMAGE_DIR}/${uiState.image}")
                if (file.exists() && file.isFile && file.canRead()) {
                    file
                } else {
                    null
                }
            }
        }
    }
    val mediaPickerLauncher = rememberLauncherForActivityResult(
        contract = MatisseContract()
    ) { result: List<MediaResource>? ->
        if (!result.isNullOrEmpty()) {
            val mediaResource = result[0]
            val uri = mediaResource.uri
            val fileName = mediaResource.name
            val dotIndex = fileName.lastIndexOf(".")
            val extension = if (dotIndex == -1) {
                ""
            } else {
                fileName.substring(dotIndex)
            }
            val newImageName = "image_${System.currentTimeMillis()}${extension}"
            try {
                context.contentResolver.openInputStream(uri)?.use { inputStream ->
                    val targetDir = File(context.filesDir, IMAGE_DIR).apply {
                        if (!exists()) mkdirs()
                    }
                    val outputFile = File(targetDir, newImageName)
                    FileOutputStream(outputFile).use { outputStream ->
                        inputStream.copyTo(outputStream)
                    }
                    viewModel.setImage(newImageName)
                } ?: throw IOException("can not open InputStream: $uri")
            } catch (e: Exception) {
                Toast.makeText(
                    context,
                    "Failed to save image: ${e.message}",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }
    val takePictureLauncher = rememberLauncherForActivityResult(
        contract = MatisseCaptureContract()
    ) { mediaResource ->
        if (mediaResource != null) {
            val uri = mediaResource.uri
            val fileName = mediaResource.name
            val dotIndex = fileName.lastIndexOf(".")
            val extension = if (dotIndex == -1) {
                ""
            } else {
                fileName.substring(dotIndex)
            }
            val newImageName = "image_${System.currentTimeMillis()}${extension}"
            try {
                context.contentResolver.openInputStream(uri)?.use { inputStream ->
                    val targetDir = File(context.filesDir, IMAGE_DIR).apply {
                        if (!exists()) mkdirs()
                    }
                    val outputFile = File(targetDir, newImageName)
                    FileOutputStream(outputFile).use { outputStream ->
                        inputStream.copyTo(outputStream)
                    }
                    viewModel.setImage(newImageName)
                } ?: throw IOException("can not open InputStream: $uri")
            } catch (e: Exception) {
                Toast.makeText(
                    context,
                    "Failed to save image: ${e.message}",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }
    val matisse = Matisse(
        maxSelectable = 1,
        imageEngine = CoilImageEngine(),
        mediaType = MediaType.ImageOnly
    )
    if (uiState.messageText.isNotBlank()) {
        LaunchedEffect(uiState.messageText) {
            state.showSnackbar(
                message = uiState.messageText,
                actionLabel = context.getString(R.string.app_ok)
            )
            viewModel.afterMessageShown()
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
        val screenSize = remember { mutableStateOf(IntSize.Zero) }
        Box(
            modifier = Modifier
                .fillMaxSize()
                .onSizeChanged {
                    screenSize.value = it
                }
        ) {
            val menuVisible = remember { mutableStateOf(false) }
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(color = theme.background)
                    .padding(paddingValues = paddingValues)
            ) {
                val focusRequester = remember { FocusRequester() }
                TitleBar(
                    imageResource = backIcon,
                    textLeft = if (uiState.addNewOption) context.getString(R.string.app_new_option) else uiState.name,
                    onImageClick = { goBack() },
                    textRight = context.getString(R.string.app_save),
                    textRightColor = saveButtonColor,
                    modifier = Modifier
                        .focusRequester(focusRequester)
                        .focusable(),
                    onTextRightClick = {
                        focusRequester.requestFocus()
                        if (uiState.name.isBlank()) {
                            viewModel.showMessage(context.getString(R.string.app_name_empty_tip))
                        } else {
                            viewModel.saveOption {
                                goBack()
                            }
                        }
                    }
                )
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(0.dp)
                        .weight(1f)
                        .background(color = theme.background)
                ) {
                    item {
                        Row(
                            modifier = Modifier
                                .padding(bottom = 15.dp)
                                .fillMaxWidth()
                                .wrapContentHeight(Alignment.CenterVertically)
                                .background(color = theme.card)
                                .padding(start = 20.dp, end = 20.dp, top = 10.dp, bottom = 10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            BasicTextField(
                                value = uiState.name,
                                onValueChange = { viewModel.setName(it) },
                                textStyle = textStyle.copy(
                                    textAlign = TextAlign.Start,
                                    lineHeight = 25.sp,
                                    color = theme.primaryTextColor,
                                    fontSize = 16.sp,
                                ),
                                singleLine = true,
                                cursorBrush = SolidColor(theme.primaryTextColor),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .fillMaxHeight()
                                    .padding(end = 2.dp),
                                decorationBox = { innerTextField ->
                                    if (uiState.name.isEmpty()) {
                                        Text(
                                            text = context.getString(R.string.app_name_input_place_holder),
                                            color = theme.secondTextColor,
                                            fontSize = 16.sp,
                                            textAlign = TextAlign.Start,
                                            lineHeight = 25.sp
                                        )
                                    }
                                    innerTextField()
                                }
                            )
                        }
                    }
                    item {
                        Box(
                            modifier = Modifier
                                .padding(bottom = 15.dp)
                                .fillMaxWidth()
                                .wrapContentHeight(Alignment.CenterVertically)
                                .background(color = theme.card)
                                .clickable {
                                    menuVisible.value = true
                                }
                                .padding(vertical = 20.dp),
                            contentAlignment = Alignment.Center,
                        ) {
                            if (imageFile.value != null) {
                                AsyncImage(
                                    model = imageFile.value,
                                    contentDescription = null,
                                    modifier = Modifier
                                        .width(240.dp)
                                        .height(160.dp),
                                    contentScale = ContentScale.Fit,
                                    placeholder = painterResource(R.drawable.ic_transparent),
                                    error = painterResource(R.drawable.ic_transparent)
                                )
                            } else {
                                Image(
                                    painter = painterResource(uiState.foodType.icon),
                                    contentDescription = null,
                                    modifier = Modifier
                                        .width(240.dp)
                                        .height(160.dp),
                                    contentScale = ContentScale.Fit
                                )
                            }
                        }
                    }
                    item {
                        Row(
                            modifier = Modifier
                                .background(color = theme.card)
                                .fillMaxWidth()
                                .height(48.dp * 3f)
                                .padding(start = 20.dp, end = 20.dp, top = 10.dp, bottom = 10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            BasicTextField(
                                value = uiState.description,
                                onValueChange = { viewModel.setDescription(it) },
                                textStyle = textStyle.copy(
                                    textAlign = TextAlign.Start,
                                    lineHeight = 25.sp,
                                    color = theme.primaryTextColor,
                                    fontSize = 16.sp,
                                ),
                                cursorBrush = SolidColor(theme.primaryTextColor),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .fillMaxHeight()
                                    .padding(end = 2.dp),
                                decorationBox = { innerTextField ->
                                    if (uiState.description.isEmpty()) {
                                        Text(
                                            text = context.getString(R.string.app_description_input_place_holder),
                                            color = theme.secondTextColor,
                                            fontSize = 16.sp,
                                            textAlign = TextAlign.Start,
                                            lineHeight = 25.sp
                                        )
                                    }
                                    innerTextField()
                                }
                            )
                        }
                    }
                    item {
                        SingleCheckBox(
                            list = types.map {
                                it to when (it) {
                                    FoodType.TYPE_1 -> context.getString(R.string.app_food_type_1)
                                    FoodType.TYPE_2 -> context.getString(R.string.app_food_type_2)
                                    FoodType.TYPE_3 -> context.getString(R.string.app_food_type_3)
                                    FoodType.TYPE_4 -> context.getString(R.string.app_food_type_4)
                                    FoodType.TYPE_5 -> context.getString(R.string.app_food_type_5)
                                    else -> context.getString(R.string.app_other_foods)
                                }
                            },
                            keyFunction = { it.value },
                            select = uiState.foodType,
                            onSelectChange = { viewModel.setFoodType(it) }
                        )
                    }
                }
            }
            AnimatedVisibility(
                visible = menuVisible.value,
                enter = fadeIn(animationSpec = tween(100)),
                exit = fadeOut(animationSpec = tween(100)),
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(color = Color.Black.copy(alpha = 0.5f))
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null,
                            onClick = {
                                menuVisible.value = false
                            }
                        )
                )
            }
            AnimatedVisibility(
                visible = menuVisible.value,
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentHeight()
                    .align(Alignment.BottomCenter),
                enter = slideInVertically(
                    animationSpec = tween(200),
                    initialOffsetY = { screenSize.value.height }),
                exit = slideOutVertically(
                    animationSpec = tween(200),
                    targetOffsetY = { screenSize.value.height }),
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .wrapContentHeight()
                        .background(color = theme.card)
                ) {
                    Box(
                        modifier = Modifier
                            .clickable {
                                mediaPickerLauncher.launch(matisse)
                                menuVisible.value = false
                            }
                            .padding(horizontal = 20.dp)
                            .fillMaxWidth()
                            .height(60.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            context.getString(R.string.app_choose_from_gallery),
                            color = theme.primaryTextColor,
                            textAlign = TextAlign.Start
                        )
                    }
                    HorizontalDivider(
                        modifier = Modifier
                            .padding(horizontal = 20.dp)
                            .fillMaxWidth(),
                        thickness = (0.5).dp,
                        color = theme.secondTextColor
                    )
                    Box(
                        modifier = Modifier
                            .clickable {
                                takePictureLauncher.launch(
                                    MatisseCapture(captureStrategy = MediaStoreCaptureStrategy())
                                )
                                menuVisible.value = false
                            }
                            .padding(horizontal = 20.dp)
                            .fillMaxWidth()
                            .height(60.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            context.getString(R.string.app_take_photo),
                            color = theme.primaryTextColor,
                            textAlign = TextAlign.Start
                        )
                    }
                    HorizontalDivider(
                        modifier = Modifier
                            .padding(horizontal = 20.dp)
                            .fillMaxWidth(),
                        thickness = (0.5).dp,
                        color = theme.secondTextColor
                    )
                    Box(
                        modifier = Modifier
                            .clickable { menuVisible.value = false }
                            .padding(horizontal = 20.dp)
                            .fillMaxWidth()
                            .height(60.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            context.getString(R.string.app_cancel),
                            color = theme.primaryTextColor,
                            textAlign = TextAlign.Start
                        )
                    }
                }
            }
        }
    }
}