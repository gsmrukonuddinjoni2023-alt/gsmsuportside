package com.example.ui

import android.app.DatePickerDialog
import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.*
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DokanKhataApp(viewModel: MainViewModel) {
    val context = LocalContext.current
    var currentTab by remember { mutableStateOf(0) }

    // Observers from ViewModel
    val products by viewModel.products.collectAsStateWithLifecycle()
    val sales by viewModel.sales.collectAsStateWithLifecycle()
    val purchases by viewModel.purchases.collectAsStateWithLifecycle()
    val expenses by viewModel.expenses.collectAsStateWithLifecycle()

    val totalSalesVal by viewModel.totalSalesVal.collectAsStateWithLifecycle()
    val totalExpensesVal by viewModel.totalExpensesVal.collectAsStateWithLifecycle()
    val totalStockVal by viewModel.totalStockVal.collectAsStateWithLifecycle()
    val estimatedProfitVal by viewModel.estimatedProfitVal.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(MaterialTheme.colorScheme.primaryContainer),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Storefront,
                                contentDescription = "দোকান খাতা লোগো",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                        Column {
                            Text(
                                text = "দোকান খাতা",
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "স্টক ও বিক্রয় খতিয়ান",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                },
                actions = {
                    Box(
                        modifier = Modifier
                            .padding(end = 12.dp)
                            .clip(RoundedCornerShape(4.dp))
                            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = "লাইভ ডাটাবেজ",
                            color = MaterialTheme.colorScheme.primary,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    scrolledContainerColor = MaterialTheme.colorScheme.surfaceColorAtElevation(3.dp)
                )
            )
        },
        bottomBar = {
            NavigationBar(
                modifier = Modifier.testTag("bottom_nav_bar"),
                tonalElevation = 8.dp
            ) {
                NavigationBarItem(
                    selected = currentTab == 0,
                    onClick = { currentTab = 0 },
                    icon = { Icon(Icons.Default.Storefront, contentDescription = "স্টক ও পণ্য") },
                    label = { Text("স্টক ও পণ্য") },
                    modifier = Modifier.testTag("tab_inventory")
                )
                NavigationBarItem(
                    selected = currentTab == 1,
                    onClick = { currentTab = 1 },
                    icon = { Icon(Icons.Default.ShoppingCart, contentDescription = "বিক্রয় (POS)") },
                    label = { Text("বিক্রয় (POS)") },
                    modifier = Modifier.testTag("tab_sales")
                )
                NavigationBarItem(
                    selected = currentTab == 2,
                    onClick = { currentTab = 2 },
                    icon = { Icon(Icons.Default.ShoppingBag, contentDescription = "কেনাকাটা") },
                    label = { Text("স্টক ইন") },
                    modifier = Modifier.testTag("tab_stock_in")
                )
                NavigationBarItem(
                    selected = currentTab == 3,
                    onClick = { currentTab = 3 },
                    icon = { Icon(Icons.Default.ReceiptLong, contentDescription = "খরচ") },
                    label = { Text("খরচ") },
                    modifier = Modifier.testTag("tab_expenses")
                )
            }
        },
        contentWindowInsets = WindowInsets.safeDrawing
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (currentTab) {
                0 -> InventoryTabScreen(
                    viewModel = viewModel,
                    products = products,
                    totalSales = totalSalesVal,
                    totalExpenses = totalExpensesVal,
                    totalStock = totalStockVal,
                    estimatedProfit = estimatedProfitVal
                )
                1 -> SalesTabScreen(
                    viewModel = viewModel,
                    products = products,
                    sales = sales
                )
                2 -> PurchaseTabScreen(
                    viewModel = viewModel,
                    products = products,
                    purchases = purchases
                )
                3 -> ExpenseTabScreen(
                    viewModel = viewModel,
                    expenses = expenses
                )
            }
        }
    }
}

