package hexlet.code.dto.label;

import jakarta.validation.constraints.NotNull;

import org.openapitools.jackson.nullable.JsonNullable;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class LabelUpdateDTO {

    @NotNull
    private JsonNullable<String> name;
}
