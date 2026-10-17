package task.tracker;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@SpringBootTest
@AutoConfigureMockMvc
@Testcontainers
class ApiIntegrationTest {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine");

    @DynamicPropertySource
    static void datasource(DynamicPropertyRegistry registry) {
        registry.add("DB_URL", postgres::getJdbcUrl);
        registry.add("DB_USER", postgres::getUsername);
        registry.add("DB_PASSWORD", postgres::getPassword);
    }

    @Autowired
    MockMvc mvc;

    @Autowired
    ObjectMapper json;

    private long createAndGetId(String url, String body) throws Exception {
        String response = mvc.perform(post(url).contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        JsonNode node = json.readTree(response);
        return node.get("id").asLong();
    }

    @Test
    void fullCrudFlow() throws Exception {
        long boardId = createAndGetId("/api/boards", "{\"name\":\"Учёба\",\"description\":\"ДЗ\"}");
        long tagId = createAndGetId("/api/tags", "{\"name\":\"срочно\",\"color\":\"#ff0000\"}");

        long taskId = createAndGetId(
                "/api/boards/" + boardId + "/tasks",
                "{\"title\":\"Сдать ДЗ\",\"priority\":\"HIGH\",\"dueDate\":\"2026-10-01\",\"tagIds\":[" + tagId + "]}");

        mvc.perform(get("/api/tasks/" + taskId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("TODO"))
                .andExpect(jsonPath("$.tags", hasSize(1)));

        mvc.perform(put("/api/tasks/" + taskId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"title\":\"Сдать ДЗ №1\",\"status\":\"IN_PROGRESS\",\"tagIds\":[]}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Сдать ДЗ №1"))
                .andExpect(jsonPath("$.priority").value("MEDIUM"))
                .andExpect(jsonPath("$.tags", hasSize(0)));

        mvc.perform(patch("/api/tasks/" + taskId + "/status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"status\":\"DONE\"}"))
                .andExpect(jsonPath("$.status").value("DONE"));

        mvc.perform(get("/api/boards/" + boardId + "/tasks").param("status", "DONE"))
                .andExpect(jsonPath("$", hasSize(1)));
        mvc.perform(get("/api/boards/" + boardId))
                .andExpect(jsonPath("$.taskCount").value(1));

        mvc.perform(delete("/api/boards/" + boardId)).andExpect(status().isNoContent());
        mvc.perform(get("/api/tasks/" + taskId)).andExpect(status().isNotFound());
        mvc.perform(delete("/api/tags/" + tagId)).andExpect(status().isNoContent());
    }

    @Test
    void validationAndConflicts() throws Exception {
        mvc.perform(post("/api/boards").contentType(MediaType.APPLICATION_JSON).content("{\"name\":\"\"}"))
                .andExpect(status().isBadRequest());

        createAndGetId("/api/tags", "{\"name\":\"dup\"}");
        mvc.perform(post("/api/tags").contentType(MediaType.APPLICATION_JSON).content("{\"name\":\"dup\"}"))
                .andExpect(status().isConflict());

        mvc.perform(get("/api/boards/999999")).andExpect(status().isNotFound());
    }
}