// =====================================================================
// 1. INVENTORY TAB SCREEN (Dashboard Stats + Add Product + Product List)
// =====================================================================
@Composable
fun InventoryTabScreen(
    viewModel: MainViewModel,
    products: List<Product>,
    totalSales: Double,
    totalExpenses: Double,
    totalStock: Double,
    estimatedProfit: Double
) {
    var showAddProductForm by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }

    val filteredProducts = products.filter {
        it.name.contains(searchQuery, ignoreCase = true) ||
                it.category.contains(searchQuery, ignoreCase = true) ||
                it.supplierName.contains(searchQuery, ignoreCase = true)
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(top = 8.dp, bottom = 24.dp)
    ) {
        // A. Header section
        item {
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "শুভ দিন!",
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "আপনার দোকানের হিসাব আজ চমৎকারভাবে সুরক্ষিত আছে। নিচে আপনার সামগ্রিক খতিয়ান দেখুন।",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Icon(
                        imageVector = Icons.Default.Storefront,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.8f),
                        modifier = Modifier.size(56.dp)
                    )
                }
            }
        }

        // B. Dashboard indicators
        item {
            Text(
                text = "দোকানের আর্থিক খতিয়ান (Dashboard)",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
        }

        item {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                // Row 1
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    DashboardCard(
                        title = "সর্বমোট বিক্রয়",
                        value = "৳ ${formatTaka(totalSales)}",
                        icon = Icons.Default.ShoppingCart,
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier.weight(1f)
                    )
                    DashboardCard(
                        title = "মোট সংগৃহীত স্টক মূল্য",
                        value = "৳ ${formatTaka(totalStock)}",
                        icon = Icons.Default.Home,
                        containerColor = MaterialTheme.colorScheme.secondaryContainer,
                        contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
                        modifier = Modifier.weight(1f)
                    )
                }

                // Row 2
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    DashboardCard(
                        title = "সর্বমোট খরচ",
                        value = "৳ ${formatTaka(totalExpenses)}",
                        icon = Icons.Default.ReceiptLong,
                        containerColor = MaterialTheme.colorScheme.errorContainer,
                        contentColor = MaterialTheme.colorScheme.onErrorContainer,
                        modifier = Modifier.weight(1f)
                    )
                    DashboardCard(
                        title = "প্রাক্কলিত লাভ (Profit)",
                        value = "৳ ${formatTaka(estimatedProfit)}",
                        icon = Icons.Default.TrendingUp,
                        containerColor = if (estimatedProfit >= 0) {
                            Color(0xFFE2F6EA)
                        } else {
                            Color(0xFFFBEAEA)
                        },
                        contentColor = if (estimatedProfit >= 0) {
                            Color(0xFF1B5E20)
                        } else {
                            MaterialTheme.colorScheme.error
                        },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        // C. Expandable add new product form
        item {
            ElevatedCard(
                onClick = { showAddProductForm = !showAddProductForm },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("add_product_toggle_card"),
                colors = CardDefaults.elevatedCardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = "নতুন পণ্য যোগ করুন (Add Product Form)",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Icon(
                        imageVector = if (showAddProductForm) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                        contentDescription = "ফর্ম খুলুন/বন্ধ করুন"
                    )
                }
            }
        }

        item {
            AnimatedVisibility(
                visible = showAddProductForm,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut()
            ) {
                AddProductForm(viewModel = viewModel) {
                    showAddProductForm = false
                }
            }
        }

        // D. List search
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "পণ্যের তালিকা ও স্টক স্তর",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Badge(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                ) {
                    Text(
                        text = "মোট: ${products.size}টি",
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }
        }

        item {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                label = { Text("পণ্যের নাম বা ক্যাটাগরি দিয়ে খুঁজুন") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = "সন্ধান করুন") },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { searchQuery = "" }) {
                            Icon(Icons.Default.Clear, contentDescription = "পরিষ্কার করুন")
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("product_search_input"),
                shape = RoundedCornerShape(12.dp)
            )
        }

        // E. Products List or Empty State
        if (filteredProducts.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp)
                        .border(
                            1.dp,
                            MaterialTheme.colorScheme.outlineVariant,
                            RoundedCornerShape(16.dp)
                        )
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.SearchOff,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                            modifier = Modifier.size(48.dp)
                        )
                        Text(
                            text = if (products.isEmpty()) "কোন পণ্য এখনও নিবন্ধিত হয়নি।" else "অনুসন্ধানের ভিত্তিতে কোন পণ্য পাওয়া যায়নি।",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = if (products.isEmpty()) "নতুন পণ্য যুক্ত করতে উপরের ফর্মে ক্লিক করুন।" else "সঠিক নাম বা ক্যাটাগরি লিখে পুনরায় চেষ্টা করুন।",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        } else {
            items(filteredProducts, key = { it.id }) { product ->
                ProductItemCard(
                    product = product,
                    onDelete = { viewModel.deleteProduct(product) }
                )
            }
        }
    }
}

@Composable
fun DashboardCard(
    title: String,
    value: String,
    icon: ImageVector,
    containerColor: Color,
    contentColor: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = containerColor,
            contentColor = contentColor
        ),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Medium,
                    color = contentColor.copy(alpha = 0.8f)
                )
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp),
                    tint = contentColor.copy(alpha = 0.9f)
                )
            }
            Text(
                text = value,
                fontSize = 18.sp,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddProductForm(
    viewModel: MainViewModel,
    onFinished: () -> Unit
) {
    val context = LocalContext.current

    var name by remember { mutableStateOf("") }
    var categoryExpanded by remember { mutableStateOf(false) }
    var selectedCategory by remember { mutableStateOf("") }
    var purchasePrice by remember { mutableStateOf("") }
    var sellingPrice by remember { mutableStateOf("") }
    var stockQuantity by remember { mutableStateOf("") }
    var unitExpanded by remember { mutableStateOf(false) }
    var selectedUnit by remember { mutableStateOf("") }
    var supplierName by remember { mutableStateOf("") }

    ElevatedCard(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("add_product_form_container"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "১. পণ্য ডাটা এন্ট্রি ফর্ম (Product Management)",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )

            // Name
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("পণ্যের নাম * (যেমন: ডানো দুধ)") },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("form_product_name"),
                singleLine = true,
                shape = RoundedCornerShape(10.dp)
            )

            // Category Dropdown
            ExposedDropdownMenuBox(
                expanded = categoryExpanded,
                onExpandedChange = { categoryExpanded = !categoryExpanded }
            ) {
                OutlinedTextField(
                    value = selectedCategory,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("ক্যাটাগরি নির্ধারণ করুন *") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = categoryExpanded) },
                    colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(),
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor()
                        .testTag("form_product_category"),
                    shape = RoundedCornerShape(10.dp)
                )
                ExposedDropdownMenu(
                    expanded = categoryExpanded,
                    onDismissRequest = { categoryExpanded = false }
                ) {
                    viewModel.categories.forEach { option ->
                        DropdownMenuItem(
                            text = { Text(option) },
                            onClick = {
                                selectedCategory = option
                                categoryExpanded = false
                            },
                            modifier = Modifier.testTag("category_option_$option")
                        )
                    }
                }
            }

            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                // Purchase Price
                OutlinedTextField(
                    value = purchasePrice,
                    onValueChange = { purchasePrice = it },
                    label = { Text("ক্রয় মূল্য (৳) *") },
                    modifier = Modifier
                        .weight(1f)
                        .testTag("form_product_purchase_price"),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    shape = RoundedCornerShape(10.dp)
                )

                // Selling Price
                OutlinedTextField(
                    value = sellingPrice,
                    onValueChange = { sellingPrice = it },
                    label = { Text("বিক্রয় মূল্য (৳) *") },
                    modifier = Modifier
                        .weight(1f)
                        .testTag("form_product_selling_price"),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    shape = RoundedCornerShape(10.dp)
                )
            }

            Row(horizontalArrangement = Arrangement.spacedBy(10.dp), verticalAlignment = Alignment.CenterVertically) {
                // Stock Qty
                OutlinedTextField(
                    value = stockQuantity,
                    onValueChange = { stockQuantity = it },
                    label = { Text("বর্তমান স্টক পরিমাণ *") },
                    modifier = Modifier
                        .weight(1f)
                        .testTag("form_product_stock"),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    shape = RoundedCornerShape(10.dp)
                )

                // Unit Dropdown
                ExposedDropdownMenuBox(
                    expanded = unitExpanded,
                    onExpandedChange = { unitExpanded = !unitExpanded },
                    modifier = Modifier.weight(1f)
                ) {
                    OutlinedTextField(
                        value = selectedUnit,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("ইউনিট নির্বাচন করুন *") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = unitExpanded) },
                        colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(),
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor()
                            .testTag("form_product_unit"),
                        shape = RoundedCornerShape(10.dp)
                    )
                    ExposedDropdownMenu(
                        expanded = unitExpanded,
                        onDismissRequest = { unitExpanded = false }
                    ) {
                        viewModel.units.forEach { option ->
                            DropdownMenuItem(
                                text = { Text(option) },
                                onClick = {
                                    selectedUnit = option
                                    unitExpanded = false
                                },
                                modifier = Modifier.testTag("unit_option_$option")
                            )
                        }
                    }
                }
            }

            // Supplier Name
            OutlinedTextField(
                value = supplierName,
                onValueChange = { supplierName = it },
                label = { Text("সাপ্লায়ারের নাম (ঐচ্ছিক)") },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("form_product_supplier"),
                singleLine = true,
                shape = RoundedCornerShape(10.dp)
            )

            Button(
                onClick = {
                    val pPrice = purchasePrice.toDoubleOrNull() ?: 0.0
                    val sPrice = sellingPrice.toDoubleOrNull() ?: 0.0
                    val sQty = stockQuantity.toDoubleOrNull() ?: 0.0

                    viewModel.addProduct(
                        name = name,
                        category = selectedCategory,
                        purchasePrice = pPrice,
                        sellingPrice = sPrice,
                        stockQuantity = sQty,
                        unit = selectedUnit,
                        supplierName = supplierName,
                        onSuccess = {
                            Toast.makeText(context, "সফলভাবে পণ্যটি যুক্ত করা হয়েছে!", Toast.LENGTH_SHORT).show()
                            // Clear states
                            name = ""
                            selectedCategory = ""
                            purchasePrice = ""
                            sellingPrice = ""
                            stockQuantity = ""
                            selectedUnit = ""
                            supplierName = ""
                            onFinished()
                        },
                        onError = { errorText ->
                            Toast.makeText(context, "ভুল: $errorText", Toast.LENGTH_LONG).show()
                        }
                    )
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp)
                    .testTag("form_product_submit_button")
            ) {
                Icon(Icons.Default.Check, contentDescription = null)
                Spacer(modifier = Modifier.width(6.dp))
                Text("পণ্য ডাটাবেজে সংরক্ষণ করুন")
            }
        }
    }
}

