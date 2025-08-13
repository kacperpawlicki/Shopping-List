package com.example.shoppinglist

import android.os.Bundle
import androidx.activity.ComponentActivity
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Checkbox
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.shoppinglist.ui.theme.ShoppingListTheme

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





                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    Column(
                        modifier = Modifier
                            .padding(innerPadding)
                            .background(Color(0xFF232222))
                            .fillMaxSize()
                    ) {
                        Box(
                            modifier = Modifier
                                .height(70.dp)
                                .fillMaxWidth()
                        ) {
                            Text(
                                text = "Lista zakupów",
                                color = Color.White,
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
                                    }
                                ) {
                                    Icon(
                                        imageVector =  Icons.Default.Delete,
                                        contentDescription = "Usuń liste",
                                    )
                                }
                                IconButton(
                                    onClick = {
                                        if(addingItems){
                                            addingItems = false
                                        } else {
                                            addingItems = true
                                        }
                                    }
                                ) {
                                    Icon(
                                        imageVector =  Icons.Default.Add,
                                        contentDescription = "Dodawanie",
                                    )
                                }
                            }


                        }

                        HorizontalDivider()

                        if(addingItems) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                            ){
                                TextField(
                                    value = name,
                                    modifier = Modifier
                                        .align(Alignment.CenterStart)
                                        .padding(10.dp),
                                    onValueChange = { text ->
                                        name = text
                                    }
                                )
                                IconButton(
                                    modifier = Modifier
                                        .padding(10.dp)
                                        .align(Alignment.CenterEnd),
                                    onClick = {
                                        if(name.isNotBlank()) {
                                            shoppingItems = shoppingItems + ShoppingItem(name, false)
                                            name = ""
                                        }
                                    }
                                ) {
                                    Icon(
                                        imageVector =  Icons.Default.Check,
                                        contentDescription = "Dodaj element",
                                    )
                                }
                            }
                        }


                        LazyColumn {
                            items(shoppingItems) { currentItem ->
                                var isChecked by remember {
                                    mutableStateOf(currentItem.checked)
                                }

                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable {
                                            isChecked = !isChecked
                                            currentItem.checked = isChecked
                                        }
                                ) {
                                    Checkbox(
                                        checked = isChecked,
                                        onCheckedChange = {
                                            isChecked = it
                                            currentItem.checked = it
                                        }
                                    )
                                    Text(
                                        text = currentItem.name,
                                        textDecoration = if(currentItem.checked) TextDecoration.LineThrough else TextDecoration.None,
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

data class ShoppingItem (val name: String, var checked: Boolean)