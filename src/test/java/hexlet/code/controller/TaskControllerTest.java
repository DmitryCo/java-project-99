package hexlet.code.controller;

import hexlet.code.util.ModelGenerator;
import hexlet.code.dto.task.TaskCreateDTO;
import hexlet.code.model.Task;
import hexlet.code.model.Label;
import hexlet.code.model.TaskStatus;
import hexlet.code.model.User;
import hexlet.code.repository.TaskRepository;
import hexlet.code.repository.TaskStatusRepository;
import hexlet.code.repository.LabelsRepository;
import hexlet.code.repository.UserRepository;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.instancio.Instancio;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.JwtRequestPostProcessor;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.nio.charset.StandardCharsets;
import java.util.Set;

import static net.javacrumbs.jsonunit.assertj.JsonAssertions.assertThatJson;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class TaskControllerTest {
    @Autowired
    private WebApplicationContext wac;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper om;

    @Autowired
    private ModelGenerator modelGenerator;

    private JwtRequestPostProcessor token;
    private User testUser;
    private Task testTask;
    private TaskStatus testStatus;
    @Autowired
    private TaskRepository taskRepository;

    @Autowired
    private UserRepository userRepository;
    @Autowired
    private LabelsRepository labelsRepository;
    @Autowired
    private TaskStatusRepository taskStatusRepository;

    @BeforeEach
    public void clean() {
        taskRepository.deleteAll();
        userRepository.deleteAll();
        taskStatusRepository.deleteAll();
    }

    @BeforeEach
    public void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(wac)
                .defaultResponseCharacterEncoding(StandardCharsets.UTF_8)
                .apply(springSecurity())
                .build();

        testUser = Instancio.of(modelGenerator.getUserModel()).create();
        token = jwt().jwt(builder -> builder.subject(testUser.getEmail()));
        testTask = Instancio.of(modelGenerator.getTaskModel()).create();
        testStatus = Instancio.of(modelGenerator.getTaskStatusModel()).create();
        taskRepository.save(testTask);
        userRepository.save(testUser);
        taskStatusRepository.save(testStatus);
    }


    @Test
    public void testShow() throws Exception {

        var request = get("/api/tasks/{id}", testTask.getId()).with(token);

        var result = mockMvc.perform(request)
                .andExpect(status().isOk())
                .andReturn();
    }

    @Test
    public void testIndex() throws Exception {

        var result = mockMvc.perform(get("/api/tasks").with(token))
                .andExpect(status().isOk())
                .andReturn();

        var body = result.getResponse().getContentAsString();

        assertThatJson(body).isArray();
    }

    @Test
    public void testFilteredIndex() throws Exception {
        var user = Instancio.of(modelGenerator.getUserModel())
                .create();
        userRepository.save(user);

        var status = new TaskStatus();
        status.setName("TestStatus");
        status.setSlug("forTest");
        taskStatusRepository.save(status);

        var label = new Label();
        label.setName("testLabel");
        labelsRepository.save(label);

        var task = new Task();
        task.setName("TaskName");
        task.setAssignee(user);
        task.setTaskStatus(status);
        task.setLabels(Set.of(label));
        taskRepository.save(task);

        var result = mockMvc.perform(get(
                        "/api/tasks?titleCont=TaskName&assigneeId={id}&status=forTest&labelId={labId}",
                        user.getId(), label.getId())
                        .with(token))
                .andExpect(status().isOk())
                .andReturn();
        var body = result.getResponse().getContentAsString();

        assertTrue(body.contains(String.valueOf(user.getId())));
        assertTrue(body.contains(status.getSlug()));
        assertTrue(body.contains(String.valueOf(label.getId())));
    }

    @Test
    public void testCreate() throws Exception {
        var dto = new TaskCreateDTO();
        Task task = Instancio.of(modelGenerator.getTaskModel()).create();

        dto.setTitle(task.getName());
        dto.setContent(task.getDescription());
        dto.setIndex(task.getIndex());
        dto.setAssigneeId(testUser.getId());
        dto.setStatus(testStatus.getSlug());

        var request = post("/api/tasks")
                .with(token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(om.writeValueAsString(dto));

        mockMvc.perform(request)
                .andExpect(status().isCreated());

        var savedTask = taskRepository.findByName(dto.getTitle()).orElseThrow();

        assertThat(savedTask.getName()).isEqualTo(dto.getTitle());
        assertThat(savedTask.getDescription()).isEqualTo(dto.getContent());
        assertThat(savedTask.getIndex()).isEqualTo(dto.getIndex());
        assertThat(savedTask.getTaskStatus().getSlug()).isEqualTo(dto.getStatus());
    }

    @Test
    public void testDelete() throws Exception {

        var request = delete("/api/tasks/{id}", testTask.getId()).with(token);

        mockMvc.perform(request)
                .andExpect(status().isNoContent());

        assertFalse(taskRepository.findById(testTask.getId()).isPresent());
    }
}