@Composable
fun ProductItemCard(
    product: Product,
    onDelete: () -> Unit
) {
    var showDeleteConfirmDialog by remember { mutableStateOf(false) }

    if (showDeleteConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirmDialog = false },
            title = { Text("নিশ্চিত অপসারণ?") },
            text = { Text("'${product.name}' পণ্যটি কি আপনি অপসারণ করতে চান? অপসারণের পর সমস্ত বিবরণ মুছে যাবে।") },
            confirmButton = {
                TextButton(onClick = {
                    onDelete()
                    showDeleteConfirmDialog = false
                }) {
                    Text("অপসারণ করুন", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirmDialog = false }) {
                    Text("বাতিল করুন")
                }
            }
        )
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("product_card_${product.id}"),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        shape = RoundedCornerShape(14.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header Row: Product Name + Category pill + Delete Icon
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = product.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = "ক্যাটাগরি: ${product.category}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                IconButton(
                    onClick = { showDeleteConfirmDialog = true },
                    modifier = Modifier.testTag("delete_product_${product.id}")
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "পণ্য অপসারণ",
                        tint = MaterialTheme.colorScheme.error.copy(alpha = 0.8f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))
            Divider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
            Spacer(modifier = Modifier.height(10.dp))

            // Details Section
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Pricing
                Column {
                    Text(
                        text = "বিক্রয় মূল্য: ৳ ${formatPrice(product.sellingPrice)} / ${product.unit.takeBeforeBracket()}",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "ক্রয় মূল্য: ৳ ${formatPrice(product.purchasePrice)}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                // Stock Indicator Badge
                val isOutOfStock = product.stockQuantity <= 0
                val isLowStock = product.stockQuantity > 0 && product.stockQuantity < 10
                val badgeColor = when {
                    isOutOfStock -> Color(0xFFFBEAEA)
                    isLowStock -> Color(0xFFFFF6E6)
                    else -> Color(0xFFE2F6EA)
                }
                val textColor = when {
                    isOutOfStock -> Color(0xFFC62828)
                    isLowStock -> Color(0xFFEF6C00)
                    else -> Color(0xFF2E7D32)
                }
                val textLabel = when {
                    isOutOfStock -> "স্টক আউট!"
                    isLowStock -> "কম স্টক"
                    else -> "পর্যাপ্ত স্টক"
                }

                Column(horizontalAlignment = Alignment.End) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(badgeColor)
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = textLabel,
                            color = textColor,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "স্টক: ${formatPrice(product.stockQuantity)} ${product.unit.takeBeforeBracket()}",
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }

            if (product.supplierName.isNotBlank()) {
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = "সাপ্লায়ার",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                        modifier = Modifier.size(14.dp)
                    )
                    Text(
                        text = "সরবরাহকারী: ${product.supplierName}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                    )
                }
            }
        }
    }
}

