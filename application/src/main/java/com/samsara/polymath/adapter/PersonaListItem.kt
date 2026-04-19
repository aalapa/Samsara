package com.samsara.polymath.adapter

import com.samsara.polymath.data.PersonaWithTaskCount

sealed class PersonaListItem {
    data class PersonaItem(val data: PersonaWithTaskCount) : PersonaListItem()
    object ArchivedHeader : PersonaListItem()
}
