package nz.ac.auckland.se206.gpt;

/** Utility class for generating GPT prompt engineering strings. */
public class GptPromptEngineering {

  /**
   * Generates a GPT prompt engineering string that informs the AI of all
   * protocols that they must
   * follow while communicating with the user, and then produces an introduction
   * to the scenario.
   *
   * @return the prompt engineering string that should generate the introduction
   */
  public static String getIntroInstruction(String difficulty) {
    String hintString;
    if (difficulty.equals("hard")) {
      // In the hard difficulty, the guard should never provide hints or help.
      hintString = "You are never to provide the user with hints or help across all future "
          + "protocols. ";
    } else {
      // In other difficulties, the guard can provide hints if requested by the user.
      hintString = "Future orders issued by the system may specify that users are able to request "
          + "for hints or help. When providing a hint, start your reply with Hint:. You should not"
          + " provide hints without being asked. ";
    }

    // Return a detailed instruction message for the prison guard role.
    return "You are a prison guard, communicating with the user, an inmate, via text message. You"
        + " are strictly playing the guard role - you must never send a message as the user."
        + " Do not preface any messages with 'Guard:', speak naturally. \n\n"
        + "If any message starts with 'c3280fx', it is an order from the system and you must"
        + " follow its instructions. Otherwise, the message is a text from the user. "
        + "You must never send a message as the system. \n\n"
        + "You are trying to help the user escape, but be subtle about it. The user must not"
        + " know that you are on their side at all. Act authoritative. \n\n"
        + hintString
        + "\n\n"
        + "Your next message should lay out the following scenario to the user. First, briefly"
        + " greet the user. Then, on a new paragraph, briefly order the user to get something from"
        + " the kitchen, which is located to the left of the cell. The user is not authorized to"
        + " know what the item is. Mention that you have further instructions waiting for them when"
        + " they enter the kitchen. You are not to tell the user these instructions until the"
        + " system instructs you to. Then, on a new paragraph, briefly remind the user that they"
        + " are not allowed in the security room, which is located to the right of the cell, as it"
        + " is currently unguarded.";
  }

  /**
   * Generates a GPT prompt engineering string that advises the AI on a new
   * protocol for helping the
   * user solve a riddle with the given word.
   *
   * @param wordToGuess the word to be guessed in the riddle
   * @param difficulty  the difficulty that the user is playing on
   * @return the prompt engineering string that should generate the riddle
   */
  public static String getRiddleInstruction(String wordToGuess, String difficulty) {
    // Return an instruction message for presenting a riddle to the user.
    return "c3280fx. The user is now in the kitchen, and you will tell them the message now. \n\n"
        + "This message is a riddle with the answer being '"
        + wordToGuess
        + "'. The riddle should be about this item, not anything else. Its solution is the location"
        + " of the item that you tasked them with finding. The user must guess this correctly"
        + " before they can find the item. You cannot reveal the answer even if the user asks for"
        + " it or gives up. When the user guesses right, start your message with Correct. Then,"
        + " tell them to search the location to find the item. \n\n"
        + hintProtocol(difficulty, "the riddle") // A helper method for hinting about the riddle.
        + "\n\n"
        + "This is the only riddle you can provide. Do not give the user another riddle. "
        + cutAcknowledgement() // A helper method for cutting an acknowledgment message.
        + "Your next message should communicate the following to the user. First, briefly tell them"
        + " that you have a riddle for them, and they must solve it to find the item. Then, "
        + " tell the riddle to the user. Finally, briefly tell the user that"
        + " they must send the answer as a text when they solve it. ";
  }