// =====================================================================
// 2. SALES / POS TAB SCREEN
// =====================================================================
@Composable
fun SalesTabScreen(
    viewModel: MainViewModel,
    products: List<Product>,
    sales: List<Sale>
) {
    val context = LocalContext.current

    var selectedProduct by remember { mutableStateOf<Product?>(null) }
    var quantityStr by remember { mutableStateOf("") }
    var discountStr by remember { mutableStateOf("") }
    var customerNamePhone by remember { mutableStateOf("") }
    var selectedPaymentMethod by remember { mutableStateOf("Cash (নগদ)") }
    var showProductSearchDialog by remember { mutableStateOf(false) }

    // Dynamic pricing calculations
    val quantity = quantityStr.toDoubleOrNull() ?: 0.0
    val discount = discountStr.toDoubleOrNull() ?: 0.0
    val itemPrice = selectedProduct?.sellingPrice ?: 0.0
    val subtotal = itemPrice * quantity
    val finalTotal = maxOf(0.0, subtotal - discount)

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(top = 8.dp, bottom = 24.dp)
    ) {
        // A. Form Card
        item {
            ElevatedCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("pos_form_card"),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "২. বিক্রয় রসিদ বা ইনভয়েস জেনারেটর (Sales/POS)",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )

                    // Auto Date/Time Box
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.4f))
                            .padding(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.CalendarToday,
                                    contentDescription = "তারিখ",
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(16.dp)
                                )
                                Text(
                                    text = "তারিখ ও সময়:",
                                    style = MaterialTheme.typography.bodySmall,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Text(
                                text = SimpleDateFormat("dd MMMM, yyyy h:mm a", Locale.getDefault()).format(Date()),
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }

                    // Product Selector Clickable Slot
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(
                                1.dp,
                                MaterialTheme.colorScheme.primary.copy(alpha = 0.5f),
                                RoundedCornerShape(10.dp)
                            )
                            .clip(RoundedCornerShape(10.dp))
                            .clickable { showProductSearchDialog = true }
                            .padding(16.dp)
                            .testTag("pos_product_selector")
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.ManageSearch,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary
                                )
                                if (selectedProduct == null) {
                                    Column {
                                        Text(
                                            text = "পণ্য নির্বাচন করুন *",
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                        Text(
                                            text = "এখানে ক্লিক করে অনুসন্ধান করুন",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                } else {
                                    Column {
                                        Text(
                                            text = selectedProduct!!.name,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                        Text(
                                            text = "ক্যাটাগরি: ${selectedProduct!!.category} | বর্তমান স্টক: ${selectedProduct!!.stockQuantity} ${selectedProduct!!.unit}",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            }
                            Icon(
                                imageVector = Icons.Default.ArrowDropDown,
                                contentDescription = "পণ্য বুকিং মেনু"
                            )
                        }
                    }

                    // Selected product helper quantities
                    if (selectedProduct != null) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            // Read-only selling price
                            OutlinedTextField(
                                value = "৳ ${formatPrice(selectedProduct!!.sellingPrice)}",
                                onValueChange = {},
                                readOnly = true,
                                label = { Text("একক বিক্রয় মূল্য (Read-only)") },
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("pos_product_fixed_unit_price"),
                                shape = RoundedCornerShape(10.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                                    disabledTextColor = MaterialTheme.colorScheme.primary
                                ),
                                enabled = false
                            )

                            // Editable quantity
                            OutlinedTextField(
                                value = quantityStr,
                                onValueChange = { quantityStr = it },
                                label = { Text("বিক্রয়ের পরিমাণ *") },
                                supportingText = { Text("বর্তমান স্টক: ${selectedProduct!!.stockQuantity}") },
                                placeholder = { Text("উদা: ৫") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("pos_product_sale_quantity"),
                                shape = RoundedCornerShape(10.dp)
                            )
                        }
                    }

                    // Discount Amount
                    OutlinedTextField(
                        value = discountStr,
                        onValueChange = { discountStr = it },
                        label = { Text("ছাড় বা ডিসকাউন্ট (৳, ঐচ্ছিক)") },
                        placeholder = { Text("উদা: ১০০") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("pos_product_discount"),
                        shape = RoundedCornerShape(10.dp)
                    )

                    // Customer info
                    OutlinedTextField(
                        value = customerNamePhone,
                        onValueChange = { customerNamePhone = it },
                        label = { Text("গ্রাহকের নাম/মোবাইল নং (ঐচ্ছিক)") },
                        placeholder = { Text("উদা: আল-আমিন বা ০১৭০০-০০০০০০") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("pos_product_customer_info"),
                        shape = RoundedCornerShape(10.dp),
                        singleLine = true
                    )

                    // Payment method selection
                    Column {
                        Text(
                            text = "পেমেন্ট মেথড (Payment Method) *",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(bottom = 6.dp)
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            viewModel.paymentMethods.forEach { method ->
                                val isSelected = selectedPaymentMethod == method
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(
                                            if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant.copy(
                                                alpha = 0.5f
                                            )
                                        )
                                        .clickable { selectedPaymentMethod = method }
                                        .padding(vertical = 10.dp, horizontal = 4.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = method.substringBefore(" ("), // shortened label for screen space
                                        color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 11.sp,
                                        textAlign = TextAlign.Center
                                    )
                                }
                            }
                        }
                    }

                    Divider(color = MaterialTheme.colorScheme.outlineVariant)

                    // Dynamic Invoice Pricing Sum Display Card
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f)
                        ),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "উপ-মোট (Subtotal):",
                                    style = MaterialTheme.typography.bodySmall
                                )
                                Text(
                                    text = "৳ ${formatPrice(subtotal)}",
                                    style = MaterialTheme.typography.bodySmall,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "ডিসকাউন্ট ছাড় (-):",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.error
                                )
                                Text(
                                    text = "৳ ${formatPrice(discount)}",
                                    style = MaterialTheme.typography.bodySmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.error
                                )
                            }
                            Divider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "সর্বমোট প্রদেয় মূল্য (Total Price):",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "৳ ${formatPrice(finalTotal)}",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Black,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    }

                    // Confirm buttons
                    Button(
                        onClick = {
                            val prod = selectedProduct
                            if (prod == null) {
                                Toast.makeText(context, "অনুগ্রহ করে প্রথমে পণ্য নির্বাচন করুন", Toast.LENGTH_SHORT).show()
                                return@Button
                            }
                            viewModel.makeSale(
                                productId = prod.id,
                                quantity = quantity,
                                discount = discount,
                                customerNamePhone = customerNamePhone,
                                paymentMethod = selectedPaymentMethod,
                                onSuccess = {
                                    Toast.makeText(context, "বিক্রয় হিসাব সফলভাবে নথিভুক্ত হয়েছে!", Toast.LENGTH_SHORT).show()
                                    // Reset UI state
                                    selectedProduct = null
                                    quantityStr = ""
                                    discountStr = ""
                                    customerNamePhone = ""
                                    selectedPaymentMethod = "Cash (নগদ)"
                                },
                                onError = { errorText ->
                                    Toast.makeText(context, "ভুল: $errorText", Toast.LENGTH_LONG).show()
                                }
                            )
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("pos_product_submit_button"),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(Icons.Default.ShoppingCartCheckout, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("বিক্রয় নিশ্চিত ও মেমো তৈরি করুন")
                    }
                }
            }
        }

        // B. Sales History logs
        item {
            Text(
                text = "বিক্রয় ও ইনভয়েস ইতিহাস (Sales History)",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(top = 8.dp)
            )
        }

        if (sales.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                        .border(
                            1.dp,
                            MaterialTheme.colorScheme.outlineVariant,
                            RoundedCornerShape(12.dp)
                        )
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "আজ কোন বিকিকিনি বা ইনভয়েস সম্পন্ন হয়নি।",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        } else {
            items(sales, key = { it.id }) { sale ->
                SalesHistoryItemCard(sale = sale, onDelete = { viewModel.deleteSale(sale) })
            }
        }
    }

    // Search dialog for Product Selector
    if (showProductSearchDialog) {
        ProductSelectorDialog(
            products = products,
            onDismiss = { showProductSearchDialog = false },
            onSelected = {
                selectedProduct = it
                showProductSearchDialog = false
            }
        )
    }
}

