package hexlet.code.dto.task;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import org.openapitools.jackson.nullable.JsonNullable;

import java.util.Set;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class TaskUpdateDTO {

    private JsonNullable<Integer> index;

    @JsonProperty("assignee_id")
    private JsonNullable<Long> assigneeId;

    @NotBlank
    private JsonNullable<String> title;

    private JsonNullable<String> content;

    @NotNull
    private JsonNullable<String> status;

    private JsonNullable<Set<Long>> taskLabelIds;
}
