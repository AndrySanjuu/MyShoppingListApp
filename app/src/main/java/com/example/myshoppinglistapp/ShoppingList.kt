package com.example.myshoppinglistapp

import android.Manifest
import android.content.Context
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.core.app.ActivityCompat
import androidx.lifecycle.ViewModel
import androidx.navigation.NavController

@Composable
fun ShoppingListApp(
    locationUtils: LocationUtils,
    viewModel: LocationViewModel,
    navController: NavController,
    context: Context,
    address: String
){
    var sItems by remember { mutableStateOf( listOf<ShoppingItem>() ) }
    var itemName by remember { mutableStateOf( "" ) }
    var itemQuantity by remember { mutableStateOf( "" ) }
    var showDialog by remember { mutableStateOf(false)}

    val requestPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions(),
        onResult = {permissions ->
            if(permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true &&
                permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true){
                locationUtils.requestLocationUpdates(viewModel)
            } else {
                val rationalRequired = ActivityCompat.shouldShowRequestPermissionRationale(context as MainActivity, Manifest.permission.ACCESS_FINE_LOCATION) ||
                        ActivityCompat.shouldShowRequestPermissionRationale(context as MainActivity, Manifest.permission.ACCESS_COARSE_LOCATION)

                if (rationalRequired){
                    Toast.makeText(context, "Location Permission is required for this feature to work", Toast.LENGTH_LONG).show()
                } else {
                    Toast.makeText(context, "Location Permission is required, please enable it in the Android Settings ", Toast.LENGTH_LONG).show()
                }
            }
        })




    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center){
        Button(
            onClick = { showDialog = true },
            modifier = Modifier.align(Alignment.CenterHorizontally)){
            Text(text = "Add Item")
        }
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)){
            items(sItems){
                item ->
                if(item.isEditing){
                    ShoppingItemEditor(item = item, ondEditComplete = {
                        editedName, editedQuantity ->
                        sItems = sItems.map {it.copy(isEditing = false)}
                        val editedItem = sItems.find { it.id == item.id }
                        editedItem?.let {
                            it.name = editedName
                            it.quantity = editedQuantity
                            it.address = address
                        }
                    })
                } else {
                    ShoppingListItem(item = item,
                        onEditClick = { sItems = sItems.map { it.copy(isEditing = it.id == item.id)}},
                        onDeleteClick = { sItems = sItems - item })

                }
            }
        }
    }

    if(showDialog){
        AlertDialog(onDismissRequest = { showDialog = false },
            confirmButton = {
                            Row (
                                Modifier
                                    .fillMaxWidth()
                                    .padding(8.dp),
                                horizontalArrangement = Arrangement.SpaceBetween){
                                Button(onClick = {

                                    if(itemName.isNotBlank()){
                                        val newItem = ShoppingItem(
                                            id = sItems.size+1,
                                            name = itemName,
                                            quantity = itemQuantity.toIntOrNull() ?: 1,
                                            address = address
                                        )
                                        sItems += newItem
                                        showDialog = false
                                        itemName = ""
                                        itemQuantity = ""
                                    }

                                }) {
                                    Text(text = "Add")
                                }
                                Button(onClick = { showDialog = false }) {
                                    Text(text = "Cancel")
                                }
                            }
            },
            title = { Text(text = "Add Shopping Item")},
            text = {
                Column {
                    OutlinedTextField(
                        value = itemName,
                        onValueChange = { itemName = it },
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(8.dp))

                    OutlinedTextField(
                        value = itemQuantity,
                        onValueChange = { itemQuantity = it },
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(8.dp))
                    Button(onClick = {
                        if(locationUtils.hasLocationPermission(context)){
                            locationUtils.requestLocationUpdates(viewModel)
                            navController.navigate("location_screen"){
                                this.launchSingleTop
                            }
                        } else {
                            requestPermissionLauncher.launch(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION))
                        }
                    }) {
                        Text(text = "Address")
                    }
                }
            })
    }
}