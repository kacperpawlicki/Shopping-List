package com.example.shoppinglist

import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import android.content.Context
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
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Checkbox
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.datastore.preferences.core.edit
import com.example.shoppinglist.ui.theme.ShoppingListTheme
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.util.UUID
import kotlin.collections.plus

/*
    Pierwszy projekt aplikacji Android z uzyciem Kotlin i Jetpack Compose
    Aplikacja tworzona podczas nauki jezyka i technologii
    Niektore komentarze w formie notatek przy nauce
 */

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            ShoppingListTheme {
                ShoppingListScreen()
            }
        }
    }
}


@Composable
fun ShoppingListScreen(){
    val scope = rememberCoroutineScope()
    val listState = rememberLazyListState()
    val context = LocalContext.current

    var shoppingItems by remember { mutableStateOf(listOf<ShoppingItem>()) }
    var name by remember { mutableStateOf("") }
    var addingItems by remember { mutableStateOf(false) }
    var showDialog by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        shoppingItems = ShoppingRepository.loadList(context)
    }

    Column(
        modifier = Modifier
            .background(MaterialTheme.colorScheme.background)
            .fillMaxSize()
            .imePadding()
            .navigationBarsPadding()
            .statusBarsPadding()
    ) {

        TopBar(
            onDeleteAll = {
                showDialog = true
            },
            onAddingToggle = {
                addingItems = !addingItems
                if (!addingItems){
                    scope.launch {
                        listState.animateScrollToItem(0)
                    }
                }
            }
        )

        HorizontalDivider()

        ShoppingList(
            listState = listState,
            items = shoppingItems,
            modifier = Modifier.weight(1f),
            onItemCheckedChange = { currentItem, checked ->
                currentItem.checked = checked
                name = ""
                scope.launch {
                    ShoppingRepository.saveList(context, shoppingItems)
                }
            },
            onRemove = { item ->
                shoppingItems = shoppingItems - item
                scope.launch {
                    ShoppingRepository.saveList(context, shoppingItems)
                }
            }
        )

        if(showDialog) {
            DeleteConfirmationDialog(
                onConfirm = {
                    shoppingItems = emptyList()
                    scope.launch {
                        ShoppingRepository.saveList(context, emptyList())
                    }
                    showDialog = false
                    addingItems = false
                },
                onDismiss = {
                    showDialog = false
                    addingItems = false
                }
            )
        }

        if(addingItems) {
            AddItemField(
                name = name,
                onCancel = {
                    addingItems = false
                    scope.launch {
                        listState.animateScrollToItem(0)
                    }
                    name = ""
                },
                onNameChange = { text ->
                    name = text
                },
                onSubmit = {
                    if(name.isNotBlank()) {
                        shoppingItems = shoppingItems + ShoppingItem(name = name, checked = false)
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
            )
        }

    }

}


@Composable
fun DeleteConfirmationDialog(onDismiss: () -> Unit, onConfirm: () -> Unit){
    AlertDialog(
        onDismissRequest = { onDismiss() },
        title = {
            Text(text = "Potwierdzenie")
        },
        text = {
            Text("Czy na pewno chcesz usunąć wszystkie elementy?")
        },
        confirmButton = {
            TextButton(onClick = { onConfirm() }) {
                Text("Usuń")
            }
        },
        dismissButton = {
            TextButton(onClick = { onDismiss() }) {
                Text("Anuluj")
            }
        }
    )
}

@Composable
fun TopBar(onDeleteAll: () -> Unit, onAddingToggle: () -> Unit) {
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
            IconButton(onClick = onDeleteAll) {
                Icon(
                    imageVector =  Icons.Default.Delete,
                    contentDescription = "Usuń liste",
                    tint = MaterialTheme.colorScheme.primary
                )
            }
            IconButton(onClick = onAddingToggle) {
                Icon(
                    imageVector =  Icons.Default.Add,
                    contentDescription = "Dodawanie",
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

@Composable
fun ShoppingList(
    listState: LazyListState,
    items: List<ShoppingItem>,
    onItemCheckedChange: (ShoppingItem, Boolean) -> Unit,
    onRemove: (ShoppingItem) -> Unit,
    modifier: Modifier
) {
    LazyColumn(
        modifier = modifier,
        state = listState
    ) {
        items(items, key = { it.id }) { currentItem ->
            val swipeToDismissBoxState = rememberSwipeToDismissBoxState(
                confirmValueChange = {
                    it == SwipeToDismissBoxValue.StartToEnd
                }
            )

            SwipeToDismissBox(
                state = swipeToDismissBoxState,
                backgroundContent = {
                    if (swipeToDismissBoxState.dismissDirection == SwipeToDismissBoxValue.StartToEnd) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(Color(0x2D650808))
                                .padding(16.dp),
                            contentAlignment = Alignment.CenterEnd
                        ) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = "Usuń",
                                tint = Color.White
                            )
                        }
                    }
                }
            ) {
                LaunchedEffect(swipeToDismissBoxState.currentValue) {
                    if (swipeToDismissBoxState.currentValue == SwipeToDismissBoxValue.StartToEnd) {
                        delay(50)
                        onRemove(currentItem)
                    }
                }
                ShoppingListItem(currentItem,
                    onCheckedChange = { checked ->
                        onItemCheckedChange(currentItem, checked)
                    },
                    )
            }
        }
    }
}

