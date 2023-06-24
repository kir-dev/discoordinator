package hu.kirdev.discordinator.converter

import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.ObjectMapper
import hu.kirdev.discordinator.authsch.PersonEntitlement
import jakarta.persistence.AttributeConverter
import jakarta.persistence.Converter

@Converter
class PersonEntitlementListConverter(
        private val objectMapper: ObjectMapper
) : AttributeConverter<MutableList<PersonEntitlement>, String> {

    override fun convertToDatabaseColumn(attribute: MutableList<PersonEntitlement>): String {
        return objectMapper.writeValueAsString(attribute)
    }

    override fun convertToEntityAttribute(dbData: String): MutableList<PersonEntitlement> {
        return objectMapper.readValue(dbData, object : TypeReference<MutableList<PersonEntitlement>>() {})
    }

}