@Composable
fun SalesHistoryItemCard(
    sale: Sale,
    onDelete: () -> Unit
) {
    var showDeleteConfirm by remember { mutableStateOf(false) }

    if (showDeleteConfirm) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            title = { Text("ইনভয়েস বাতিল করবেন?") },
            text = { Text("এই বিক্রয় মেমো অপসারণ বা বাতিল করলে বিক্রয় মূল্য বিয়োগ হবে এবং সংশ্লিষ্ট পণ্যের স্টক পুনরায় পুনরুদ্ধার (rollbacked) হবে। আপনি কি নিশ্চিত?") },
            confirmButton = {
                TextButton(onClick = {
                    onDelete()
                    showDeleteConfirm = false
                }) {
                    Text("নিশ্চিত বাতিল করুন", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirm = false }) {
                    Text("বাতিল করুন")
                }
            }
        )
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("sale_history_card_${sale.id}"),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = sale.productName,
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = SimpleDateFormat("dd/MM/yyyy, hh:mm a", Locale.getDefault()).format(Date(sale.timestamp)),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .background(MaterialTheme.colorScheme.primaryContainer)
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = sale.paymentMethod,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
            Divider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
            Spacer(modifier = Modifier.height(8.dp))

            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "পরিমাণ: ${formatPrice(sale.quantity)} পিস/কেজি | একক মূল্য: ৳ ${formatPrice(sale.unitPrice)}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "টানা উপমোট: ৳ ${formatPrice(sale.unitPrice * sale.quantity)}",
                        style = MaterialTheme.typography.bodySmall
                    )
                }
                if (sale.discount > 0) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "ডিসকাউন্ট বা ছাড়:",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.error
                        )
                        Text(
                            text = "- ৳ ${formatPrice(sale.discount)}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.error,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
                if (sale.customerNamePhone.isNotBlank()) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Person,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(12.dp)
                            )
                            Text(
                                text = "গ্রাহক: ${sale.customerNamePhone}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
            Divider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "মোট মেমো মূল্য: ",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = "৳ ${formatPrice(sale.totalPrice)}",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                IconButton(
                    onClick = { showDeleteConfirm = true },
                    modifier = Modifier
                        .size(32.dp)
                        .testTag("delete_sale_history_${sale.id}")
                ) {
                    Icon(
                        imageVector = Icons.Default.DeleteSweep,
                        contentDescription = "রসিদ বাতিল ও মুছে দিন",
                        tint = MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}

// =====================================================================
// 3. PURCHASE / STOCK-IN TAB SCREEN
// =====================================================================
@Composable
fun PurchaseTabScreen(
    viewModel: MainViewModel,
    products: List<Product>,
    purchases: List<Purchase>
) {
    val context = LocalContext.current

    var supplierName by remember { mutableStateOf("") }
    var memoNo by remember { mutableStateOf("") }
    var selectedProduct by remember { mutableStateOf<Product?>(null) }
    var quantityStr by remember { mutableStateOf("") }
    var purchasePriceStr by remember { mutableStateOf("") }
    var selectedPaymentStatus by remember { mutableStateOf("Paid (পরিশোধিত)") }
    var showProductSearchDialog by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(top = 8.dp, bottom = 24.dp)
    ) {
        // A. Form Card
        item {
            ElevatedCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("purchase_form_card"),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "৩. নতুন কেনাকাটা বা স্টক ইন (Purchase/Stock In)",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )

                    // Supplier Name
                    OutlinedTextField(
                        value = supplierName,
                        onValueChange = { supplierName = it },
                        label = { Text("সাপ্লায়ার বা সরবরাহকারীর নাম *") },
                        placeholder = { Text("উদা: আবুল ট্রেডার্স") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("purchase_supplier_name"),
                        shape = RoundedCornerShape(10.dp),
                        singleLine = true
                    )

                    // Memo No
                    OutlinedTextField(
                        value = memoNo,
                        onValueChange = { memoNo = it },
                        label = { Text("চালান নম্বর বা মেমো নং (Memo No) *") },
                        placeholder = { Text("উদা: চালান-৪৩২১") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("purchase_memo_no"),
                        shape = RoundedCornerShape(10.dp),
                        singleLine = true
                    )

                    // Product Selector Clickable Slot
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(
                                1.dp,
                                MaterialTheme.colorScheme.primary.copy(alpha = 0.5f),
                                RoundedCornerShape(10.dp)
                            )
                            .clip(RoundedCornerShape(10.dp))
                            .clickable { showProductSearchDialog = true }
                            .padding(16.dp)
                            .testTag("purchase_product_selector")
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.ManageSearch,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary
                                )
                                if (selectedProduct == null) {
                                    Column {
                                        Text(
                                            text = "স্টক বাড়াতে পণ্য নির্বাচন করুন *",
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                        Text(
                                            text = "এখানে ক্লিক করে অনুসন্ধান করুন",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                } else {
                                    Column {
                                        Text(
                                            text = selectedProduct!!.name,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                        Text(
                                            text = "ক্যাটাগরি: ${selectedProduct!!.category} | বর্তমান স্টক: ${selectedProduct!!.stockQuantity} ${selectedProduct!!.unit}",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            }
                            Icon(
                                imageVector = Icons.Default.ArrowDropDown,
                                contentDescription = "পণ্য বুকিং মেনু"
                            )
                        }
                    }

                    // Product selection details setup
                    if (selectedProduct != null) {
                        // Prefill purchase price if possible
                        LaunchedEffect(selectedProduct) {
                            if (purchasePriceStr.isEmpty()) {
                                purchasePriceStr = selectedProduct!!.purchasePrice.toString()
                            }
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            // Quantity
                            OutlinedTextField(
                                value = quantityStr,
                                onValueChange = { quantityStr = it },
                                label = { Text("নতুন চালানের পরিমাণ *") },
                                suffix = { Text(selectedProduct!!.unit.takeBeforeBracket()) },
                                placeholder = { Text("উদা: ৫০") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("purchase_quantity"),
                                shape = RoundedCornerShape(10.dp)
                            )

                            // Purchase Price
                            OutlinedTextField(
                                value = purchasePriceStr,
                                onValueChange = { purchasePriceStr = it },
                                label = { Text("নতুন ক্রয় মুল্য (৳) *") },
                                placeholder = { Text("উদা: ২৫০") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("purchase_price"),
                                shape = RoundedCornerShape(10.dp)
                            )
                        }
                    }

                    // Payment status selection
                    Column {
                        Text(
                            text = "পেমেন্ট স্ট্যাটাস (Payment Status) *",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(bottom = 6.dp)
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            viewModel.paymentStatuses.forEach { status ->
                                val isSelected = selectedPaymentStatus == status
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(
                                            if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant.copy(
                                                alpha = 0.5f
                                            )
                                        )
                                        .clickable { selectedPaymentStatus = status }
                                        .padding(vertical = 12.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = status,
                                        color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.sp
                                    )
                                }
                            }
                        }
                    }

                    // Bottom info label
                    val qty = quantityStr.toDoubleOrNull() ?: 0.0
                    val rate = purchasePriceStr.toDoubleOrNull() ?: 0.0
                    val rowSum = qty * rate
                    if (rowSum > 0) {
                        Text(
                            text = "সংগৃহীত মোট স্টক ক্রয় হিসাব: ৳ ${formatPrice(rowSum)}",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(vertical = 4.dp)
                        )
                    }

                    Button(
                        onClick = {
                            val prod = selectedProduct
                            if (prod == null) {
                                Toast.makeText(context, "অনুগ্রহ করে পণ্য নির্বাচন করুন", Toast.LENGTH_SHORT).show()
                                return@Button
                            }
                            val pQty = quantityStr.toDoubleOrNull() ?: 0.0
                            val pPrice = purchasePriceStr.toDoubleOrNull() ?: 0.0

                            viewModel.makePurchase(
                                supplierName = supplierName,
                                memoNo = memoNo,
                                productId = prod.id,
                                quantity = pQty,
                                purchasePrice = pPrice,
                                paymentStatus = selectedPaymentStatus,
                                onSuccess = {
                                    Toast.makeText(context, "স্টক বাড়ানো ও ক্রয় সফল হয়েছে!", Toast.LENGTH_SHORT).show()
                                    // Reset UI states
                                    supplierName = ""
                                    memoNo = ""
                                    selectedProduct = null
                                    quantityStr = ""
                                    purchasePriceStr = ""
                                    selectedPaymentStatus = "Paid (পরিশোধিত)"
                                },
                                onError = { errorText ->
                                    Toast.makeText(context, "ভুল: $errorText", Toast.LENGTH_LONG).show()
                                }
                            )
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("purchase_submit_button"),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(Icons.Default.AddCircleOutline, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("স্টক আপডেট ও ইন-করুণ")
                    }
                }
            }
        }

        // B. Purchase History
        item {
            Text(
                text = "ক্রয় ও স্টক রিসিভ খতিয়ান (Stock In History)",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(top = 8.dp)
            )
        }

        if (purchases.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                        .border(
                            1.dp,
                            MaterialTheme.colorScheme.outlineVariant,
                            RoundedCornerShape(12.dp)
                        )
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "ক্রয় বা সংগৃহীত মেমোর কোন ইতিহাস খুঁজে পাওয়া যায়নি।",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        } else {
            items(purchases, key = { it.id }) { purchase ->
                PurchaseHistoryItemCard(
                    purchase = purchase,
                    onDelete = { viewModel.deletePurchase(purchase) }
                )
            }
        }
    }

    // Search dialog for Product Selector in Purchase
    if (showProductSearchDialog) {
        ProductSelectorDialog(
            products = products,
            onDismiss = { showProductSearchDialog = false },
            onSelected = {
                selectedProduct = it
                showProductSearchDialog = false
            }
        )
    }
}

@Composable
fun PurchaseHistoryItemCard(
    purchase: Purchase,
    onDelete: () -> Unit
) {
    var showDeleteConfirm by remember { mutableStateOf(false) }

    if (showDeleteConfirm) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            title = { Text("ক্রয় সংরক্ষণ বাতিল করবেন?") },
            text = { Text("এই মেমো মুছে দিলে যুক্ত করা স্টক পুনরায় বিয়োগ হবে (rollbacked)। আপনি কি নিশ্চিত?") },
            confirmButton = {
                TextButton(onClick = {
                    onDelete()
                    showDeleteConfirm = false
                }) {
                    Text("নিশ্চিত অপসারণ করুন", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirm = false }) {
                    Text("বাতিল করুন")
                }
            }
        )
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("purchase_history_card_${purchase.id}"),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = purchase.productName,
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "মেমো নং: ${purchase.memoNo} | " + SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date(purchase.timestamp)),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .background(
                            if (purchase.paymentStatus.contains("Due")) Color(0xFFFBEAEA) else Color(0xFFE2F6EA)
                        )
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = purchase.paymentStatus,
                        color = if (purchase.paymentStatus.contains("Due")) Color(0xFFC62828) else Color(0xFF2E7D32),
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))
            Divider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
            Spacer(modifier = Modifier.height(10.dp))

            // Quantities
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "সরবরাহকারী: ${purchase.supplierName}",
                        style = MaterialTheme.typography.bodySmall,
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "পরিমাণ: ${formatPrice(purchase.quantity)} | ক্রয় মূল্য: ৳ ${formatPrice(purchase.purchasePrice)}",
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Medium
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = "মোট ক্রয় ব্যয়",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "৳ ${formatPrice(purchase.quantity * purchase.purchasePrice)}",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }

                    IconButton(
                        onClick = { showDeleteConfirm = true },
                        modifier = Modifier
                            .size(32.dp)
                            .testTag("delete_purchase_history_${purchase.id}")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "অপসারণ করুন",
                            tint = MaterialTheme.colorScheme.error.copy(alpha = 0.8f)
                        )
                    }
                }
            }
        }
    }
}

