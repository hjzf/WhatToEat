package com.lkjhgfdsa.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.lkjhgfdsa.ui.theme.LocalTheme

@Composable
fun TitleBar(
    imageResource: Int,
    modifier: Modifier = Modifier,
    textLeft: String = "",
    textRight: String = "",
    textLeftColor: Color = LocalTheme.current.primaryTextColor,
    textRightColor: Color = LocalTheme.current.primaryTextColor,
    textFontSize: TextUnit = 17.sp,
    height: Dp = 43.dp,
    onImageClick: () -> Unit = {},
    onTextLeftClick: () -> Unit = {},
    onTextRightClick: () -> Unit = {}
) {
    val theme = LocalTheme.current
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(height)
            .background(color = theme.background)
    ) {
        Column(
            modifier = modifier
                .wrapContentWidth()
                .fillMaxHeight()
                .clickable(
                    onClick = onImageClick,
                    indication = null,
                    interactionSource = remember { MutableInteractionSource() }
                ),
            verticalArrangement = Arrangement.Center,
        ) {
            Image(
                imageVector = ImageVector.vectorResource(imageResource),
                contentDescription = "",
                modifier = Modifier
                    .padding(start = 18.dp, end = 8.dp)
                    .width(41.dp)
            )
        }
        Column(
            modifier = modifier
                .wrapContentWidth()
                .fillMaxHeight()
                .clickable(
                    onClick = {},
                    indication = null,
                    interactionSource = remember { MutableInteractionSource() }
                ),
            verticalArrangement = Arrangement.Center,
        ) {
            Text(
                text = textLeft,
                modifier = modifier
                    .wrapContentWidth()
                    .wrapContentHeight()
                    .clickable(
                        onClick = onTextLeftClick,
                        indication = null,
                        interactionSource = remember { MutableInteractionSource() })
                    .padding(end = 18.dp),
                color = textLeftColor,
                fontSize = textFontSize
            )
        }
        Spacer(
            modifier = modifier
                .width(0.dp)
                .weight(1f)
                .fillMaxHeight()
        )
        Column(
            modifier = modifier
                .wrapContentWidth()
                .fillMaxHeight()
                .clickable(
                    onClick = {},
                    indication = null,
                    interactionSource = remember { MutableInteractionSource() }),
            verticalArrangement = Arrangement.Center,
        ) {
            Text(
                text = textRight,
                modifier = modifier
                    .wrapContentWidth()
                    .wrapContentHeight()
                    .clickable(
                        onClick = onTextRightClick,
                        indication = null,
                        interactionSource = remember { MutableInteractionSource() })
                    .padding(start = 18.dp, end = 18.dp),
                color = textRightColor,
                fontSize = textFontSize
            )
        }
    }
}