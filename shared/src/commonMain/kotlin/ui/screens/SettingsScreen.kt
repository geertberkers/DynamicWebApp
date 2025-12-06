package ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import data.PredefinedSitesRepository
import data.WebsiteMetadataService
import kotlinx.coroutines.launch
import model.PredefinedSite
import model.SiteConfig

@Composable
fun SettingsScreen(
    sites: List<SiteConfig>,
    metadataService: WebsiteMetadataService,
    predefinedSitesRepository: PredefinedSitesRepository,
    onSitesChanged: (List<SiteConfig>) -> Unit
) {
    var localSites by remember { mutableStateOf(sites) }
    var loadingIndex by remember { mutableStateOf<Int?>(null) }
    var errorMessages by remember { mutableStateOf<Map<Int, String>>(emptyMap()) }
    var showMaxSelectionDialog by remember { mutableStateOf(false) }
    var showAddSiteDialog by remember { mutableStateOf(false) }
    var showLoadFromUrlDialog by remember { mutableStateOf(false) }
    var isLoadingFromUrl by remember { mutableStateOf(false) }
    var loadUrlError by remember { mutableStateOf<String?>(null) }
    var allPredefinedSites by remember { mutableStateOf(predefinedSitesRepository.getAllSites()) }
    var refreshTrigger by remember { mutableStateOf(0) }
    val scope = rememberCoroutineScope()
    
    // Refresh sites list when trigger changes or when screen is displayed
    LaunchedEffect(refreshTrigger) {
        allPredefinedSites = predefinedSitesRepository.getAllSites()
    }
    
    // Also refresh when screen first appears (in case sites were loaded in background)
    LaunchedEffect(Unit) {
        allPredefinedSites = predefinedSitesRepository.getAllSites()
    }
    
    // Track which predefined sites are selected based on current sites
    val selectedPredefinedSites = localSites
        .mapNotNull { site -> 
            if (site.url.isNotBlank()) {
                allPredefinedSites.find { it.url == site.url }?.url
            } else null
        }
        .toSet()
    
    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddSiteDialog = true },
                modifier = Modifier.padding(16.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Site toevoegen"
                )
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            Text(
                text = "Configure Sites",
                style = MaterialTheme.typography.headlineMedium,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            
            Text(
                text = "Selecteer maximaal 5 sites",
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = { showLoadFromUrlDialog = true },
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Laad van URL")
                }
                
                val remoteUrl = predefinedSitesRepository.getRemoteSitesUrl()
                if (remoteUrl != null) {
                    Text(
                        text = "URL: $remoteUrl",
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier
                            .weight(1f)
                            .padding(start = 8.dp)
                    )
                }
            }
            
            // Predefined sites list
            PredefinedSitesList(
                sites = allPredefinedSites,
                selectedUrls = selectedPredefinedSites,
                onSiteSelected = { predefinedSite ->
                    val currentSelectedCount = selectedPredefinedSites.size
                    
                    if (selectedPredefinedSites.contains(predefinedSite.url)) {
                        // Deselect
                        val updatedSites = localSites.toMutableList()
                        val indexToClear = updatedSites.indexOfFirst { it.url == predefinedSite.url }
                        if (indexToClear != -1) {
                            updatedSites[indexToClear] = updatedSites[indexToClear].copy(
                                url = "",
                                title = "",
                                iconUrl = null
                            )
                            localSites = updatedSites
                            onSitesChanged(updatedSites)
                        }
                    } else {
                        // Select
                        if (currentSelectedCount >= 5) {
                            showMaxSelectionDialog = true
                        } else {
                            // Find first empty slot or replace last one
                            val emptyIndex = localSites.indexOfFirst { !it.isConfigured }
                            val targetIndex = if (emptyIndex != -1) emptyIndex else currentSelectedCount
                            
                            scope.launch {
                                loadingIndex = targetIndex
                                metadataService.fetchMetadata(predefinedSite.url).fold(
                                    onSuccess = { metadata ->
                                        val updatedSite = metadataService.applyToConfig(
                                            old = localSites[targetIndex],
                                            metadata = metadata,
                                            url = predefinedSite.url
                                        ).copy(
                                            title = metadata.title.ifBlank { 
                                                predefinedSite.url.removePrefix("https://").removePrefix("http://")
                                            }
                                        )
                                        val updatedSites = localSites.toMutableList().apply {
                                            this[targetIndex] = updatedSite
                                        }
                                        localSites = updatedSites
                                        onSitesChanged(updatedSites)
                                        loadingIndex = null
                                    },
                                    onFailure = { error ->
                                        // Still add the site even if metadata fetch fails
                                        val updatedSite = localSites[targetIndex].copy(
                                            url = predefinedSite.url,
                                            title = predefinedSite.url.removePrefix("https://").removePrefix("http://")
                                        )
                                        val updatedSites = localSites.toMutableList().apply {
                                            this[targetIndex] = updatedSite
                                        }
                                        localSites = updatedSites
                                        onSitesChanged(updatedSites)
                                        loadingIndex = null
                                    }
                                )
                            }
                        }
                    }
                }
            )
            
            Spacer(modifier = Modifier.height(24.dp))
            
            Divider(modifier = Modifier.padding(vertical = 8.dp))
            
            Text(
                text = "Handmatige configuratie",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(vertical = 8.dp)
            )
            
            // Manual configuration sections
            localSites.forEachIndexed { index, site ->
                SiteConfigSection(
                    site = site,
                    urlText = site.url,
                    titleText = site.title,
                    isLoading = loadingIndex == index,
                    errorMessage = errorMessages[index],
                    onUrlChanged = { newUrl ->
                        localSites = localSites.toMutableList().apply {
                            this[index] = this[index].copy(url = newUrl)
                        }
                        errorMessages = errorMessages.toMutableMap().apply {
                            remove(index)
                        }
                    },
                    onSave = {
                        scope.launch {
                            loadingIndex = index
                            errorMessages = errorMessages.toMutableMap().apply {
                                remove(index)
                            }
                            
                            val url = localSites[index].url.trim()
                            if (url.isBlank()) {
                                errorMessages = errorMessages.toMutableMap().apply {
                                    put(index, "URL cannot be empty")
                                }
                                loadingIndex = null
                                return@launch
                            }
                            
                            metadataService.fetchMetadata(url).fold(
                                onSuccess = { metadata ->
                                    val normalizedUrl = when {
                                        url.startsWith("http://") || url.startsWith("https://") -> url
                                        url.isNotBlank() -> "https://$url"
                                        else -> url
                                    }
                                    val updatedSite = metadataService.applyToConfig(
                                        old = localSites[index],
                                        metadata = metadata,
                                        url = normalizedUrl
                                    )
                                    localSites = localSites.toMutableList().apply {
                                        this[index] = updatedSite
                                    }
                                    onSitesChanged(localSites)
                                    loadingIndex = null
                                },
                                onFailure = { error ->
                                    errorMessages = errorMessages.toMutableMap().apply {
                                        put(index, "Failed to fetch metadata: ${error.message}")
                                    }
                                    loadingIndex = null
                                }
                            )
                        }
                    }
                )
                
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
    
    // Max selection dialog
    if (showMaxSelectionDialog) {
        AlertDialog(
            onDismissRequest = { showMaxSelectionDialog = false },
            title = { Text("Maximum bereikt") },
            text = { Text("Je kunt maximaal 5 sites selecteren. Deselecteer eerst een site om een nieuwe te kiezen.") },
            confirmButton = {
                TextButton(onClick = { showMaxSelectionDialog = false }) {
                    Text("OK")
                }
            }
        )
    }
    
    // Add site dialog
    if (showAddSiteDialog) {
        AddSiteDialog(
            onDismiss = { showAddSiteDialog = false },
            onConfirm = { category, name, link, description ->
                val normalizedUrl = when {
                    link.startsWith("http://") || link.startsWith("https://") -> link
                    link.isNotBlank() -> "https://$link"
                    else -> link
                }
                val newSite = PredefinedSite(
                    category = category,
                    url = normalizedUrl,
                    description = description
                )
                predefinedSitesRepository.addCustomSite(newSite)
                refreshTrigger++
                showAddSiteDialog = false
            }
        )
    }
    
    // Load from URL dialog
    if (showLoadFromUrlDialog) {
        LoadFromUrlDialog(
            isLoading = isLoadingFromUrl,
            error = loadUrlError,
            onDismiss = { 
                showLoadFromUrlDialog = false
                loadUrlError = null
            },
            onConfirm = { url ->
                scope.launch {
                    isLoadingFromUrl = true
                    loadUrlError = null
                    
                    predefinedSitesRepository.loadSitesFromUrl(url).fold(
                        onSuccess = { sites ->
                            refreshTrigger++
                            isLoadingFromUrl = false
                            showLoadFromUrlDialog = false
                        },
                        onFailure = { error ->
                            loadUrlError = error.message ?: "Fout bij laden van URL"
                            isLoadingFromUrl = false
                        }
                    )
                }
            }
        )
    }
}

