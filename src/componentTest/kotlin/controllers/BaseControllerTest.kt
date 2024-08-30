package controllers

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import org.junit.jupiter.api.BeforeEach
import org.mockito.kotlin.whenever
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.http.MediaType
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.ResultActions
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders
import tech.hexd.adaptiveLearningCompanion.controllers.TaskCreateRequest
import tech.hexd.adaptiveLearningCompanion.repositories.*
import tech.hexd.adaptiveLearningCompanion.services.UserDetailsServiceImpl
import tech.hexd.adaptiveLearningCompanion.util.JwtUtil
import java.time.LocalDate
import java.util.*

abstract class BaseControllerTest {
    @Autowired
    protected lateinit var mockMvc: MockMvc

    @MockBean
    protected lateinit var jwtUtil: JwtUtil

    @MockBean
    protected lateinit var appUserRepository: AppUserRepository

    @MockBean
    protected lateinit var taskRepository: TaskRepository

    @MockBean
    protected lateinit var dailyPlanRepository: DailyPlanRepository

    @MockBean
    protected lateinit var userDetailsService: UserDetailsServiceImpl

    protected val testToken = "someValidToken"
    protected val testUsername = "someUsername"
    protected val testUserId = "someUserId"

    @BeforeEach
    fun baseSetup() {
        whenever(jwtUtil.validateToken(testToken)).thenReturn(true)
        whenever(jwtUtil.extractUsername(testToken)).thenReturn(testUsername)
        whenever(jwtUtil.extractRoles(testToken)).thenReturn(arrayListOf("ROLE_USER"))
    }

    protected fun performAuthenticatedPost(url: String, content: Any): ResultActions =
        mockMvc.perform(
            MockMvcRequestBuilders.post(url)
                .header("Authorization", "Bearer $testToken")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(jacksonObjectMapper().writeValueAsString(content))
        )

    protected fun performAuthenticatedGet(url: String): ResultActions =
        mockMvc.perform(
            MockMvcRequestBuilders.get(url)
                .header("Authorization", "Bearer $testToken")
                .with(csrf())
        )

    protected fun createTestTaskCreateRequest(
        category: TaskCategory = TaskCategory.BLUE,
        size: TaskSize = TaskSize.BIG,
        description: String = "finish implementation of TaskController tests"
    ) = TaskCreateRequest(category, size, description)

    protected fun createTestSavedTaskResponse(
        id: String = "someSavedTaskId",
        category: TaskCategory = TaskCategory.BLUE,
        size: TaskSize = TaskSize.BIG,
        description: String = "finish implementation of TaskController tests"
    ) = Task(id, testUsername, category, size, description)

    protected fun generateRandomTaskFor(username: String): Task {
        return Task(
            id = UUID.randomUUID().toString(),
            ownerUsername = username,
            category = TaskCategory.entries.toTypedArray().random(),
            size = TaskSize.entries.toTypedArray().random(),
            description = generateRandomDescription(),
        )
    }

    private fun generateRandomDescription(): String {
        val adjectives = listOf("Urgent", "Important", "Routine", "Critical", "Optional")
        val verbs = listOf("Implement", "Debug", "Refactor", "Optimize", "Test")
        val nouns = listOf("feature", "bug", "module", "function", "algorithm")

        return "${adjectives.random()} task: ${verbs.random()} the ${nouns.random()}"
    }

    protected fun createRandomDailyPlanFor(username: String, date: LocalDate): DailyPlan {
        val todo = List(3) { generateRandomTaskFor(username) }
        val done = List(3) { generateRandomTaskFor(username) }
        return DailyPlan(
            id = UUID.randomUUID().toString(),
            ownerUsername = username,
            day = date,
            todo = todo,
            done = done,
        )
    }
}
