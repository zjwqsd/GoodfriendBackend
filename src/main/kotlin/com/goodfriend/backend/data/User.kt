package com.goodfriend.backend.data

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import jakarta.persistence.*
import java.time.LocalDate
import java.time.LocalDateTime

@Entity
@Table(name = "users")
data class User(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,

    @Column(unique = true, nullable = false)
    var phone: String = "",

    var password: String = "",

    var name: String = "开发用户",

    var age: Int = 18,

    @Enumerated(EnumType.STRING)
    var gender: Gender = Gender.UNKNOWN,

    var region: String = "未知",

    var avatar: String = "user/avatars/default.jpg",  // 相对路径
    var birthday: LocalDate? = null,
    var hobby: String? = null,


    @Column(name = "created_at", updatable = false)
    val createdAt: LocalDateTime = LocalDateTime.now(),

    @Column(name = "updated_at")
    var updatedAt: LocalDateTime = LocalDateTime.now()
)

@Entity
@Table(name = "consultants")
data class Consultant(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,

    var name: String = "开发咨询师",

    @Column(unique = true, nullable = false)
    var phone: String = "",

    var password: String = "",

    var level: String = "初级咨询师",

    @Convert(converter = StringListConverter::class)
    @Column(columnDefinition = "TEXT")
    var specialty: List<String> = listOf("情感关系"),

    @Enumerated(EnumType.STRING)
    var gender: Gender = Gender.UNKNOWN,

    var location: String = "未知",

    var rating: Double = 0.0,

    var avatar: String = "/images/avatars/default.jpg",

    var experienceYears: Int = 0,

    var consultationCount: Int = 0,

    var pricePerHour: Int = 0,

    var trainingHours: Int = 0,
    var supervisionHours: Int = 0,
    @Column(columnDefinition = "TEXT")
    var bio: String = "",

    @Convert(converter = StringListConverter::class)
    @Column(columnDefinition = "TEXT")
    var consultationMethods: List<String> = listOf(),

    @Column(columnDefinition = "TEXT")
    var availability: String = "",

    @Convert(converter = EducationListConverter::class)
    @Column(columnDefinition = "TEXT")
    var educationList: List<Education> = listOf(),

    @Convert(converter = ExperienceListConverter::class)
    @Column(columnDefinition = "TEXT")
    var experienceList: List<Experience> = listOf(),

    @Convert(converter = CertificationListConverter::class)
    @Column(columnDefinition = "TEXT")
    var certificationList: List<Certification> = listOf(),

    @Column(name = "created_at", updatable = false)
    val createdAt: LocalDateTime = LocalDateTime.now(),

    @Column(name = "updated_at")
    var updatedAt: LocalDateTime = LocalDateTime.now()
)


enum class Gender {
    MALE, FEMALE, UNKNOWN
}

@Converter
class StringListConverter : AttributeConverter<List<String>, String> {
    override fun convertToDatabaseColumn(attribute: List<String>?): String {
        return attribute?.joinToString(",") ?: ""
    }

    override fun convertToEntityAttribute(dbData: String?): List<String> {
        return dbData?.split(",")?.filter { it.isNotBlank() } ?: emptyList()
    }
}

data class Education(
    val degree: String,
    val school: String,
    val major: String,
    val time: String
)

data class Experience(
    val company: String,
    val position: String,
    val duration: String,
    val description: String
)

data class Certification(
    val name: String,
    val number: String,
    val issuer: String,
    val date: String
)

object JsonUtils {
    val mapper = jacksonObjectMapper()

    inline fun <reified T> toJson(value: T): String {
        return mapper.writeValueAsString(value)
    }

    inline fun <reified T> fromJson(json: String): T {
        return mapper.readValue(json)
    }
}

@Converter
class EducationListConverter : AttributeConverter<List<Education>, String> {

    override fun convertToDatabaseColumn(attribute: List<Education>?): String {
        return JsonUtils.toJson(attribute ?: emptyList())
    }

    override fun convertToEntityAttribute(dbData: String?): List<Education> {
        return if (dbData.isNullOrBlank()) emptyList()
        else JsonUtils.fromJson(dbData)
    }
}

@Converter
class ExperienceListConverter : AttributeConverter<List<Experience>, String> {

    override fun convertToDatabaseColumn(attribute: List<Experience>?): String {
        return JsonUtils.toJson(attribute ?: emptyList())
    }

    override fun convertToEntityAttribute(dbData: String?): List<Experience> {
        return if (dbData.isNullOrBlank()) emptyList()
        else JsonUtils.fromJson(dbData)
    }
}

@Converter
class CertificationListConverter : AttributeConverter<List<Certification>, String> {

    override fun convertToDatabaseColumn(attribute: List<Certification>?): String {
        return JsonUtils.toJson(attribute ?: emptyList())
    }

    override fun convertToEntityAttribute(dbData: String?): List<Certification> {
        return if (dbData.isNullOrBlank()) emptyList()
        else JsonUtils.fromJson(dbData)
    }
}