@Composable
fun ShoppingListItem(
    item: ShoppingItem,
    onCheckedChange: (Boolean) -> Unit,
) {
    var isChecked by remember {
        mutableStateOf(item.checked)
    }

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = {
                isChecked = !isChecked
                onCheckedChange(isChecked)
            })
    ) {
        Checkbox(
            checked = isChecked,
            onCheckedChange = {
                isChecked = it
                onCheckedChange(it)
            }
        )
        Text(
            text = item.name,
            textDecoration = if(item.checked) TextDecoration.LineThrough else TextDecoration.None,
            color = MaterialTheme.colorScheme.secondary,
            fontSize = 16.sp
        )
    }
}

@Composable
fun AddItemField(name: String, onCancel: () -> Unit, onNameChange: (String) -> Unit, onSubmit: () -> Unit){
    val focusRequester = FocusRequester()

    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
    }

    BackHandler { onCancel() }

    Box(
        modifier = Modifier.fillMaxWidth()
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
                onNameChange(text)
            },
            keyboardOptions = KeyboardOptions(
                imeAction = ImeAction.Done
            ),
            keyboardActions = KeyboardActions(
                onDone = { onSubmit() }
            ),
            placeholder = {Text(text = "Dodaj produkt...")},
            trailingIcon = {
                IconButton(
                    modifier = Modifier
                        .padding(10.dp)
                        .align(Alignment.CenterEnd),
                    onClick = { onSubmit() }
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



data class ShoppingItem (
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    var checked: Boolean
)


//tworzy nowa instancje dataStore przypisana do kontekstu
val Context.dataStore by preferencesDataStore(name = "shopping_list")

//definicja klucza w dataStore
val SHOPPING_LIST_KEY = stringPreferencesKey("shopping_list")



object ShoppingRepository {
    private val gson = Gson()

    suspend fun saveList(context: Context, list: List<ShoppingItem>) {
        val json = gson.toJson(list)
        //edycja zawartosci dataStore pod tym kluczem
        context.dataStore.edit { prefs ->
            prefs[SHOPPING_LIST_KEY] = json
        }
    }

    suspend fun loadList(context: Context): List<ShoppingItem> {
        //pobiera nowa wartosc dataStore
        val prefs = context.dataStore.data.first()
        val json = prefs[SHOPPING_LIST_KEY] ?: "[]"

        //deserializacja, wyciaga z JSON do listy
        val type = object : TypeToken<List<ShoppingItem>>() {}.type
        return gson.fromJson(json, type)
    }
}