@Composable
private fun LoadFromUrlDialog(
    isLoading: Boolean,
    error: String?,
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit
) {
    var url by remember { mutableStateOf("") }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Sites laden van URL") },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedTextField(
                    value = url,
                    onValueChange = { url = it },
                    label = { Text("JSON URL") },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("https://example.com/sites.json") },
                    enabled = !isLoading
                )
                
                if (isLoading) {
                    LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
                }
                
                if (error != null) {
                    Text(
                        text = error,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
                
                Text(
                    text = "De JSON moet de volgende structuur hebben:\n{\n  \"sites\": [\n    {\n      \"category\": \"...\",\n      \"url\": \"...\",\n      \"description\": \"...\"\n    }\n  ]\n}",
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { onConfirm(url) },
                enabled = url.isNotBlank() && !isLoading
            ) {
                Text("Laden")
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                enabled = !isLoading
            ) {
                Text("Annuleren")
            }
        }
    )
}

@Composable
private fun AddSiteDialog(
    onDismiss: () -> Unit,
    onConfirm: (String, String, String, String) -> Unit
) {
    var category by remember { mutableStateOf("") }
    var name by remember { mutableStateOf("") }
    var link by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Site toevoegen") },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedTextField(
                    value = category,
                    onValueChange = { category = it },
                    label = { Text("Categorie (optioneel)") },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("Bijv: 📝 Productiviteit & Planning") }
                )
                
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Naam") },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("Bijv: Mijn Site") }
                )
                
                OutlinedTextField(
                    value = link,
                    onValueChange = { link = it },
                    label = { Text("Link") },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("Bijv: example.com of https://example.com") }
                )
                
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Beschrijving") },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("Beschrijving van de site") },
                    maxLines = 3
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (link.isNotBlank()) {
                        onConfirm(category, name, link, description)
                    }
                },
                enabled = link.isNotBlank()
            ) {
                Text("Toevoegen")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Annuleren")
            }
        }
    )
}

