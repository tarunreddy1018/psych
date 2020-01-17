package com.psych.game;

import com.psych.game.model.EllenAnswer;
import com.psych.game.model.Round;

public interface EllenStrategy {
    EllenAnswer getAnswer(Round round);
}
