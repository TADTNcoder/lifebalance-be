package com.lifebalance.task.model;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.UUID;

import jakarta.validation.Validation;
import jakarta.validation.Validator;

import org.junit.jupiter.api.Test;

class TagTest {

    private final UUID userId = UUID.randomUUID();
    private final Validator validator = Validation.buildDefaultValidatorFactory().getValidator();

    @Test
    void tagBelongsToOwningUser() {
        Tag tag = baseTag();

        assertThat(tag.belongsTo(userId)).isTrue();
        assertThat(tag.belongsTo(UUID.randomUUID())).isFalse();
        assertThat(tag.belongsTo(null)).isFalse();
    }

    @Test
    void applyDefaultsGeneratesSlugAndSystemFlag() {
        Tag tag = Tag.builder()
                .userId(userId)
                .name("Công việc quan trọng")
                .isSystem(null)
                .build();

        tag.applyDefaults();

        assertThat(tag.getSlug()).isEqualTo("cong-viec-quan-trong");
        assertThat(tag.getIsSystem()).isFalse();
    }

    @Test
    void explicitSlugIsNormalized() {
        Tag tag = baseTag();

        tag.setSlug("  C++ / Work  ");

        assertThat(tag.getSlug()).isEqualTo("c-work");
    }

    @Test
    void beanValidationCatchesRequiredFields() {
        Tag tag = Tag.builder()
                .name(" ")
                .build();

        assertThat(validator.validate(tag))
                .extracting(violation -> violation.getPropertyPath().toString())
                .contains("userId", "name");
    }

    @Test
    void beanValidationCatchesInvalidColor() {
        Tag tag = Tag.builder()
                .userId(userId)
                .name("Work")
                .color("blue")
                .build();

        assertThat(validator.validate(tag))
                .extracting(violation -> violation.getPropertyPath().toString())
                .contains("color");
    }

    @Test
    void beanValidationCatchesNameLengthLimit() {
        Tag tag = Tag.builder()
                .userId(userId)
                .name("a".repeat(101))
                .build();

        assertThat(validator.validate(tag))
                .extracting(violation -> violation.getPropertyPath().toString())
                .contains("name");
    }

    @Test
    void equalsUsesPersistedIdentityOnly() {
        UUID id = UUID.randomUUID();
        Tag first = baseTag();
        Tag second = baseTag();
        first.setId(id);
        second.setId(id);

        assertThat(first).isEqualTo(second);
        assertThat(first.hashCode()).isEqualTo(second.hashCode());
        assertThat(baseTag()).isNotEqualTo(baseTag());
    }

    private Tag baseTag() {
        return Tag.builder()
                .userId(userId)
                .name("Work")
                .build();
    }
}