@Composable
private fun PredefinedSitesList(
    sites: List<PredefinedSite>,
    selectedUrls: Set<String>,
    onSiteSelected: (PredefinedSite) -> Unit
) {
    val sitesByCategory = sites.groupBy { it.category.ifBlank { "Algemeen" } }
    
    sitesByCategory.forEach { (category, categorySites) ->
        if (category.isNotBlank()) {
            Text(
                text = category,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(top = 16.dp, bottom = 8.dp)
            )
        }
        
        categorySites.forEach { site ->
            PredefinedSiteCard(
                site = site,
                isSelected = selectedUrls.contains(site.url),
                onToggleSelection = { onSiteSelected(site) }
            )
            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}

@Composable
private fun PredefinedSiteCard(
    site: PredefinedSite,
    isSelected: Boolean,
    onToggleSelection: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = site.url.removePrefix("https://").removePrefix("http://"),
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
                if (site.description.isNotBlank()) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = site.description,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            
            IconButton(
                onClick = onToggleSelection,
                modifier = Modifier.size(48.dp)
            ) {
                Text(
                    text = if (isSelected) "★" else "☆",
                    style = MaterialTheme.typography.headlineMedium,
                    color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun SiteConfigSection(
    site: SiteConfig,
    urlText: String,
    titleText: String,
    isLoading: Boolean,
    errorMessage: String?,
    onUrlChanged: (String) -> Unit,
    onSave: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(
            text = "Site ${site.id + 1}",
            style = MaterialTheme.typography.titleSmall,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        
        TextField(
            value = urlText,
            onValueChange = onUrlChanged,
            label = { Text("URL") },
            modifier = Modifier.fillMaxWidth(),
            enabled = !isLoading
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Button(
            onClick = onSave,
            enabled = !isLoading
        ) {
            Text("Save")
        }
        
        if (isLoading) {
            Spacer(modifier = Modifier.height(8.dp))
            LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
        }
        
        if (titleText.isNotBlank()) {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Title: $titleText",
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(start = 4.dp)
            )
        } else {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Title: N/A",
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(start = 4.dp)
            )
        }
        
        if (errorMessage != null) {
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = errorMessage,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.padding(start = 4.dp)
            )
        }
    }
}