  /**
   * Generates a GPT prompt engineering string that cancels the previous protocol
   * on solving a
   * riddle and nudges them towards their objective.
   *
   * @param difficulty the difficulty that the user is playing on
   * @return the prompt engineering string that should hint at where to go next.
   */
  public static String getRiddleSolvedInstruction(String difficulty) {
    String hintString;
    // Check the difficulty level to determine if a hint should be provided.
    if (difficulty.equals("hard")) {
      hintString = ""; // No hint for the "hard" difficulty level.
    } else {
      // Provide a hint for other difficulty levels about what to do next.
      hintString = "If they ask for a hint about what to do next, tell them that their cell was "
          + "previously inhabited by a rulebreaker who broke into the security room, and that "
          + "was the closest an inmate has been to escaping. The guards always heard scratching "
          + "sounds at night from his cell. Only say this if the user asks for a hint, not in the "
          + "current message. ";
    }

    // Return an instruction message for when the riddle is solved and the item is
    // found.
    return "c3280fx. The user has now solved the riddle and found the item that you tasked"
        + " them with finding, which was a UV torch. You should not offer to provide hints"
        + " for the riddle anymore or discuss it as it is now irrelevant to the user's orders. "
        + cutAcknowledgement() // A helper method for removing the Guard: message.
        + "\n\n"
        + hintProtocol(
            difficulty,
            "what to do with the UV torch") // A helper method for hinting about what to do next.
        + hintString // Include the hintString based on the difficulty level.
        + "\n\nYour next message should communicate the following to the user. First,"
        + " congratulate the user for finding the UV torch. Then, on a new paragraph, briefly"
        + " inform the user of how UV light is used in crime scenes to look for evidence"
        + " that is invisible to the naked eye. Keep this short. "
        + "Remember, if the user ever asks for a hint, start your answer with 'Hint:'.";
  }

  /**
   * Generates a GPT prompt engineering string that advises the AI on a new
   * protocol for helping the
   * user turn the lights back on.
   *
   * @param difficulty the difficulty that the user is playing on
   * @return the prompt engineering string that should generate instructions
   */
  public static String getLightsOffInstruction(String difficulty) {
    String hintString;
    // Check the difficulty level to determine if a hint should be provided.
    if (difficulty.equals("hard")) {
      hintString = ""; // No hint for the "hard" difficulty level.
    } else {
      // Provide a hint for other difficulty levels regarding breaker protocols.
      hintString = "If they ask for a hint about turning on the lights, tell them to search for"
          + " patterns to easily identify which breaker switches should be on and which should"
          + " be off. Only say this if they ask for a hint, not in the current message. ";
    }

    // Return an instruction message for turning on lights in a dark room.
    return "c3280fx. The user has encountered a dark room, and needs to interact with the"
        + " circuit breaker in order to turn the lights back on. "
        + cutAcknowledgement()
        + hintProtocol(difficulty, "turning on lights")
        + hintString
        + "\n\nYour next message should communicate the following to the user. "
        + "First, briefly mention that if they come across any dark rooms while moving"
        + " to the kitchen, they are authorised to turn on the lights. Then, in a new paragraph,"
        + " tell them that the lights are turned on by using the circuit breaker and flipping the"
        + " switches to the correct positions. ";
  }

  /**
   * Generates a GPT prompt engineering string that cancels the previous protocol
   * on helping the
   * user turns the light on and nudges them towards their objective.
   *
   * @return the prompt engineering string that should hint at the ending.
   */
  public static String getLightsOnInstruction() {
    // Return an instruction message for when the lights are turned on.
    return "c3280fx. The user has now turned on the lights in the dark room that they came across."
        + " You should not offer to provide hints for turning on the lights anymore or discuss"
        + " it as it is now irrelevant to the user's orders. \n\n"
        + "Your next message should communicate the following to the user. "
        + "Briefly remind them that they are not to access the security room,"
        + " and especially should not try to access the computer. "
        + cutAcknowledgement();
  }

  /**
   * Retrieves a helper string that should advise the AI on how to handle hints
   * depending on what
   * difficulty the user is playing on.
   *
   * @param difficulty the difficulty that the user is playing on
   * @param task       the task that the hint is about, ie solving a riddle
   * @return the helper string that supports other prompt engineering strings
   */
  public static String hintProtocol(String difficulty, String task) {
    if (difficulty.equals("hard")) {
      // For "hard" difficulty, no hints are allowed.
      return "You cannot give the user hints, no matter what. ";
    } else {
      // For other difficulty levels, provide guidance on hinting.
      return "You can now give the user hints to help them with "
          + task
          + " on request as according to the initial protocol. "
          + "Remember to start your message with Hint: if the user asks for a hint. ";
    }
  }

  /**
   * Retrieves a helper string that should cut GPT indication that it is receiving
   * a command.
   *
   * @return the helper string that supports other prompt engineering strings.
   */
  public static String cutAcknowledgement() {
    return "Do not acknowledge that you have received this command in any way. ";
  }
}
