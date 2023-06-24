package hu.kirdev.discordinator.converter

import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.ObjectMapper
import jakarta.persistence.AttributeConverter
import jakarta.persistence.Converter

@Converter
class IntListConverter(
    private val objectMapper: ObjectMapper
) : AttributeConverter<MutableList<Int>, String> {

    override fun convertToDatabaseColumn(attribute: MutableList<Int>): String {
        return objectMapper.writeValueAsString(attribute)
    }

    override fun convertToEntityAttribute(dbData: String): MutableList<Int> {
        return objectMapper.readValue(dbData, object : TypeReference<MutableList<Int>>() {})
    }

}