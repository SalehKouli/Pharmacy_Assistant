package com.example.pharmacistassistant

import android.util.Log
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import com.example.pharmacistassistant.viewmodel.ProductViewModel

@Composable
fun MainScreen(
    productViewModel: ProductViewModel,
    scannedBarcode: String,
    lastScanTime: Long,
    onScanButtonClick: () -> Unit
) {
    val searchResults by productViewModel.searchResults.collectAsState()
    var query by remember { mutableStateOf("") }
    var isDropdownVisible by remember { mutableStateOf(false) }
    var selectedProducts by remember { mutableStateOf(listOf<ProductData>()) }

    LaunchedEffect(selectedProducts) {
        Log.d("MainScreen", "selectedProducts updated. Count: ${selectedProducts.size}")
    }

    val columnSelection = remember { mutableStateOf(getInitialColumnSelection().toMap()) }

    val context = LocalContext.current
    val drawerState = rememberDrawerState(DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    LaunchedEffect(scannedBarcode, lastScanTime) {
        Log.d("MainScreen", "LaunchedEffect triggered with scannedBarcode: $scannedBarcode")
        if (scannedBarcode.isNotEmpty()) {
            query = scannedBarcode
            Log.d("MainScreen", "Updating query to: $query")
            productViewModel.searchByBarcodeOrTradeName(query)
            isDropdownVisible = true
        }
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            DrawerContent(columnSelection)
        }
    ) {
        Scaffold(
            topBar = {
                TopBarWithSearch(
                    query = query,
                    onQueryChange = { newQuery ->
                        query = newQuery
                        Log.d("MainScreen", "Query changed to: $query")
                        if (query.isNotEmpty()) {
                            productViewModel.searchByBarcodeOrTradeName(query)
                        }
                        isDropdownVisible = query.isNotEmpty() && searchResults.isNotEmpty()
                    },
                    isDropdownVisible = isDropdownVisible,
                    searchResults = searchResults,
                    onDropdownItemSelected = { result ->
                        Log.d("MainScreen", "Before adding product. Current count: ${selectedProducts.size}")
                        selectedProducts = selectedProducts + result
                        Log.d("MainScreen", "Added product to selection: ${result.tradeName}, Commons price: ${result.commonsPrice}")
                        isDropdownVisible = false
                    }
                )
            },
            floatingActionButton = {
                FloatingActionButtonWithPermission(onClick = onScanButtonClick)
            }
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clickable(
                        indication = null,
                        interactionSource = remember { MutableInteractionSource() }
                    ) {
                        isDropdownVisible = false
                    }
            ) {
                MainContent(
                    modifier = Modifier.padding(innerPadding),
                    productViewModel = productViewModel,
                    drawerState = drawerState,
                    selectedProducts = selectedProducts,
                    columnSelection = columnSelection,
                    onReset = {
                        productViewModel.resetSearch()
                        query = ""
                        selectedProducts = emptyList()
                        columnSelection.value = getInitialColumnSelection().toMutableMap()
                    }
                )
            }
        }
    }
}