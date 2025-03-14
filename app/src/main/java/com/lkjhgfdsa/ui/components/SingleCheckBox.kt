package com.lkjhgfdsa.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.key
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.lkjhgfdsa.R
import com.lkjhgfdsa.ui.theme.LocalTheme

@Composable
fun <T> SingleCheckBox(
    modifier: Modifier = Modifier,
    list: List<Pair<T, String>>,
    keyFunction: (T) -> Any?,
    select: T,
    onSelectChange: (T) -> Unit
) {
    Column(
        modifier = modifier
    ) {
        for ((value, label) in list) {
            key(keyFunction(value)) {
                SingleCheckBoxItem(
                    modifier = modifier,
                    label = label,
                    selected = select == value,
                    onItemClick = {
                        onSelectChange(value)
                    }
                )
            }
        }
    }
}

@Composable
private fun SingleCheckBoxItem(
    modifier: Modifier = Modifier, label: String, selected: Boolean, onItemClick: () -> Unit = {}
) {
    val theme = LocalTheme.current
    val focusRequester = remember { FocusRequester() }
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(50.dp)
            .focusRequester(focusRequester)
            .focusable()
            .clickable(
                onClick = {
                    focusRequester.requestFocus()
                    onItemClick()
                },
                indication = null,
                interactionSource = remember { MutableInteractionSource() }
            ),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Start
    ) {
        Box(
            modifier = modifier
                .padding(start = 20.dp, end = 20.dp)
                .width(30.dp)
                .height(30.dp)
        ) {
            Image(
                painter = painterResource(
                    id = if (selected) {
                        R.drawable.ic_square_box_selected
                    } else {
                        R.drawable.ic_square_box_unselected
                    }
                ), contentDescription = "select box"
            )
        }
        Text(text = label, color = theme.primaryTextColor, fontSize = 16.sp)
    }
}