// =====================================================================
// 4. EXPENSE TAB SCREEN
// =====================================================================
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExpenseTabScreen(
    viewModel: MainViewModel,
    expenses: List<Expense>
) {
    val context = LocalContext.current

    var amountStr by remember { mutableStateOf("") }
    var noteStr by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("") }
    var expenseCategoryExpanded by remember { mutableStateOf(false) }

    // Date Tracker State
    val calendar = remember { Calendar.getInstance() }
    var dateMillis by remember { mutableStateOf(calendar.timeInMillis) }
    val formattedDateString = remember(dateMillis) {
        SimpleDateFormat("dd MMMM, yyyy", Locale.getDefault()).format(Date(dateMillis))
    }

    // Initialize date picker dialog
    val datePickerDialog = remember {
        DatePickerDialog(
            context,
            { _, year, month, dayOfMonth ->
                val newCal = Calendar.getInstance()
                newCal.set(year, month, dayOfMonth)
                dateMillis = newCal.timeInMillis
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        )
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(top = 8.dp, bottom = 24.dp)
    ) {
        // A. Form Card
        item {
            ElevatedCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("expense_form_card"),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "৪. দোকানের খরচ ব্যবস্থাপনা (Expense Tracking)",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )

                    // Expense Category Drops
                    ExposedDropdownMenuBox(
                        expanded = expenseCategoryExpanded,
                        onExpandedChange = { expenseCategoryExpanded = !expenseCategoryExpanded }
                    ) {
                        OutlinedTextField(
                            value = selectedCategory,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("খরচের খাত নির্বাচন করুন *") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expenseCategoryExpanded) },
                            colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(),
                            modifier = Modifier
                                .fillMaxWidth()
                                .menuAnchor()
                                .testTag("expense_category_selector"),
                            shape = RoundedCornerShape(10.dp)
                        )
                        ExposedDropdownMenu(
                            expanded = expenseCategoryExpanded,
                            onDismissRequest = { expenseCategoryExpanded = false }
                        ) {
                            viewModel.expenseCategories.forEach { option ->
                                DropdownMenuItem(
                                    text = { Text(option) },
                                    onClick = {
                                        selectedCategory = option
                                        expenseCategoryExpanded = false
                                    },
                                    modifier = Modifier.testTag("expense_category_option_$option")
                                )
                            }
                        }
                    }

                    // Expense Amount
                    OutlinedTextField(
                        value = amountStr,
                        onValueChange = { amountStr = it },
                        label = { Text("খরচের পরিমাণ (টাকা) *") },
                        placeholder = { Text("৳ উদা: ৫০০") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("expense_amount"),
                        shape = RoundedCornerShape(10.dp),
                        singleLine = true
                    )

                    // Date Picker Clicking Area
                    OutlinedTextField(
                        value = formattedDateString,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("খরচের তারিখ *") },
                        trailingIcon = {
                            IconButton(onClick = { datePickerDialog.show() }) {
                                Icon(Icons.Default.CalendarMonth, contentDescription = "তারিখ নির্বাচন")
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { datePickerDialog.show() }
                            .testTag("expense_date_picker"),
                        shape = RoundedCornerShape(10.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            disabledContainerColor = Color.Transparent,
                            disabledTextColor = MaterialTheme.colorScheme.onSurface
                        ),
                        enabled = false
                    )

                    // Comments Notes
                    OutlinedTextField(
                        value = noteStr,
                        onValueChange = { noteStr = it },
                        label = { Text("মন্তব্য / খরচের বিবরণ (ঐচ্ছিক)") },
                        placeholder = { Text("উদা: মে মাসের দোকান লাইট বিল") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("expense_comments"),
                        shape = RoundedCornerShape(10.dp)
                    )

                    Button(
                        onClick = {
                            val expAmt = amountStr.toDoubleOrNull() ?: 0.0
                            viewModel.addExpense(
                                category = selectedCategory,
                                amount = expAmt,
                                notes = noteStr,
                                dateMillis = dateMillis,
                                onSuccess = {
                                    Toast.makeText(context, "খরচের হিসাব সংরক্ষণ হয়েছে!", Toast.LENGTH_SHORT).show()
                                    // Reset inputs
                                    selectedCategory = ""
                                    amountStr = ""
                                    noteStr = ""
                                    dateMillis = Calendar.getInstance().timeInMillis
                                },
                                onError = { errorText ->
                                    Toast.makeText(context, "ভুল: $errorText", Toast.LENGTH_LONG).show()
                                }
                            )
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("expense_submit_button"),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(Icons.Default.Save, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("খরচ হিসাব সংরক্ষণ করুন")
                    }
                }
            }
        }

        // B. Expense History
        item {
            Text(
                text = "দোকানের খরচের খতিয়ান খাতসমূহ (Expense Log)",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(top = 8.dp)
            )
        }

        if (expenses.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                        .border(
                            1.dp,
                            MaterialTheme.colorScheme.outlineVariant,
                            RoundedCornerShape(12.dp)
                        )
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "দোকানের খরচের এখনো কোন হিসাব জেনারেট হয়নি।",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        } else {
            items(expenses, key = { it.id }) { expense ->
                ExpenseItemCard(
                    expense = expense,
                    onDelete = { viewModel.deleteExpense(expense) }
                )
            }
        }
    }
}

