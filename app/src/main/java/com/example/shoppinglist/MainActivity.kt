package com.example.shoppinglist

import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import android.content.Context
import android.content.res.Resources
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxColors
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusEvent
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ModifierLocalBeyondBoundsLayout
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.LineHeightStyle
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.datastore.preferences.core.edit
import com.example.shoppinglist.ui.theme.ShoppingListTheme
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            ShoppingListTheme {

                var shoppingItems by remember {
                    mutableStateOf(listOf<ShoppingItem>())
                }

                var name by remember {
                    mutableStateOf("")
                }

                var addingItems by remember {
                    mutableStateOf(false)
                }

                val focusRequester = FocusRequester()

                val keyboardController = LocalSoftwareKeyboardController.current

                //testowy kod
                val context = LocalContext.current
                val scope = rememberCoroutineScope()
                val listState = rememberLazyListState()

                LaunchedEffect(Unit) {
                    shoppingItems = ShoppingRepository.loadList(context)
                }
                //

                Column(
                    modifier = Modifier
                        .background(MaterialTheme.colorScheme.background)
                        .fillMaxSize()
                        .imePadding()
                        .navigationBarsPadding()
                        .statusBarsPadding()
                ) {
                    Box(
                        modifier = Modifier
                            .height(70.dp)
                            .fillMaxWidth()
                    ) {
                        Text(
                            text = "Lista zakupów",
                            color = MaterialTheme.colorScheme.primary,
                            fontSize = 30.sp,
                            modifier = Modifier
                                .padding(10.dp)
                                .align(Alignment.CenterStart)
                        )
                        Row(
                            modifier = Modifier
                                .align(Alignment.CenterEnd)
                                .padding(10.dp),
                        ) {
                            IconButton(
                                onClick = {
                                    shoppingItems = emptyList()
                                    scope.launch {
                                        ShoppingRepository.saveList(context, emptyList())
                                    }
                                }
                            ) {
                                Icon(
                                    imageVector =  Icons.Default.Delete,
                                    contentDescription = "Usuń liste",
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            }
                            IconButton(
                                onClick = {
                                    addingItems = !addingItems
                                    if (!addingItems){
                                        scope.launch {
                                            listState.animateScrollToItem(0)
                                        }
                                    }
                                }
                            ) {
                                Icon(
                                    imageVector =  Icons.Default.Add,
                                    contentDescription = "Dodawanie",
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            }
                        }


                    }


                    HorizontalDivider()


                    LazyColumn(
                        modifier = Modifier
                            .weight(1f),
                        state = listState
                    ) {
                        items(shoppingItems) { currentItem ->
                            var isChecked by remember {
                                mutableStateOf(currentItem.checked)
                            }

                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        if (addingItems) {
                                            addingItems = false
                                        } else {
                                            isChecked = !isChecked
                                            currentItem.checked = isChecked
                                            scope.launch {
                                                ShoppingRepository.saveList(context, shoppingItems)
                                            }
                                        }
                                    }
                            ) {
                                Checkbox(
                                    checked = isChecked,
                                    onCheckedChange = {
                                        isChecked = it
                                        currentItem.checked = it
                                        addingItems = false
                                        scope.launch {
                                            ShoppingRepository.saveList(context, shoppingItems)
                                        }
                                    }
                                )
                                Text(
                                    text = currentItem.name,
                                    textDecoration = if(currentItem.checked) TextDecoration.LineThrough else TextDecoration.None,
                                    color = MaterialTheme.colorScheme.secondary,
                                    fontSize = 16.sp
                                )
                            }
                        }
                    }


                    if(addingItems) {
                        LaunchedEffect(Unit) {
                            focusRequester.requestFocus()
                        }

                        BackHandler {
                            addingItems = false
                            scope.launch {
                                listState.animateScrollToItem(0)
                            }
                            name = ""
                        }

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                        ){
                            OutlinedTextField(
                                value = name,
                                singleLine = true,
                                modifier = Modifier
                                    .align(Alignment.CenterStart)
                                    .padding(10.dp)
                                    .focusRequester(focusRequester)
                                    .fillMaxWidth(),
                                onValueChange = { text ->
                                    name = text
                                },
                                keyboardOptions = KeyboardOptions(
                                    imeAction = ImeAction.Done
                                ),
                                keyboardActions = KeyboardActions(
                                    onDone = {
                                        keyboardController?.hide()
                                        shoppingItems = shoppingItems + ShoppingItem(name, false)
                                        name = ""
                                        scope.launch {
                                            ShoppingRepository.saveList(context, shoppingItems)
                                            listState.animateScrollToItem(0)
                                        }
                                        addingItems = false
                                    }
                                ),
                                placeholder = {Text(text = "Dodaj produkt...")},
                                trailingIcon = {
                                    IconButton(
                                        modifier = Modifier
                                            .padding(10.dp)
                                            .align(Alignment.CenterEnd),
                                        onClick = {
                                            if(name.isNotBlank()) {
                                                shoppingItems = shoppingItems + ShoppingItem(name, false)
                                                name = ""
                                                scope.launch {
                                                    ShoppingRepository.saveList(context, shoppingItems)
                                                    listState.animateScrollToItem(shoppingItems.lastIndex)
                                                }
                                            } else {
                                                addingItems = false
                                                scope.launch {
                                                    listState.animateScrollToItem(0)
                                                }
                                            }
                                        }
                                    ) {
                                        Icon(
                                            imageVector =  Icons.Default.Check,
                                            contentDescription = "Dodaj element",
                                        )
                                    }
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

data class ShoppingItem (val name: String, var checked: Boolean)


//testowy kod
val Context.dataStore by preferencesDataStore(name = "shopping_list")
val SHOPPING_LIST_KEY = stringPreferencesKey("shopping_list")

object ShoppingRepository {
    private val gson = Gson()

    suspend fun saveList(context: Context, list: List<ShoppingItem>) {
        val json = gson.toJson(list)
        context.dataStore.edit { prefs ->
            prefs[SHOPPING_LIST_KEY] = json
        }
    }

    suspend fun loadList(context: Context): List<ShoppingItem> {
        val prefs = context.dataStore.data.first()
        val json = prefs[SHOPPING_LIST_KEY] ?: "[]"
        val type = object : TypeToken<List<ShoppingItem>>() {}.type
        return gson.fromJson(json, type)
    }
}