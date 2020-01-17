package com.psych.game.model;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.psych.game.Constants;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.validator.constraints.URL;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name="questions")
public class Question extends Auditable {
    @NotBlank
    @Getter
    @Setter
    @Column(length = Constants.MAX_QUESTION_LENGTH)
    private String questionText;

    @NotBlank
    @Getter
    @Setter
    @Column(length = Constants.MAX_ANSWER_LENGTH)
    private String correctAnswer;

    @NotNull
    @Getter
    @Setter
    private GameMode gameMode;

    @OneToMany(mappedBy = "question")
    @Getter
    @Setter
    @JsonManagedReference
    private List<EllenAnswer> ellenAnswers = new ArrayList<>();

    private Question(Builder builder) {
        setQuestionText(builder.questionText);
        setCorrectAnswer(builder.correctAnswer);
        setGameMode(builder.gameMode);
    }

    public Question() {}

    public static final class Builder {
        @NotBlank
        private String questionText;

        @NotBlank
        private String correctAnswer;

        @NotNull
        private GameMode gameMode;

        public Builder() {
        }

        public Builder questionText(@NotBlank String val) {
            questionText = val;
            return this;
        }

        public Builder correctAnswer(@NotBlank String val) {
            correctAnswer = val;
            return this;
        }

        public Builder gameMode(@NotNull GameMode val) {
            gameMode = val;
            return this;
        }

        public Question build() {
            return new Question(this);
        }
    }
}