@Composable
fun ExpenseItemCard(
    expense: Expense,
    onDelete: () -> Unit
) {
    var showDeleteConfirm by remember { mutableStateOf(false) }

    if (showDeleteConfirm) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            title = { Text("খরচ বাতিল নিশ্চিত করুন?") },
            text = { Text("আপনি কি খরচের খাত '${expense.expenseCategory}' এর এই হিসাব বিবরণটি মুছে ফেলতে নিশ্চিত?") },
            confirmButton = {
                TextButton(onClick = {
                    onDelete()
                    showDeleteConfirm = false
                }) {
                    Text("নিশ্চিত অপসারণ", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirm = false }) {
                    Text("বাতিল করুন")
                }
            }
        )
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("expense_history_card_${expense.id}"),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                modifier = Modifier.weight(1f),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Circle category shorthand
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.errorContainer),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.TrendingDown,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onErrorContainer,
                        modifier = Modifier.size(20.dp)
                    )
                }

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = expense.expenseCategory,
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = SimpleDateFormat("dd MMMM, yyyy", Locale.getDefault()).format(Date(expense.timestamp)),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    if (expense.notes.isNotBlank()) {
                        Text(
                            text = expense.notes,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "৳ ${formatPrice(expense.amount)}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.error
                )

                IconButton(
                    onClick = { showDeleteConfirm = true },
                    modifier = Modifier
                        .padding(start = 4.dp)
                        .testTag("delete_expense_${expense.id}")
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "অপসারণ করুন",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}

// =====================================================================
// 5. HELPER DIALOGS & CORE UTILITIES
// =====================================================================
@Composable
fun ProductSelectorDialog(
    products: List<Product>,
    onDismiss: () -> Unit,
    onSelected: (Product) -> Unit
) {
    var query by remember { mutableStateOf("") }
    val matching = products.filter {
        it.name.contains(query, ignoreCase = true) ||
                it.category.contains(query, ignoreCase = true)
    }

    Dialog(onDismissRequest = onDismiss) {
        ElevatedCard(
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.8f)
                .testTag("product_selector_dialog")
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "হিসারের পণ্য নির্বাচন করুন",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "বন্ধ করুন")
                    }
                }

                // Search box
                OutlinedTextField(
                    value = query,
                    onValueChange = { query = it },
                    label = { Text("পণ্যের নাম বা ক্যাটাগরি লিখুন") },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("selector_search_field"),
                    shape = RoundedCornerShape(10.dp)
                )

                Divider(color = MaterialTheme.colorScheme.outlineVariant)

                if (products.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .weight(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "কোন পণ্য ডাটাবেজে যুক্ত নেই! অনুগ্রহ করে প্রথমে পণ্য যোগ করুন।",
                            style = MaterialTheme.typography.bodySmall,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(16.dp),
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                } else if (matching.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .weight(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "এই নামে কোন পণ্য খুঁজে পাওয়া যায়নি।",
                            style = MaterialTheme.typography.bodySmall,
                            textAlign = TextAlign.Center,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .weight(1f),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        items(matching) { product ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp))
                                    .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(8.dp))
                                    .clickable { onSelected(product) }
                                    .padding(12.dp)
                                    .testTag("selector_item_${product.id}"),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = product.name,
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = "ক্যাটাগরি: ${product.category} | স্টক: ${product.stockQuantity} ${product.unit}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                Column(horizontalAlignment = Alignment.End) {
                                    Text(
                                        text = "৳ ${formatPrice(product.sellingPrice)}",
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                    Text(
                                        text = "ক্রয়: ৳ ${formatPrice(product.purchasePrice)}",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
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

// Formatter Helpers
fun formatTaka(value: Double): String {
    return if (value % 1.0 == 0.0) {
        String.format(Locale.US, "%,d", value.toLong())
    } else {
        String.format(Locale.US, "%,.2f", value)
    }
}

fun formatPrice(value: Double): String {
    return if (value % 1.0 == 0.0) {
        String.format(Locale.US, "%.0f", value)
    } else {
        String.format(Locale.US, "%.2f", value)
    }
}

// Extension to drop anything in parenthesis or bracket to keep display beautiful
private fun String.takeBeforeBracket(): String {
    return this.substringBefore(" (").substringBefore(" (").trim()
}
