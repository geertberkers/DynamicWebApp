package ui.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import model.SiteConfig
import model.Tab

@Composable
fun BottomNavBar(
    sites: List<SiteConfig>,
    currentTab: Tab,
    onTabSelected: (Tab) -> Unit
) {
    NavigationBar(
        modifier = Modifier.fillMaxWidth()
    ) {
        // All 5 site tabs
        sites.forEachIndexed { index, site ->
            val tab = Tab.Site(index)
            val isSelected = currentTab == tab
            val label = site.title.ifBlank { "Site ${index + 1}" }
            val iconText = site.title.firstOrNull()?.uppercase() ?: "${index + 1}"
            
            NavigationBarItem(
                selected = isSelected,
                onClick = { onTabSelected(tab) },
                icon = {
                    Text(text = iconText)
                },
                label = { 
                    Text(
                        text = label,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    ) 
                }
            )
        }
    }
}

