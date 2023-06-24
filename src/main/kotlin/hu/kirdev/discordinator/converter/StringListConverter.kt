package hu.kirdev.discordinator.converter

import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.ObjectMapper
import jakarta.persistence.AttributeConverter
import jakarta.persistence.Converter

@Converter
class StringListConverter(
    private val objectMapper: ObjectMapper
) : AttributeConverter<MutableList<String>, String> {

    override fun convertToDatabaseColumn(attribute: MutableList<String>): String {
        return objectMapper.writeValueAsString(attribute)
    }

    override fun convertToEntityAttribute(dbData: String): MutableList<String> {
        return objectMapper.readValue(dbData, object : TypeReference<MutableList<String>>() {})
    }

}