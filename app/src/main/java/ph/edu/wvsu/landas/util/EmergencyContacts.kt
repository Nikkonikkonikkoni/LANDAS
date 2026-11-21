package ph.edu.wvsu.landas.util

data class EmergencyContact(
    val id: String,
    val name: String,
    val category: String,
    val phone: String,
    val displayPhone: String,
    val description: String,
    val available: String,
    val primary: Boolean = false
)

/**
 * Directory entries use publicly listed contact details. Field deployment should
 * re-verify every number with WVSU-PC and the relevant local government office.
 */
object EmergencyContacts {
    val contacts = listOf(
        EmergencyContact(
            id = "national",
            name = "National Emergency Hotline",
            category = "NATIONAL",
            phone = "911",
            displayPhone = "911",
            description = "Unified national emergency line for police, fire, medical, and rescue response.",
            available = "24/7",
            primary = true
        ),
        EmergencyContact(
            id = "campus-security",
            name = "WVSU-PC Campus Security",
            category = "CAMPUS",
            phone = "0335298716",
            displayPhone = "(033) 529-8716",
            description = "WVSU-Pototan Campus trunk line. Ask to be connected to the security office.",
            available = "Campus trunk line",
            primary = true
        ),
        EmergencyContact(
            id = "cdrrmo-coordinator",
            name = "CDRRMO / MDRRMO Pototan",
            category = "CAMPUS",
            phone = "09307529623",
            displayPhone = "0930 752 9623",
            description = "Municipal disaster risk reduction and management office hotline for rescue coordination.",
            available = "24/7 hotline"
        ),
        EmergencyContact(
            id = "health-services",
            name = "WVSU-PC Health Services",
            category = "CAMPUS",
            phone = "0335298716",
            displayPhone = "(033) 529-8716",
            description = "Campus clinic and first-aid support through the campus trunk line.",
            available = "Office hours"
        ),
        EmergencyContact(
            id = "pototan-fire",
            name = "Pototan Fire Station",
            category = "MUNICIPAL",
            phone = "09093365139",
            displayPhone = "0909 336 5139",
            description = "Bureau of Fire Protection – Pototan Fire Station emergency hotline.",
            available = "24/7",
            primary = true
        ),
        EmergencyContact(
            id = "pototan-fire-landline",
            name = "Pototan Fire Station – Landline",
            category = "MUNICIPAL",
            phone = "0335022108",
            displayPhone = "(033) 502-2108",
            description = "Landline dispatch line for the Pototan Fire Station.",
            available = "24/7"
        ),
        EmergencyContact(
            id = "pototan-police",
            name = "Pototan Municipal Police Station",
            category = "MUNICIPAL",
            phone = "09985986794",
            displayPhone = "0998 598 6794",
            description = "Pototan MPS hotline for police assistance and security response.",
            available = "24/7",
            primary = true
        ),
        EmergencyContact(
            id = "pototan-police-alt",
            name = "Pototan Police – Alternate",
            category = "MUNICIPAL",
            phone = "09488190417",
            displayPhone = "0948 819 0417",
            description = "Alternate Pototan Municipal Police Station hotline.",
            available = "24/7"
        ),
        EmergencyContact(
            id = "iloilo-pdrrmo",
            name = "Iloilo Provincial DRRMO",
            category = "PROVINCIAL",
            phone = "09989686790",
            displayPhone = "0998 968 6790",
            description = "Provincial disaster response coordination for Iloilo.",
            available = "24/7 hotline"
        ),
        EmergencyContact(
            id = "iloilo-pdrrmo-office",
            name = "Iloilo PDRRMO – Office",
            category = "PROVINCIAL",
            phone = "0333287920",
            displayPhone = "(033) 328-7920",
            description = "Iloilo Provincial Capitol disaster risk reduction office.",
            available = "Office hours"
        )
    )

    fun primaryContacts() = contacts.filter { it.primary }
}
