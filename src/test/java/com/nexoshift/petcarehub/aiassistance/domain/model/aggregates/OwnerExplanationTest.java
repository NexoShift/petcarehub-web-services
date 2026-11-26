package com.nexoshift.petcarehub.aiassistance.domain.model.aggregates;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.nexoshift.petcarehub.aiassistance.domain.model.commands.RequestOwnerExplanationCommand;
import org.junit.jupiter.api.Test;

class OwnerExplanationTest {

  @Test
  void commandTrimsTheQuestion() {
    var command = new RequestOwnerExplanationCommand(1L, null, 9L, "  ¿Qué tiene?  ");
    assertEquals("¿Qué tiene?", command.question());
  }

  @Test
  void commandRejectsBlankOrLongQuestions() {
    assertThrows(IllegalArgumentException.class,
        () -> new RequestOwnerExplanationCommand(1L, null, 9L, "  "));
    assertThrows(IllegalArgumentException.class,
        () -> new RequestOwnerExplanationCommand(1L, null, 9L, "a".repeat(1001)));
    assertThrows(IllegalArgumentException.class,
        () -> new RequestOwnerExplanationCommand(null, null, 9L, "¿Qué tiene?"));
  }

  @Test
  void keepsQuestionAnswerAndOwner() {
    var explanation = new OwnerExplanation(
        new RequestOwnerExplanationCommand(1L, null, 9L, "¿Qué tiene?"), " Una alergia. ", "fake");

    assertEquals(1L, explanation.getPetId());
    assertNull(explanation.getConsultationId());
    assertEquals(9L, explanation.getOwnerId());
    assertEquals("Una alergia.", explanation.getAnswer());
  }

  @Test
  void rejectsAnEmptyAnswer() {
    var command = new RequestOwnerExplanationCommand(1L, 1L, 9L, "¿Qué tiene?");
    assertThrows(IllegalArgumentException.class, () -> new OwnerExplanation(command, " ", "m"));
  }
}
